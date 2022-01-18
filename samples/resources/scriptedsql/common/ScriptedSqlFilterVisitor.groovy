package common

import common.ColumnPrefixMapper
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.AttributeUtil
import org.identityconnectors.framework.common.objects.Name
import org.identityconnectors.framework.common.objects.Uid
import org.identityconnectors.framework.common.objects.filter.*

import java.sql.Date
import java.sql.Timestamp
import java.time.ZonedDateTime

/**
 * Created by Viliam Repan (lazyman).
 */
class ScriptedSqlFilterVisitor implements FilterVisitor<String, Void> {

    private static Log LOG = Log.getLog(ScriptedSqlFilterVisitor.class)

    private static final int DISCRIMINATOR_MAX_BUCKET = 10000;

    private Map<String, Object> params

    private String uidColumn
    private String nameColumn
    private ColumnPrefixMapper columnMapper
    private Class<?> uidType

    ScriptedSqlFilterVisitor(Map<String, Object> params, String uidColumn, String nameColumn, ColumnPrefixMapper columnMapper, Class<?> uidType) {
        this.params = params
        this.uidColumn = uidColumn
        this.nameColumn = nameColumn
        this.columnMapper = columnMapper
        this.uidType = uidType
    }

    @Override
    String visitOrFilter(Void v, OrFilter filter) {
        return visitFilter(filter, "or")
    }

    @Override
    String visitAndFilter(Void v, AndFilter filter) {
        return visitFilter(filter, "and")
    }

    @Override
    String visitNotFilter(Void v, NotFilter filter) {
        return "not (" + filter.getFilter().accept(this, null) + ")"
    }

    @Override
    String visitContainsFilter(Void v, ContainsFilter filter) {
        return visitFilter(filter, "CONTAINS")
    }

    @Override
    String visitEndsWithFilter(Void v, EndsWithFilter filter) {
        return visitFilter(filter, "ENDSWITH")
    }

    @Override
    String visitEqualsFilter(Void v, EqualsFilter filter) {
        return visitFilter(filter, "EQUALS")
    }

    @Override
    String visitGreaterThanFilter(Void v, GreaterThanFilter filter) {
        return visitFilter(filter, "GREATERTHAN")
    }

    @Override
    String visitGreaterThanOrEqualFilter(Void v, GreaterThanOrEqualFilter filter) {
        return visitFilter(filter, "GREATERTHANOREQUAL")
    }

    @Override
    String visitLessThanFilter(Void v, LessThanFilter filter) {
        return visitFilter(filter, "LESSTHAN")
    }

    @Override
    String visitLessThanOrEqualFilter(Void v, LessThanOrEqualFilter filter) {
        return visitFilter(filter, "LESSTHANOREQUAL")
    }

    @Override
    String visitStartsWithFilter(Void v, StartsWithFilter filter) {
        return visitFilter(filter, "STARTSWITH")
    }

    @Override
    String visitContainsAllValuesFilter(Void v, ContainsAllValuesFilter filter) {
        throw new UnsupportedOperationException("ContainsAllValuesFilter transformation is not supported")
    }

    @Override
    String visitExtendedFilter(Void v, Filter filter) {
        throw new UnsupportedOperationException("Filter type is not supported: " + filter.getClass())
    }

    @Override
    String visitEqualsIgnoreCaseFilter(Void v, EqualsIgnoreCaseFilter filter) {
        throw new UnsupportedOperationException("Filter type is not supported: " + filter.getClass())
    }

    private Map<String, Object> createMap(String operation, AttributeFilter filter) {
        Map<String, Object> map = new LinkedHashMap(4)
        String name = filter.getAttribute().getName()
        String value = AttributeUtil.getAsStringValue(filter.getAttribute())

        map.put("not", false)
        map.put("operation", operation)
        map.put("left", name)
        map.put("right", value)

        return map
    }

    protected String visitFilter(Filter filter, String type) {
        if (filter in AttributeFilter) {
            return visitAttributeFilter(filter, type)
        }

        return visitCompositeFilter(filter, type)
    }

