import common.ScriptedSqlUtils

import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.*

import java.sql.Connection
import java.sql.SQLIntegrityConstraintViolationException

import static common.ScriptedSqlUtils.*

def log = log as Log
def operation = operation as OperationType
def options = options as OperationOptions
def objectClass = objectClass as ObjectClass
def attributes = attributes as Set<Attribute>
def connection = connection as Connection
def id = id as String
def configuration = configuration as ScriptedSQLConfiguration

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

Uid handleUser(Sql sql) {

    Map<String, Object> params = [
            login               : id,
            fullname            : getString(attributes, "fullname"),
            email               : getString(attributes, "email")
    ]

    def uid = null
    sql.withTransaction {
        try {
            def ret = ScriptedSqlUtils.buildAndExecuteInsertQuery(sql, BaseScript.TABLE_USERS, params)
            uid = new Uid(String.valueOf(ret[0][0]))
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new AlreadyExistsException("Object with id " + id + " already exists", ex)
        }
    }

    List valuesToAdd = AttributeUtil.find('access', attributes)?.getValue()
    if (valuesToAdd != null && valuesToAdd.size() > 0) {
        for (ConnectorObjectReference valueToAdd : (valuesToAdd as List<ConnectorObjectReference>)) {
            def attributes = valueToAdd.referencedValue.attributes
            log.info('access to add: {0}', attributes)
            UpdateScript.insertAccess(sql, uid.getUidValue() as Integer, attributes)
        }
    }

    return uid  //return __UID__ of the created user
}

Uid handleDocumentStore(Sql sql) {

    Map<String, Object> params = [
            name                : id,
            description         : getString(attributes, "description")
    ]

    def uid = null
    sql.withTransaction {
        try {
            def ret = ScriptedSqlUtils.buildAndExecuteInsertQuery(sql, BaseScript.TABLE_DOCUMENT_STORES, params)
            uid = new Uid(String.valueOf(ret[0][0]))
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new AlreadyExistsException("Object with id " + id + " already exists", ex)
        }
    }
    return uid  //return __UID__ of the created group
}
