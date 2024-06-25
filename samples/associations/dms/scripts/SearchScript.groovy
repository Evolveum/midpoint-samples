import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.ConnectorObject
import org.identityconnectors.framework.common.objects.ConnectorObjectReference
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

switch (objectClass.objectClassValue) {
    case 'user':
        handleUser(sql)
        break
    case 'documentStore':
        handleDocumentStore(sql)
        break
    case 'access':
        handleAccess(sql)
        break
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

return new SearchResult()

// =================================================================================

void handleUser(Sql sql) {
    String sqlQuery = "select " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".* from " + BaseScript.TABLE_USERS + " " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix

    Map params = [:]

    String where = buildWhereClause(filter, params, "id", 'login', Constants.PREFIX_MAPPER_ACCOUNT, Constants.UID_TYPE_ACCOUNT)
    if (!where.isEmpty()) {
        sqlQuery += " where " + where
    }

    sql.withTransaction {
        executeQuery(sqlQuery, params, options, { row -> buildUser(sql, row) }, handler, sql)
    }
}

static ConnectorObject buildUser(Sql sql, GroovyObject row) {

    def userId = row.id as Integer

    def accessValues = []
    sql.eachRow('select a.id, a.document_store_id, ds.name as document_store_name, a.level from accesses a join document_stores ds on a.document_store_id = ds.id where user_id = :userId', [userId: userId]) {
        accessRow ->
            accessValues.add(
                    new ConnectorObjectReference(ICFObjectBuilder.co {
                        objectClass 'access'
                        uid accessRow.id as String
                        id accessRow.id as String
                        attribute 'level', accessRow.level
                        attribute 'documentStore', [new ConnectorObjectReference(
                                ICFObjectBuilder.co {
                                    objectClass 'documentStore'
                                    uid accessRow.document_store_id as String
                                    id accessRow.document_store_name as String
                                })]
                    }))
    }

    return ICFObjectBuilder.co {

        objectClass 'user'

        uid row.id as String
        id row.login

        attribute 'fullname', row.fullname
        attribute 'email', row.email
        attribute 'access', accessValues
    }

}


void handleDocumentStore(Sql sql) {
    Closure closure = { row ->
        ICFObjectBuilder.co {
            uid row.id as String
            id row.name
            attribute 'description', row.description
        }
    }

    String sqlQuery = "select " + Constants.PREFIX_MAPPER_GROUP.defaultPrefix + ".* from " + BaseScript.TABLE_DOCUMENTS + " " + Constants.PREFIX_MAPPER_GROUP.defaultPrefix

    Map params = [:]

    String where = buildWhereClause(filter, params, "id", "name", Constants.PREFIX_MAPPER_GROUP, Constants.UID_TYPE_GROUP)
    if (!where.isEmpty()) {
        sqlQuery += " where " + where
    }

    sql.withTransaction {
        executeQuery(sqlQuery, params, options, closure, handler, sql)
    }
}

void handleAccess(Sql sql) {
    Closure closure = { row ->
        ICFObjectBuilder.co {
            uid row.id as String
            id row.id as String
        }
    }

    String sqlQuery = "select " + Constants.PREFIX_MAPPER_GROUP.defaultPrefix + ".* from " + BaseScript.TABLE_ACCESSES + " " + Constants.PREFIX_MAPPER_GROUP.defaultPrefix

    Map params = [:]

    String where = buildWhereClause(filter, params, "id", "name", Constants.PREFIX_MAPPER_GROUP, Constants.UID_TYPE_GROUP)
    if (!where.isEmpty()) {
        sqlQuery += " where " + where
    }

    sql.withTransaction {
        executeQuery(sqlQuery, params, options, closure, handler, sql)
    }
}