    private String translateAttributeName(String original) {
        String left = original

        if (Uid.NAME == left) {
            left = uidColumn
        } else if (Name.NAME == left) {
            left = nameColumn
        } else if ("discriminator" == left) {
            // if we switched to varchar2 comparation (slower)
            // left = "cast (" + DISCRIMINATOR_MAX_BUCKET + " + ORA_HASH(" + translateAttributeName(uidColumn) + ", " + (DISCRIMINATOR_MAX_BUCKET - 1) + ") as varchar2(6))"

            left = "(" + DISCRIMINATOR_MAX_BUCKET + " + ORA_HASH(" + translateAttributeName(uidColumn) + ", " + (DISCRIMINATOR_MAX_BUCKET - 1) + "))"
        }

        left = left.toLowerCase()

        if (columnMapper == null) {
            return left
        }

        String prefix = ""
        if ("discriminator" != original) {
            if (columnMapper.prefixes.containsKey(left)) {
                prefix = columnMapper.prefixes[left]
            }

            if (prefix.isEmpty() && columnMapper.defaultPrefix != null) {
                prefix = columnMapper.defaultPrefix
            }
        }

        if (columnMapper.columns.containsKey(left)) {
            left = columnMapper.columns[left]
        }

        if (!prefix.isEmpty()) {
            left = prefix + "." + left
        }

        return left
    }

    private String visitAttributeFilter(AttributeFilter filter, String type) {
        Map<String, Object> query = createMap(type, filter)

        LOG.info("Visiting attribute filter, query {0}, uidcolumn {1}, nameColumn {2}", query, uidColumn, nameColumn)

        String columnName = query.get("left").toLowerCase()
        if (columnName.contains(".")) {
            columnName = columnName.replaceFirst("[\\w]+\\.", "")
        }

        Attribute attr = filter.getAttribute()

        String left = translateAttributeName(query.get("left"))

        Object right = query.get("right")
        if ("discriminator" == columnName) {
            right = rightPad((String) right, DISCRIMINATOR_MAX_BUCKET.toString().length(), (char) "0")
        } else if (this.uidType != null && "__uid__" == columnName) {
            right = right.asType(this.uidType)
        } else if (attr != null && attr.getValue() != null && attr.getValue().size() == 1) {
            Object val = attr.getValue().get(0)
            if (val instanceof ZonedDateTime) {
                java.util.Date date = Date.from(((ZonedDateTime) val).toInstant())
                right = new Timestamp(date.getTime())
            }
        }

        String operation = query.get("operation")
        boolean not = query.get("not")

        if (right == null && "EQUALS" == operation) {
            return left + (not ? " is not null" : " is null")
        }

        switch (operation) {
            case "CONTAINS":
                right = '%' + right + '%'
                break;
            case "ENDSWITH":
                right = '%' + right
                break;
            case "STARTSWITH":
                right = right + '%'
                break;
        }

        String paramName = columnName
        int i = 0
        while (params.containsKey(paramName)) {
            paramName = columnName + i
            i++
        }

        params.put(paramName, right)
        right = ":" + paramName

        String where
        switch (operation) {
            case "EQUALS":
                where = " " + left + (not ? " <> " : " = ") + right
                break
            case "CONTAINS":
            case "ENDSWITH":
            case "STARTSWITH":
                where = " " + left + (not ? " not like " : " like ") + right
                break
            case "GREATERTHAN":
                where = " " + left + (not ? " <= " : " > ") + right
                break
            case "GREATERTHANOREQUAL":
                where = " " + left + (not ? " < " : " >= ") + right
                break
            case "LESSTHAN":
                where = " " + left + (not ? " >= " : " < ") + right
                break
            case "LESSTHANOREQUAL":
                where = " " + left + (not ? " > " : " <= ") + right
                break
            default:
                where = ""
        }

        LOG.info("Filter translated to: {0}, with parameters {1}", where, params)

        return where
    }

    private String visitCompositeFilter(CompositeFilter filter, String type) {
        List<String> partial = []

        for (Filter f : filter.getFilters()) {
            partial.add("(" + f.accept(this, null) + ")")
        }

        StringBuilder where = new StringBuilder()
        where.append('(')
        where.append(join(partial, " " + type + " "))
        where.append(')')
    }

    private static String join(List list, String separator) {
        if (list == null) {
            return null
        }

        if (separator == null) {
            separator = ""
        }

        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i))
            if (i + 1 < list.size()) {
                sb.append(separator)
            }
        }

        return sb.toString()
    }

    private String rightPad(String str, int size, char padStr) {
        if (str == null) {
            return null;
        }

        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }

        StringBuilder sb = new StringBuilder()
        sb.append(str)
        for (int i = 0; i < pads; i++) {
            sb.append(padStr)
        }

        return sb.toString()
    }
}
