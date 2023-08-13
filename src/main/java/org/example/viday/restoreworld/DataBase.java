package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBase {
    public static HikariDataSource hds;

    public Connection con;

    public DataBase(HikariConfig config) {
        hds = new HikariDataSource(config);
        try {
            con = hds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet getBlocks(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_block WHERE rolled_back = 0 ORDER BY time DESC LIMIT 1000")) {
            try(ResultSet result = stmt.executeQuery()){
                System.out.println("test");
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getWorld(int id){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_world WHERE id = ?")) {
            stmt.setInt(1, id);
            try(ResultSet result = stmt.executeQuery()){
                return result.getString("world");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMaterial(int id){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_material_map WHERE id = ?")) {
            stmt.setInt(1, id);
            try(ResultSet result = stmt.executeQuery()){
                return result.getString("material");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getBlockData(int id){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_blockdata_map WHERE id = ?")) {
            stmt.setInt(1, id);
            try(ResultSet result = stmt.executeQuery()){
                return result.getString("data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}