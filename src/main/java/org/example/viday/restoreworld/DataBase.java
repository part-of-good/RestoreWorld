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
    private long count = 0;
    private @NotNull BukkitTask async;
    private boolean isStartedSetBlock = false;
    private boolean isFinishedQuery = false;


    public DataBase(HikariConfig config) {
        hds = new HikariDataSource(config);
        try {
            con = hds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        startAsyncQuery();
        async = Bukkit.getScheduler().runTaskTimer(RestoreWorld.getInstance(), this::setBlock, 0, 20);
    }

    private void startAsyncQuery() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    RestoreWorld.getInstance().getLogger().info("Формируем запрос...");
                    String query = "SELECT t1.* FROM co_block t1 JOIN (SELECT wid, x, y, z, MAX(time) AS max_time FROM co_block WHERE rolled_back = 0 GROUP BY wid, x, y, z) t2 ON t1.wid = t2.wid AND t1.x = t2.x AND t1.y = t2.y AND t1.z = t2.z AND t1.time = t2.max_time AND t1.time < 1691790003 AND t2.max_time < 1691790003 AND t1.time > 1691096400 AND t2.max_time > 1691096400;";
                    //String query = "SELECT t1.* FROM co_block t1 JOIN (SELECT wid, x, y, z, MAX(time) AS max_time FROM co_block WHERE rolled_back = 0 GROUP BY wid, x, y, z) t2 ON t1.wid = t2.wid AND t1.x = t2.x AND t1.y = t2.y AND t1.z = t2.z AND t1.time = t2.max_time";
                    final PreparedStatement stmt = con.prepareStatement(query);
                    ResultSet result = stmt.executeQuery();
                    RestoreWorld.getInstance().getLogger().info("Получили данные!");
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
                        int time = result.getInt("time");
                        if (action == 1) {
                            RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, getMaterial(type), time);
                        }
                        else {
                            RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, "air", time);
                        }
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
            RestoreWorld.getInstance().getLogger().info("Ожидание окончание работы бд");
            return;
        }
        RestoreWorld.getInstance().getLogger().info("Blocks added: " + BlockDataManager.count);
        if (isStartedSetBlock) {
            return;
        }
        isStartedSetBlock = true;
        RestoreWorld.getInstance().getLogger().info("Запуск установки блоков");
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<BlockData> iterator = RestoreWorld.getInstance().blockDataManager.getLocationDataList().iterator();
                while (iterator.hasNext()) {
                    BlockData blockData = iterator.next();
                    try {
                        Location locationData = blockData.getLocation();
                        try {
                            locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(blockData.getMaterial() + "[" + blockData.getMeta() + "]"));
                        } catch (Exception e) {
                            RestoreWorld.getInstance().getLogger().info("Установка блока который не имеет BlockData");
                            locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(blockData.getMaterial()));
                        }
                        RestoreWorld.getInstance().getLogger().info("[" + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(Long.parseLong( blockData.getTime() + "000"))) + "] " +
                                "[" + Math.round(( ((double) count / 62_000_000) * 100 ) * 1e10) / 1e10 + "%] " +
                                blockData.getMaterial() + " " +
                                blockData.getLocation().getBlockX() + " " +
                                blockData.getLocation().getBlockY() + " " +
                                blockData.getLocation().getBlockZ());
                        count++;
                        iterator.remove();
                    } catch (Exception e) {
                        iterator.remove();
                        RestoreWorld.getInstance().getLogger().warning("EXCEPTION!EXCEPTION!EXCEPTION!");
                    }
                }
                if (RestoreWorld.getInstance().blockDataManager.getLocationDataList().isEmpty()) {
                    async.cancel();
                    this.cancel();
                }
            }
        }.runTask(RestoreWorld.getInstance());

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