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
    public DataBase dataBase;
    public Store store;
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
        dataBase.getWorlds();
        dataBase.getMetadata();
        dataBase.getMaterials();
        dataBase.updateBlocks();

    }

/*    public void updateBlocks() {
        ResultSet result = dataBase.getBlocks();
        try {
            System.out.println("test1.5");
            while (result.next()){
                System.out.println("test2");

            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        System.out.println("FINISH!");

    }*/

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RestoreWorld getInstance(){
        return instance;
    }
}