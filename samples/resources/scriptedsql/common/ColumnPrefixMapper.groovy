package common

/**
 * Created by Viliam Repan (lazyman).
 */
class ColumnPrefixMapper {

    // <connector_column, db_column>
    Map<String, String> columns

    // <db_column, prefix>
    Map<String, String> prefixes

    String defaultPrefix

    ColumnPrefixMapper(String defaultPrefix) {
        this(defaultPrefix, [:])
    }

    ColumnPrefixMapper(String defaultPrefix, Map<String, String> columns) {
        this(defaultPrefix, columns, [:])
    }

    ColumnPrefixMapper(String defaultPrefix, Map<String, String> columns, Map<String, String> prefixes) {
        this.columns = columns
        this.prefixes = prefixes
        this.defaultPrefix = defaultPrefix
    }
}
