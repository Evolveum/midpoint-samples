import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.StringUtil
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.*

import java.sql.Timestamp
import java.util.function.BiFunction
import java.util.function.Function


def configuration = configuration as ScriptedSQLConfiguration
def operation = operation as OperationType
def objectClass = objectClass as ObjectClass
def log = log as Log

log.info("Entering " + operation + " Script");

def sql = new Sql(connection)

switch (operation) {
    case OperationType.SYNC:
        def token = token as Object
        def handler = handler as SyncResultsHandler

        handleSync(sql, token, handler)
        break
    case OperationType.GET_LATEST_SYNC_TOKEN:
        return handleGetLatestSyncToken(sql)
}

void handleSync(Sql sql, Object tokenObject, SyncResultsHandler handler) {
    String token = (String) tokenObject

    switch (objectClass) {
        case ObjectClass.ACCOUNT:
            handleSyncAccount(sql, token, handler)
            break
        default:
            throw new ConnectorException("Unknown object class " + objectClass)
    }
}

Object handleGetLatestSyncToken(Sql sql) {
    switch (objectClass) {
        case ObjectClass.ACCOUNT:
            return handleSyncTokenForAccount(sql)
        default:
            throw new ConnectorException("Unknown object class " + objectClass)
    }
}

private void handleSyncAccount(Sql sql, String token, SyncResultsHandler handler) {
    String query = Constants.SYNC_ACCOUNT

    handleSyncGeneric(sql, token, handler, query,
            { t -> buildParamsFromTokenForAccount(t) },
            syncTokenRowTransformForAccount(),
            { sql1, result -> buildAccountObject(sql1, result) },
            ObjectClass.ACCOUNT
    )
}

private Object handleSyncTokenForAccount(Sql sql) {
    return handleSyncTokenGeneric(sql, Constants.SYNC_TOKEN_ACCOUNT, syncTokenRowTransformForAccount())
}

private Map buildParamsFromTokenForAccount(String token) {
    String[] array = token.split(";")

    return [
            timestamp   : Timestamp.valueOf(array[0]),
            id: array[1] as Integer
    ]
}
private Closure syncTokenRowTransformForAccount() {
    return { row ->
        [
                row.get("timestamp").toString(),
                row.get("id")?.toString()
        ]
    }
}

private ConnectorObject buildAccountObject(Sql sql, GroovyRowResult rowResult) {
    String query = Constants.QUERY_ACCOUNT + " where " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".id = :id"
    Map params = [id: rowResult.get("id")]

    log.info("Executing sync query to build account object {0} with params {1}", query, params)

    List<GroovyRowResult> rows = sql.rows(params, query, 0, 1);
    if (rows == null || rows.isEmpty()) {
        log.info("Couldn't find account for specified account identifier {}", params)
        return null
    }

    return SearchScript.buildAccount(sql, rows.get(0))
}


private void handleSyncGeneric(Sql sql, String token, SyncResultsHandler handler, String query, Function<String, Map> buildParamsFromToken,
                               Function<GroovyRowResult, List<String>> buildTokenFromRow,
                               BiFunction<Sql, GroovyRowResult, ConnectorObject> buildConnectorObject, ObjectClass oc) {
    if (token == null) {
        return
    }

    Map params = buildParamsFromToken.apply(token)

    int countProcessed = 0

    List<GroovyRowResult> results
    outer:
    while (true) {
        log.info("Executing handle sync generic query {0} with params {1}", query, params)

        sql.withTransaction {
            results = sql.rows(params, query, 1, Constants.SYNC_MAX_ROWS)
        }

        if (results == null || results.isEmpty()) {
            log.info("Nothing found in queue")
            break
        }

        log.info("Starting to process {0} records", results.size())

        for (int i = 0; i < results.size(); i++) {
            GroovyRowResult result = results.get(i)

            ConnectorObject object = null
            String newToken = null
            sql.withTransaction {
                object = buildConnectorObject.apply(sql, result)
                newToken = buildSyncToken(result, buildTokenFromRow)
            }

            if (object == null) {
                continue
            }

            SyncDelta delta = buildSyncDelta(SyncDeltaType.CREATE_OR_UPDATE, oc, newToken, object)

            log.info("Created sync delta for object {0} with token {1}, delta on TRACE level", object.getUid().getUidValue(), newToken)
            if (log.isOk()) {
                log.ok("Delta {0}", delta)
            }

            if (!handler.handle(delta)) {
                log.info("Handler paused processing")
                break outer
            }

            params = buildParamsFromToken.apply(newToken)

            countProcessed++
        }
    }

    log.info("Synchronization done, processed {0} events", countProcessed)
}

private Object handleSyncTokenGeneric(Sql sql, String query, Function<GroovyRowResult, List<String>> rowTransform) {
    String result = null

    sql.withTransaction {
        log.ok("Executing get sync token generic query {0}", query)

        GroovyRowResult row = sql.firstRow(query)
        if (row == null) {
            row = sql.firstRow("select now() as timestamp, 0 as id")
        }

        result = buildSyncToken(row, rowTransform)

        log.info("Created token: {0}", result)
    }

    return result
}

private String buildSyncToken(Map row, Function<GroovyRowResult, List<String>> rowTransform) {
    if (row == null) {
        return null
    }

    List<String> values = rowTransform.apply(row)
    if (values == null || values.isEmpty()) {
        return null
    }

    if (values.size() == 1) {
        result = values.get(0)
    }

    result = StringUtil.join(values, (char) ';')
}

private SyncDelta buildSyncDelta(SyncDeltaType type, ObjectClass oc, Object newToken, ConnectorObject obj) {
    SyncDeltaBuilder builder = new SyncDeltaBuilder()
    builder.setDeltaType(type)
    builder.setObjectClass(oc)

    if (newToken != null) {
        builder.setToken(new SyncToken(newToken))
    }

    builder.setObject(obj)
    builder.setUid(obj.getUid())

    return builder.build()
}
