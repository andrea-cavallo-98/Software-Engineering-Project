package it.polito.ezshop.controllers;

import java.sql.*;

public class DB {
    private static String jdbcUrl = "jdbc:sqlite:ezshopdb.db";
    private static Connection connection;

    public static Connection getConnectionToDB() throws SQLException {
        if(connection == null) {
            connection = DriverManager.getConnection(jdbcUrl);
        }
        return connection;
    }
    public static void alterJDBCUrl() throws SQLException {
        if(connection != null) {
            connection.close();
            connection = null;
        }
        jdbcUrl = "wrong_url";
    }

    public static void restoreJDBCUrl() {
        jdbcUrl = "jdbc:sqlite:ezshopdb.db";
    }


    public static void cleanDatabase() throws SQLException {
        Connection connection = DB.getConnectionToDB();
        ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
        while(resultSet.next()) {
            String sql = "DELETE FROM " + resultSet.getString("TABLE_NAME");
            Statement statement = connection.createStatement();
            statement.execute(sql);
        }
    }
}
