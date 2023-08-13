package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataBase {
    public static HikariDataSource hds;
    private final RestoreWorld restoreWorld;

    public Connection con;

    public DataBase(RestoreWorld restoreWorld) {
        this.restoreWorld = restoreWorld;
    }

    public void connect() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        config.setJdbcUrl("jdbc:sqlite:"+ restoreWorld.getDataFolder() + "/database.db");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hds = new HikariDataSource(config);

        con = hds.getConnection();
    }
}