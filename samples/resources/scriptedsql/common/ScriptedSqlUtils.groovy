package common

import common.ScriptedSqlFilterVisitor
import groovy.sql.Sql
import org.identityconnectors.common.IOUtil
import org.identityconnectors.common.StringUtil
import org.identityconnectors.common.logging.Log
import org.identityconnectors.common.security.GuardedString
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.AttributeBuilder
import org.identityconnectors.framework.common.objects.AttributeUtil
import org.identityconnectors.framework.common.objects.ConnectorObject
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.filter.Filter
import org.identityconnectors.framework.common.objects.filter.FilterVisitor

import java.sql.Clob
import java.sql.Connection
import java.sql.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Created by Viliam Repan (lazyman).
 */
class ScriptedSqlUtils {

    private static Log LOG = Log.getLog(ScriptedSqlUtils.class)

    static String buildWhereClause(Filter filter, Map sqlParams, String uidColumn, ColumnPrefixMapper mapper) {
        return buildWhereClause(filter, sqlParams, uidColumn, uidColumn, mapper)
    }

    static String buildWhereClause(Filter filter, Map sqlParams, String uidColumn, String nameColumn, ColumnPrefixMapper mapper, Class<?> uidType) {
        ScriptedSqlFilterVisitor visitor = new ScriptedSqlFilterVisitor(sqlParams, uidColumn, nameColumn, mapper, uidType)

        return buildWhereClause(filter, visitor)
    }

    static String buildWhereClause(Filter filter, FilterVisitor filterVisitor) {
        if (filter == null) {
            LOG.info("Returning empty where clause")
            return ""
        }

        return filter.accept(filterVisitor, null)
    }

    static void executeQuery(String sqlQuery, Map sqlParams, OperationOptions options, Closure closure, Closure handler, Sql sql) {
        String paging = buildPaging(options, sqlParams)

        if (!paging.isEmpty()) {
            sqlQuery = sqlQuery + " " + paging
        }

        LOG.info("Select query ''{0}'', params ''{1}''", sqlQuery, sqlParams)

        Closure filter = { row ->

            ConnectorObject object = closure.call(row)
            Set<Attribute> attributes = object.getAttributes()

            Set<Attribute> filtered = attributes.stream()
                    .filter({ a -> !isAttributeEmpty(a) }).collect(Collectors.toSet())

            return handler.call(new ConnectorObject(object.getObjectClass(), filtered))
        }

        if (!sqlParams.isEmpty()) {
            sql.eachRow(sqlParams, sqlQuery, filter)
        } else {
            sql.eachRow(sqlQuery, filter)
        }
    }

    static boolean isAttributeEmpty(Attribute attr) {
        return attr.getValue() == null || attr.getValue().isEmpty()
    }

    static String buildPaging(OperationOptions options, Map sqlParams) {
        if (options == null) {
            return ""
        }

        Integer pageSize = options.pageSize
        Integer pagedResultsOffset = options.pagedResultsOffset
        LOG.info("Page size {0}, offset {1}", pageSize, pagedResultsOffset)

        if (pageSize == null || pagedResultsOffset == null) {
            return ""
        }

        sqlParams.put("offset", pagedResultsOffset - 1)
        sqlParams.put("limit", pageSize)

        return "limit :limit offset :offset"
    }

    static void testConnection(Sql sql, List<String> tables) {
        Connection connection = sql.getConnection()

        LOG.info("Using driver: {0} version: {1}",
                connection.getMetaData().getDriverName(),
                connection.getMetaData().getDriverVersion())

        List<String> failedCheckTables = []

        Exception lastException = null
        for (String table : tables) {
            try {
                sql.eachRow("SELECT 1 FROM " + table, {})
            } catch (Exception ex) {
                lastException = ex
                failedCheckTables.add(table)
            }
        }

        if (failedCheckTables.isEmpty()) {
            return
        }

        StringBuilder msg = new StringBuilder()
        msg.append("Failed test connection for tables (").append(failedCheckTables.size()).append("): ")
        msg.append(StringUtil.join(failedCheckTables, (char) ','))
        msg.append(". Last exception error: ")
        msg.append(lastException != null ? lastException.getMessage() : null)

        throw new ConnectorException(msg.toString(), lastException)
    }

    static String clobToString(Clob clob) {
        if (clob == null) {
            return clob
        }

        Reader reader = null
        try {
            reader = clob.getCharacterStream()
            return IOUtil.readerToString(reader)
        } finally {
            IOUtil.quietClose(reader)
        }
    }

    static final Long numberToLong(Number n) {
        return n?.longValue()
    }

    static Long dateToLong(Date date) {
        return date?.getTime()
    }

