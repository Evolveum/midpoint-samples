import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.ConnectorObjectReference
import org.identityconnectors.framework.common.objects.OperationOptionInfoBuilder
import org.identityconnectors.framework.spi.operations.SearchOp

import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.MULTIVALUED
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_CREATABLE
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_READABLE
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_UPDATEABLE

def log = log as Log
def operation = operation as OperationType
def builder = builder as ICFObjectBuilder
def configuration = configuration as ScriptedSQLConfiguration

log.info("Entering " + operation + " Script")

builder.schema({

    objectClass {
        type 'user'
        attributes {
            fullname()
            email()
            access ConnectorObjectReference, 'userAccess#1', MULTIVALUED
        }
    }

    objectClass {
        type 'documentStore'
        attributes {
            description()
            access ConnectorObjectReference, 'accessDocumentStore#2', NOT_READABLE, NOT_UPDATEABLE, NOT_CREATABLE, NOT_RETURNED_BY_DEFAULT, MULTIVALUED
        }
    }

    objectClass {
        type 'access'
        associated()
        attributes {
            level()
            //noinspection GroovyAssignabilityCheck
            user ConnectorObjectReference, 'userAccess#2', NOT_READABLE, NOT_UPDATEABLE, NOT_CREATABLE, NOT_RETURNED_BY_DEFAULT
            documentStore ConnectorObjectReference, 'accessDocumentStore#1'
        }
    }

    defineOperationOption OperationOptionInfoBuilder.buildPagedResultsOffset(), SearchOp
    defineOperationOption OperationOptionInfoBuilder.buildPageSize(), SearchOp
})