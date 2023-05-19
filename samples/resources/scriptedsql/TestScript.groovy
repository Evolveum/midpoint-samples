import common.ScriptedSqlUtils
import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log

import java.sql.Connection

def log = log as Log
def operation = operation as OperationType
def connection = connection as Connection
def configuration = configuration as ScriptedSQLConfiguration

log.info("Entering " + operation + " Script")

List<String> TABLES = [
        BaseScript.TABLE_USER,
        BaseScript.TABLE_GROUPS
]

def sql = new Sql(connection)

sql.withTransaction {
    ScriptedSqlUtils.testConnection(sql, TABLES)
}