    static ZonedDateTime dateToZonedDate(Date date) {
        if (date == null) {
            return null
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"))
    }

    static GuardedString stringToGuarded(String value) {
        if (value == null) {
            return null
        }

        return new GuardedString(value.chars)
    }

    static Date zonedDateToDate(ZonedDateTime date) {
        if (date == null) {
            return null
        }
        return Date.from(date.toInstant())
    }

    static Boolean numberToBoolean(Number n) {
        if (n == null) {
            return null
        }

        return 0 != n.intValue()
    }

    static Boolean stringToBoolean(String value) {
        if (value == null) {
            return null
        }

        return "y".equalsIgnoreCase(value.trim()) || "true".equalsIgnoreCase(value.trim())
    }

    static Long getLong(Set attributes, String name) {
        Attribute attr = AttributeUtil.find(name, attributes)
        return attr != null ? AttributeUtil.getLongValue(attr) : null
    }

    static String getString(Set attributes, String name) {
        Attribute attr = AttributeUtil.find(name, attributes)
        return attr != null ? AttributeUtil.getStringValue(attr) : null
    }

    static Boolean getBoolean(Set attributes, String name) {
        Attribute attr = AttributeUtil.find(name, attributes)
        return attr != null ? AttributeUtil.getBooleanValue(attr) : null
    }

    static Date getDate(Set attributes, String name) {
        Attribute attr = AttributeUtil.find(name, attributes)
        if (attr == null || AttributeUtil.getSingleValue(attr) == null) {
            return null
        }

        ZonedDateTime date = (ZonedDateTime) AttributeUtil.getSingleValue(attr)

        Date d = Date.from(date.toInstant())
        return new Timestamp(d.getTime())
    }

    static void buildAndExecuteInsertQueryList(Sql sql, String table, List<Map<String, Object>> params) {
        if (params == null) {
            return
        }

        for (Map<String, Object> param : params) {
            buildAndExecuteInsertQuery(sql, table, param)
        }
    }

    static List<List<Object>> buildAndExecuteInsertQuery(Sql sql, String table, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return
        }

        List<String> columns = []
        columns.addAll(params.keySet())

        Collections.sort(columns)

        String query = "insert into " + table + " ("
        query += StringUtil.join(columns, (char) ',')
        query += ") values ("

        List<String> keys = columns.stream().map(new Function<String, String>() {

            @Override
            String apply(String t) {
                return ":" + t
            }
        }).collect Collectors.toList()
        query += StringUtil.join(keys, (char) ',')
        query += ")"

        LOG.info("Insert query {0} with params {1}", query, params)

        def ret = sql.executeInsert(params, query)

        LOG.info("Insert query return {0}, {1}", ret, String.valueOf(ret[0][0]))
        return ret
    }

    static Query buildUpdateQuery(String table, Map<String, Object> params, Map<String, Object> where) {
        if (params == null || params.isEmpty()) {
            return null
        }

        List<String> columns = []
        columns.addAll(params.keySet())

        Collections.sort(columns)

        String query = "update " + table + " set "
        List<String> values = prepareSqlNamedParameters(params, "", false)
        query += StringUtil.join(values, (char) ',')
        query += " where "
        List<String> whereValues = prepareSqlNamedParameters(where, "w", true)
        query += join(whereValues, " and ")

        Map fullParams = [:]
        fullParams.putAll(params)
        where.forEach({ k, v -> fullParams.put("w" + k, v) })

        return new Query(query, fullParams)
    }

    static void buildAndExecuteUpdateQuery(Sql sql, String table, Map<String, Object> params, Map<String, Object> where) {
        Query query = buildUpdateQuery(table, params, where)
        if (query == null) {
            return
        }

        LOG.info("Update query {0} with params {1}", query.query, query.params)

        int changedRows = sql.executeUpdate(query.params, query.query)

        LOG.ok("Updated {0} rows", changedRows)
    }

    /**
     * @param where whether it's where clause, we know if it's for "where" clause -&gt; handle column=null differently -&gt; column is null
     * @return
     */
    static List<String> prepareSqlNamedParameters(Map<String, Object> params, String paramPrefix, boolean where) {
        if (paramPrefix == null) {
            paramPrefix = ""
        }

        List<String> columns = []
        for (Map.Entry<String, Object> entry : params) {
            if (entry.getValue() == null && where) {
                columns.add(entry.getKey() + " is null")
                continue
            }

            columns.add(entry.getKey() + "=:" + paramPrefix + entry.getKey())
        }

        Collections.sort(columns)

        return columns
    }

    static int buildAndExecuteDeleteQuery(Sql sql, String table, Map<String, Object> where) {
        String query = "delete from " + table + " where "

        List<String> params = prepareSqlNamedParameters(where, "", true)
        query += join(params, " and ")

        LOG.info("Delete query {0} with params {1}", query, where)

        return sql.executeUpdate(where, query)
    }

    static String join(List<String> list, String separator) {
        StringBuilder sb = new StringBuilder()

        for (int i = 0; i < list.size(); i++) {
            String param = list.get(i)
            sb.append(param)
            if (i + 1 < list.size()) {
                sb.append(separator)
            }
        }

        return sb
    }

    static void addAttribute(List<Attribute> attributes, String name, Object value) {
        if (attributes == null || value == null) {
            return
        }

        attributes.add(AttributeBuilder.build(name, value))
    }

    static class Query {

        String query
        Map<String, Object> params

        Query(String query) {
            this.query = query
        }

        Query(String query, Map<String, Object> params) {
            this.query = query
            this.params = params
        }
    }
}
