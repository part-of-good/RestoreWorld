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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DataBase {
    public static HikariDataSource hds;
    public Connection con;
    private long count = 102161;
    private @NotNull BukkitTask async;
    private String timePrefix;
    private String precent;
    private Store store;
    private final HashMap<BlockData, Long> checkTime = new HashMap<>();
    private int sizeToFinish = 0;
    private boolean isStartedSetBlock = false;
    private boolean isFinishedQuery = false;


    public DataBase(HikariConfig config, Store store) {
        hds = new HikariDataSource(config);
        this.store = store;
        try {
            con = hds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        startAsyncQuery();
        async = Bukkit.getScheduler().runTaskTimer(RestoreWorld.getInstance(), this::setBlock, 0, 1);
    }

    private void startAsyncQuery() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Формируем запрос...");
                    String query = "SELECT wid, x, y, z, blockdata, time, action, type " +
                            "FROM co_block " +
                            "WHERE rolled_back = 0 " +
                            "ORDER BY wid, x, y, z, time DESC";
                    final PreparedStatement stmt = con.prepareStatement(query);
                    System.out.println("Получили данные!");
                    ResultSet result = stmt.executeQuery();
                    while (result.next()) {
                        String meta = "";
                        if (result.getString("blockdata") != null) {
                            result.getString("blockdata").split(",");
                            String[] dataInt = result.getString("blockdata").split(",");
                            String[] data = new String[dataInt.length];
                            for (int i = 0; i < dataInt.length; i++) {
                                data[i] = RestoreWorld.getInstance().dataBase.getBlockData(Integer.parseInt(dataInt[i]));
                            }
                            meta = String.join(",", data);
                        }

                        // Обработка результатов
                        Location loc = new Location(Bukkit.getWorld(RestoreWorld.getInstance().dataBase.getWorld(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                        int type = result.getInt("type");
                        int action = result.getInt("action");
                        RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, action, getMaterial(type));
                        //RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, result.getInt("action"), RestoreWorld.getInstance().dataBase.getMaterial(result.getInt("type")));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isFinishedQuery = true;
            }
        }.runTaskAsynchronously(RestoreWorld.getInstance());
    }

    public void setBlock(){
        if (!isFinishedQuery) {
            System.out.println("Ожидание окончание работы бд");
            return;
        }
        if (isStartedSetBlock) {
            return;
        }
        isStartedSetBlock = true;
        System.out.println("Запуск установки блоков");
        Iterator<BlockData> iterator = RestoreWorld.getInstance().blockDataManager.getLocationDataList().iterator();
        while (iterator.hasNext()) {
            BlockData blockData = iterator.next();
            try {
                System.out.println(blockData.getMaterial() + " " + blockData.getAction() + " " + blockData.getLocation().getBlockX() + " " + blockData.getLocation().getBlockY() + " " + blockData.getLocation().getBlockZ());
                Location locationData = blockData.getLocation();
                if (blockData.getAction() == 1) {
                    locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(blockData.getMaterial() + "[" + blockData.getMeta() + "]"));
                } else {
                    locationData.getBlock().setType(Material.AIR);
                }
                iterator.remove();
            } catch (Exception e) {
                iterator.remove();
                RestoreWorld.getInstance().getLogger().warning("EXCEPTION!EXCEPTION!EXCEPTION!");
            }
        }
        if (RestoreWorld.getInstance().blockDataManager.getLocationDataList().isEmpty()) {
            async.cancel();

        }
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