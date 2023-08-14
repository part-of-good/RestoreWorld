package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataBase {
    public static HikariDataSource hds;
    public Connection con;
    private long count = 102161;
    private boolean isFinished = false;
    private @NotNull BukkitTask async;
    private String timePrefix;
    private String precent;

    public DataBase(HikariConfig config) {
        hds = new HikariDataSource(config);
        try {
            con = hds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        async = Bukkit.getScheduler().runTaskTimer(RestoreWorld.getInstance(), this::setBlock, 0, 1);
    }

    public void updateBlocksTest(){
        new BukkitRunnable(){
            @Override
            public void run() {
                // try(final PreparedStatement stmt = con.prepareStatement("SELECT * FROM co_block WHERE rolled_back = 0 AND time < 1691774000 ORDER BY time DESC")) {
                try(final PreparedStatement stmt = con.prepareStatement("SELECT * FROM co_block WHERE rolled_back = 0")) {
                    try(ResultSet result = stmt.executeQuery()){
                        while (result.next()){
                            timePrefix = "[" + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(Long.parseLong(result.getInt("time") + "000"))) + "] ";
                            precent = "[" + Math.round(( ((double) count / 62_000_000) * 100 ) * 1e10) / 1e10 + "%] ";

                            Location loc = new Location(Bukkit.getWorld(RestoreWorld.getInstance().dataBase.getWorld(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                            if (RestoreWorld.getInstance().store.checkExists(loc)) {
                                System.out.println(timePrefix + precent + "SKIP");
                                continue;
                            }
                            RestoreWorld.getInstance().store.addLocation(loc);
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
                            RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, result.getInt("action"), RestoreWorld.getInstance().dataBase.getMaterial(result.getInt("type")), result.getInt("time"));
                            count++;

                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                isFinished = true;
                System.out.println("Finish");
            }
        }.runTaskAsynchronously(RestoreWorld.getInstance());
    }

    public void setBlock() {
        BlockData blockDataSave = null;
        try {
            for (BlockData blockData: RestoreWorld.getInstance().blockDataManager.getLocationDataList()) {
                blockDataSave = blockData;
                Location locationData = blockData.getLocation();
                if (blockData.getAction() == 1) {
                    locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(blockData.getMaterial()+"["+blockData.getMeta()+"]"));
                }
                else {
                    locationData.getBlock().setType(Material.AIR);
                }
                RestoreWorld.getInstance().blockDataManager.removeBlockData(blockData.getLocation(), blockData.getMeta(), blockData.getAction(), blockData.getMaterial(), blockData.getTime());
                System.out.println(timePrefix + precent + blockData.getMaterial() + " | " + blockData.getLocation().getBlockX() + " " + blockData.getLocation().getBlockY() + " " + blockData.getLocation().getBlockZ());
            }
        } catch (Exception e) {
            if (blockDataSave != null) {
                RestoreWorld.getInstance().getLogger().info("Удаление из массива после ошибки");
                RestoreWorld.getInstance().blockDataManager.removeBlockData(blockDataSave.getLocation(), blockDataSave.getMeta(), blockDataSave.getAction(), blockDataSave.getMaterial(), blockDataSave.getTime());
            }
            RestoreWorld.getInstance().getLogger().warning("EXCEPTION!EXCEPTION!EXCEPTION!");
        }
        if (isFinished) async.cancel();
    }

    public void updateBlocks(){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_block WHERE rolled_back = 0 AND time < 1691774000 ORDER BY time DESC")) {
            try(ResultSet result = stmt.executeQuery()){
                while (result.next()){
                    String timePrefix = "[" + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(Long.parseLong(result.getInt("time") + "000"))) + "] ";
                    String precent = "[" + Math.round(( ((double) count / 62_000_000) * 100 ) * 1e10) / 1e10 + "%] ";

                    Location loc = new Location(Bukkit.getWorld(RestoreWorld.getInstance().dataBase.getWorld(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                    if (RestoreWorld.getInstance().store.checkExists(loc)) {
                        System.out.println(timePrefix + precent + "SKIP");
                        continue;
                    }
                    RestoreWorld.getInstance().store.addLocation(loc);
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
                    try {
                        if (result.getString("action").equalsIgnoreCase("1"))
                            block.setBlockData(RestoreWorld.getInstance().getServer().createBlockData(RestoreWorld.getInstance().dataBase.getMaterial(result.getInt("type"))+"["+meta+"]"));
                        else if (result.getString("action").equalsIgnoreCase("0")) {
                            block.setType(Material.AIR);
                        }
                        System.out.println(timePrefix + precent + block.getType().toString() + " | " + block.getX() + " " + block.getY() + " " + block.getZ());
                        count++;
                    } catch (Exception e) {
                        System.out.println(timePrefix + precent + "EXCEPTION");
                        count++;
                        continue;
                    }
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