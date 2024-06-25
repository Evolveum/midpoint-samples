import groovy.sql.Sql
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.Uid

import java.sql.SQLIntegrityConstraintViolationException

class BaseScript extends Script {

    public static final String GROUP_NAME = "Group"

    public static final ObjectClass GROUP = new ObjectClass(BaseScript.GROUP_NAME)

    public static final String ORGANIZATION_NAME = "Organization"

    public static final ObjectClass ORGANIZATION = new ObjectClass(BaseScript.ORGANIZATION_NAME)

    public static final String TABLE_USER = "Users"
    public static final String TABLE_GROUPS = "Groups"

    public static final String TABLE_USERS = 'users'
    public static final String TABLE_DOCUMENTS = 'document_stores'
    public static final String TABLE_DOCUMENT_STORES = 'document_stores'
    public static final String TABLE_ACCESSES = 'accesses'

    @Override
    Object run() {
        return null
    }
}