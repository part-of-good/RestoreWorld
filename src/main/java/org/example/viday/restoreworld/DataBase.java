package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        hds = new HikariDataSource(config);
        con = hds.getConnection();
    }

    ResultSet getBlocks(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_blocks WHERE rolled_back = 0 ORDER BY time DESC;")) {
            try(ResultSet result = stmt.executeQuery()){
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}