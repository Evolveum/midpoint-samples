import common.ScriptedSqlUtils
import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.*

import java.sql.Connection

def log = log as Log
def operation = operation as OperationType
def options = options as OperationOptions
def objectClass = objectClass as ObjectClass
def uid = uid as Uid
def configuration = configuration as ScriptedSQLConfiguration
def connection = connection as Connection
def schema = schema as Schema

log.info("Entering " + operation + " Script")

def sql = new Sql(connection)

switch (objectClass.objectClassValue) {
    case 'user':
        return handleUser(sql)
    case 'documentStore':
        return handleDocumentStore(sql)
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

void handleUser(Sql sql) {
    sql.withTransaction {
        ScriptedSqlUtils.buildAndExecuteDeleteQuery(sql, BaseScript.TABLE_USERS, [id: uid.getUidValue() as Integer])
    }
}

void handleDocumentStore(Sql sql) {
    sql.withTransaction {
        ScriptedSqlUtils.buildAndExecuteDeleteQuery(sql, BaseScript.TABLE_DOCUMENT_STORES, [id: uid.getUidValue() as Integer])
    }
}
