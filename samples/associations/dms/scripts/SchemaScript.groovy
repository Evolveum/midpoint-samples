import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.ConnectorObjectReference
import org.identityconnectors.framework.common.objects.OperationOptionInfoBuilder
import org.identityconnectors.framework.spi.operations.SearchOp

import static org.identityconnectors.framework.common.objects.AttributeInfo.*
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.MULTIVALUED

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
            access ConnectorObjectReference, '->access', MULTIVALUED, RoleInReference.SUBJECT
        }
    }

    objectClass {
        type 'documentStore'
        attributes {
            description()
        }
    }

    objectClass {
        type 'access'
        embedded()
        attributes {
            level()
            documentStore ConnectorObjectReference, '->documentStore', RoleInReference.SUBJECT
        }
    }

    defineOperationOption OperationOptionInfoBuilder.buildPagedResultsOffset(), SearchOp
    defineOperationOption OperationOptionInfoBuilder.buildPageSize(), SearchOp
})