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
def attributes = attributes as Set<Attribute>
def uid = uid as Uid
def id = id as String
def configuration = configuration as ScriptedSQLConfiguration
def connection = connection as Connection
def schema = schema as Schema

log.info("Entering " + operation + " Script")

def sql = new Sql(connection)

switch (objectClass) {
    case ObjectClass.ACCOUNT:
        return handleAccount(sql)
    case BaseScript.GROUP:
        return handleGroup(sql)
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

Uid handleAccount(Sql sql) {

    sql.withTransaction {
        Map<String, Object> params = [:]

        List skipAttributes = [Uid.NAME]

        for (Attribute attribute : attributes) {

            if (skipAttributes.contains(attribute.getName())) {
                continue
            }

            Object value
            if (attribute.getValue() != null && attribute.getValue().size() > 1) {
                value = attribute.getValue()
            } else {
                value = AttributeUtil.getSingleValue(attribute)
            }

            params.put(attribute.getName(), value)
        }

        ScriptedSqlUtils.buildAndExecuteUpdateQuery(sql, BaseScript.TABLE_USER, params, [id: uid.getUidValue() as Integer])

    }

    return new Uid(uid.getUidValue())
}

Uid handleGroup(Sql sql) {

    sql.withTransaction {
        Map<String, Object> params = [:]

        for (Attribute attribute : attributes) {

            Object value
            if (attribute.getValue() != null && attribute.getValue().size() > 1) {
                value = attribute.getValue()
            } else {
                value = AttributeUtil.getSingleValue(attribute)
            }

            params.put(attribute.getName(), value)
        }

        ScriptedSqlUtils.buildAndExecuteUpdateQuery(sql, BaseScript.TABLE_GROUPS, params, [id: uid.getUidValue() as Integer])

    }

    return new Uid(uid.getUidValue())
}