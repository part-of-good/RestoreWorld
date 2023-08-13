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
import java.util.HashMap;

public class DataBase {
    public static HikariDataSource hds;

    public Connection con;

    private final HashMap<Integer, String> worlds = new HashMap<>();
    private final HashMap<Integer, String> materials = new HashMap<>();
    private final HashMap<Integer, String> metadata = new HashMap<>();

    public DataBase(HikariConfig config) {
        hds = new HikariDataSource(config);
        try {
            con = hds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateBlocks(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_block WHERE rolled_back = 0 ORDER BY time DESC")) {
            try(ResultSet result = stmt.executeQuery()){
                while (result.next()){
                    Location loc = new Location(Bukkit.getWorld(worlds.get(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                    if (RestoreWorld.getInstance().store.checkExists(loc)) continue;
                    System.out.println("test1");
                    Block block = loc.getBlock();
                    String meta;
                    if (result.getString("blockdata") != null){
                        result.getString("blockdata").split(",");
                        String[] dataInt = result.getString("blockdata").split(",");
                        String[] data = new String[dataInt.length];
                        for (int i = 0; i < dataInt.length; i++){
                            data[i] =  metadata.get(Integer.parseInt(dataInt[i]));
                        }
                        meta = String.join(",",data);
                    } else {
                        meta = "";
                    }
                    try {
                        if (result.getString("action").equalsIgnoreCase("1"))
                            block.setBlockData(RestoreWorld.getInstance().getServer().
                                    createBlockData(materials.get(result.getInt("id"))+"["+meta+"]"));
                        else if (result.getString("action").equalsIgnoreCase("0"))
                            block.setType(Material.AIR);
                        RestoreWorld.getInstance().store.addLocation(loc);
                    } catch (SQLException e) {
                        System.out.println("Skiping block "+block.getType() + " at cord " + block.getX() + " " + block.getY() + " " + block.getZ());
                    }
                    System.out.println(block.getType() + " | " + block.getX() + " " + block.getY() + " " + block.getZ());
                }
            } catch (SQLException e) {
                return;
            }
        } catch (SQLException e) {
            return;
        }
        System.out.println("Finish");
        System.out.println(materials);
        System.out.println(materials.keySet());
        System.out.println(materials.values());
        System.out.println(worlds);
    }

    public void getWorlds(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_world")) {
            try(ResultSet result = stmt.executeQuery()){
                while (result.next()){
                    worlds.put(result.getInt("id"), result.getString("world"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getMaterials(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_material_map")) {
            try(ResultSet result = stmt.executeQuery()){
                while (result.next()){
                    materials.put(result.getInt("id"), result.getString("material"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String getMetadata(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_blockdata_map")) {
            try(ResultSet result = stmt.executeQuery()){
                while (result.next()){
                    metadata.put(result.getInt("id"), result.getString("data"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}