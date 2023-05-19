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

switch (objectClass) {
    case ObjectClass.ACCOUNT:
        return handleAccount(sql)
    case BaseScript.GROUP:
        return handleGroup(sql)
    /*
    case BaseScript.ORGANIZATION:
        return handleOrganization(sql)
     */
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

Uid handleAccount(Sql sql) {

    Map<String, Object> params = [
            login               : id,
            firstname           : getString(attributes, "firstname"),
            lastname            : getString(attributes, "lastname"),
            fullname            : getString(attributes, "fullname"),
            email               : getString(attributes, "email"),
            organization        : getString(attributes, "organization"),
            password            : getString(attributes, "password"),
            disabled            : getBoolean(attributes, "__ENABLE__")
    ]

    def uid = null
    sql.withTransaction {
        try {
            def ret = ScriptedSqlUtils.buildAndExecuteInsertQuery(sql, BaseScript.TABLE_USER, params)
            uid = new Uid(String.valueOf(ret[0][0]))
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new AlreadyExistsException("Object with id " + id + " already exists", ex)
        }
    }
    return uid  //return __UID__ of the created user
}

Uid handleGroup(Sql sql) {

    Map<String, Object> params = [
            name                : id,
            description         : getString(attributes, "description")
    ]

    def uid = null
    sql.withTransaction {
        try {
            def ret = ScriptedSqlUtils.buildAndExecuteInsertQuery(sql, BaseScript.TABLE_GROUPS, params)
            uid = new Uid(String.valueOf(ret[0][0]))
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new AlreadyExistsException("Object with id " + id + " already exists", ex)
        }
    }
    return uid  //return __UID__ of the created group
}