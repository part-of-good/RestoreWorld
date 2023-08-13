package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

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

    public void updateBlocks(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_block ORDER BY time DESC")) {
            try(ResultSet result = stmt.executeQuery()){
                System.out.println("test");
                while (result.next()){
                    System.out.println("test1");
                    Location loc = new Location(Bukkit.getWorld(RestoreWorld.getInstance().dataBase.getWorld(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                    if (RestoreWorld.getInstance().store.checkExists(loc)) continue;
                    Block block = loc.getBlock();
                    String meta = "";
                    if (result.getString("blockdata") != null){
                        result.getString("blockdata").split(",");
                        String[] dataInt = result.getString("blockdata").split(",");
                        String[] data = new String[dataInt.length];
                        for (int i = 0; i < dataInt.length; i++){
                            data[i] =  RestoreWorld.getInstance().dataBase.getBlockData(Integer.parseInt(dataInt[i]));
                        }
                        meta = String.join(",",data);
                    }
                    if (result.getString("action").equalsIgnoreCase("1")) block.setBlockData(RestoreWorld.getInstance().getServer().createBlockData(RestoreWorld.getInstance().dataBase.getMaterial(result.getInt("type"))+"["+meta+"]"));
                    else if (result.getString("action").equalsIgnoreCase("0")) {
                        block.setType(Material.AIR);
                    }
                    RestoreWorld.getInstance().store.addLocation(loc);
                    RestoreWorld.getInstance().getServer().broadcastMessage(block.getType().toString() + " | " + block.getX() + " " + block.getY() + " " + block.getZ());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish");
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