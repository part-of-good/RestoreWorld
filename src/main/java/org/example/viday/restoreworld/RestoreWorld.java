package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public final class RestoreWorld extends JavaPlugin {
    private DataBase dataBase;
    private Store store;
    private static RestoreWorld instance;

    @Override
    public void onEnable() {
        //Коннкетимся к базе данных
        instance = this;
        store = new Store();
        store.loadStore();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:"+getDataFolder()+"/database.db");
        dataBase = new DataBase(config);
        updateBlocks();
    }

    public void updateBlocks() {
        ResultSet result = dataBase.getBlocks();
        CompletableFuture.runAsync(() -> {
            try {
                while (result.next()){
                    System.out.println(result);
                    Location loc = new Location(Bukkit.getWorld(dataBase.getWorld(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                    if (store.checkExists(loc)) continue;
                    Block block = loc.getBlock();
                    String[] dataInt = result.getString("blockdata").split(",");
                    String[] data = new String[dataInt.length];
                    for (int i = 0; i < dataInt.length; i++){
                        data[i] =  dataBase.getBlockData(Integer.parseInt(dataInt[i]));
                    }
                    block.setBlockData(getServer().createBlockData(dataBase.getMaterial(result.getInt("type"))+"["+String.join(",",data)+"]"));
                    store.addLocation(loc);
                }
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RestoreWorld getInstance(){
        return instance;
    }
}