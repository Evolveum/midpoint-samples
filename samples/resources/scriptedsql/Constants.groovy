import common.ColumnPrefixMapper

class Constants {

    public static final ColumnPrefixMapper PREFIX_MAPPER_ACCOUNT = new ColumnPrefixMapper("a")

    public static final ColumnPrefixMapper PREFIX_MAPPER_GROUP = new ColumnPrefixMapper("g")

    public static final Class<?> UID_TYPE_ACCOUNT = Integer
    public static final Class<?> UID_TYPE_GROUP = Integer

    public static final int SYNC_MAX_ROWS = 5000

    public static final String QUERY_ACCOUNT = "select " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".* from " + BaseScript.TABLE_USER + " " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix

    public static final String SYNC_ACCOUNT = "" +
            "SELECT " +
                PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".timestamp, " +
                PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".id " +
            "FROM " + BaseScript.TABLE_USER + " " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + " " +
            "WHERE " +
            "    (" + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".timestamp = :timestamp AND " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".id > :id)  OR " +
            "    (" + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".timestamp > :timestamp) " +
            "ORDER by " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".timestamp, " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".id"

    public static final String SYNC_TOKEN_ACCOUNT = "" +
            "SELECT " +
                PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".timestamp, " +
                PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".id " +
            "FROM " + BaseScript.TABLE_USER + " " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + " " +
            "ORDER by " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".timestamp DESC, " + PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".id DESC " +
            "LIMIT 1"
}
