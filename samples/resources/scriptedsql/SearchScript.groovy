import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.ConnectorObject
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.ResultsHandler
import org.identityconnectors.framework.common.objects.SearchResult
import org.identityconnectors.framework.common.objects.filter.Filter

import java.sql.Connection

import static common.ScriptedSqlUtils.*



def log = log as Log
def operation = operation as OperationType
def options = options as OperationOptions
def objectClass = objectClass as ObjectClass
def configuration = configuration as ScriptedSQLConfiguration
def filter = filter as Filter
def connection = connection as Connection
def query = query as Closure
def handler = handler as ResultsHandler

log.info("Entering " + operation + " Script")

def sql = new Sql(connection)

switch (objectClass) {
    case ObjectClass.ACCOUNT:
        handleAccount(sql)
        break
    case BaseScript.GROUP:
        handleGroup(sql)
        break
    /*
    case BaseScript.ORGANIZATION:
        handleOrganization(sql)
        break
     */
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

return new SearchResult()

// =================================================================================

static ConnectorObject buildAccount(Sql sql, GroovyObject row) {

    return ICFObjectBuilder.co {
        objectClass ObjectClass.ACCOUNT

        uid row.id as String
        id row.login

        attribute '__ENABLE__', !row.disabled
        attribute 'fullname', row.fullname
        attribute 'firstname', row.firstname
        attribute 'lastname', row.lastname
        attribute 'email', row.email
        attribute 'organization', row.organization
    }

}

void handleAccount(Sql sql) {
    Closure closure = { row ->
        buildAccount(sql, row)
    }

    String sqlQuery = "select " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".* from " + BaseScript.TABLE_USER + " " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix

    Map params = [:]

    String where = buildWhereClause(filter, params, "id", 'login', Constants.PREFIX_MAPPER_ACCOUNT, Constants.UID_TYPE_ACCOUNT)
    if (!where.isEmpty()) {
        sqlQuery += " where " + where
    }

    sql.withTransaction {
        executeQuery(sqlQuery, params, options, closure, handler, sql)
    }
}


void handleGroup(Sql sql) {
    Closure closure = { row ->
        ICFObjectBuilder.co {
            uid row.id as String
            id row.name
            attribute 'description', row.description
        }
    }

    String sqlQuery = "select " + Constants.PREFIX_MAPPER_GROUP.defaultPrefix + ".* from " + BaseScript.TABLE_GROUPS + " " + Constants.PREFIX_MAPPER_GROUP.defaultPrefix

    Map params = [:]

    String where = buildWhereClause(filter, params, "id", "name", Constants.PREFIX_MAPPER_GROUP, Constants.UID_TYPE_GROUP)
    if (!where.isEmpty()) {
        sqlQuery += " where " + where
    }

    sql.withTransaction {
        executeQuery(sqlQuery, params, options, closure, handler, sql)
    }
}
