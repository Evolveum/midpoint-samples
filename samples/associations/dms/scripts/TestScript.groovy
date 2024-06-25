import common.ScriptedSqlUtils
import groovy.sql.Sql
import org.identityconnectors.common.logging.Log

import java.sql.Connection

def log = log as Log
def connection = connection as Connection

log.info("Entering " + operation + " Script")

List<String> TABLES = [
        BaseScript.TABLE_USERS
]

def sql = new Sql(connection)

sql.withTransaction {
    ScriptedSqlUtils.testConnection(sql, TABLES)
}