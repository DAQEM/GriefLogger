package com.daqem.grieflogger.database.dialect;

public class MySQLDialect implements IDatabaseDialect {

    @Override
    public String getInsertIgnore() {
        return "INSERT IGNORE";
    }

    @Override
    public String getOnConflictUpdate(String key, String update) {
        return "ON DUPLICATE KEY UPDATE " + update;
    }

    @Override
    public String getOnConflictDoNothing(String key) {
        return "ON DUPLICATE KEY UPDATE " + key + " = " + key;
    }

    @Override
    public String getDataType(String type) {
        return switch (type) {
            case "integer" -> "int";
            case "bigint" -> "bigint";
            case "text" -> "text";
            case "varchar" -> "varchar";
            default -> type;
        };
    }
}
