import common.ColumnPrefixMapper

class Constants {

    public static final ColumnPrefixMapper PREFIX_MAPPER_ACCOUNT = new ColumnPrefixMapper("a")

    public static final ColumnPrefixMapper PREFIX_MAPPER_GROUP = new ColumnPrefixMapper("g")

    public static final Class<?> UID_TYPE_ACCOUNT = Integer
    public static final Class<?> UID_TYPE_GROUP = Integer

    public static final int SYNC_MAX_ROWS = 5000

    public static final String QUERY_ACCOUNT = "select " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix + ".* from " + BaseScript.TABLE_USER + " " + Constants.PREFIX_MAPPER_ACCOUNT.defaultPrefix
}
