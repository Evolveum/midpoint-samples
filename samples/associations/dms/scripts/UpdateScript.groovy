import common.ScriptedSqlUtils
import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.*

import static common.ScriptedSqlUtils.*

import java.sql.Connection
import java.sql.SQLIntegrityConstraintViolationException

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

switch (objectClass.objectClassValue) {
    case 'user':
        return handleUser(sql)
    case 'documentStore':
        return handleDocumentStore(sql)
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

Uid handleUser(Sql sql) {

    def operation = operation as OperationType

    sql.withTransaction {
        Map<String, Object> params = [:]

        List skipAttributes = [Uid.NAME, 'access']

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

        log.info('operation = {0}', operation)
        if (operation == OperationType.ADD_ATTRIBUTE_VALUES) {
            List valuesToAdd = AttributeUtil.find('access', attributes)?.getValue()
            if (valuesToAdd != null && valuesToAdd.size() > 0) {
                for (ConnectorObjectReference valueToAdd : (valuesToAdd as List<ConnectorObjectReference>)) {
                    def attributes = valueToAdd.referencedValue.attributes
                    log.info('access to add: {0}', attributes)
                    insertAccess(sql, uid.getUidValue() as Integer, attributes)
                }
            }
        } else if (operation == OperationType.REMOVE_ATTRIBUTE_VALUES) {
            List valuesToRemove = AttributeUtil.find('access', attributes)?.getValue()
            if (valuesToRemove != null && valuesToRemove.size() > 0) {
                for (ConnectorObjectReference valueToRemove : (valuesToRemove as List<ConnectorObjectReference>)) {
                    def attributes = valueToRemove.referencedValue.attributes
                    log.info('access to remove: {0}', attributes)
                    deleteAccess(sql, uid.getUidValue() as Integer, attributes)
                }
            }
        }

        ScriptedSqlUtils.buildAndExecuteUpdateQuery(sql, BaseScript.TABLE_USER, params, [id: uid.getUidValue() as Integer])

    }

    return new Uid(uid.getUidValue())
}

Uid handleDocumentStore(Sql sql) {

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

        ScriptedSqlUtils.buildAndExecuteUpdateQuery(sql, BaseScript.TABLE_DOCUMENTS, params, [id: uid.getUidValue() as Integer])

    }

    return new Uid(uid.getUidValue())
}

static void insertAccess(Sql sql, Integer userId, Set attributes) {

    Map<String, Object> params = [
            user_id             : userId,
            document_store_id   : getDocumentStoreId(attributes),
            level               : getString(attributes, 'level')
    ]

    sql.withTransaction {
        ScriptedSqlUtils.buildAndExecuteInsertQuery(sql, BaseScript.TABLE_ACCESSES, params)
    }
}

void deleteAccess(Sql sql, Integer userId, Set attributes) {

    Map<String, Object> params = [
            user_id             : userId,
            document_store_id   : getDocumentStoreId(attributes),
            level               : getString(attributes, 'level')
    ]

    sql.withTransaction {
        ScriptedSqlUtils.buildAndExecuteDeleteQuery(sql, BaseScript.TABLE_ACCESSES, params)
    }
}

static Integer getDocumentStoreId(Set attributes) {
    Attribute attr = AttributeUtil.find('documentStore', attributes)
    if (attr == null) {
        throw new IllegalArgumentException('No document store in ' + attributes)
    }
    def attrValue = attr.getValue().get(0)
    Set<Attribute> storeAttrs = (attrValue as ConnectorObjectReference).referencedValue.attributes
    return AttributeUtil.getUidAttribute(storeAttrs).getUidValue() as Integer
}
