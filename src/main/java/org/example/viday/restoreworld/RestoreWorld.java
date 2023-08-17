package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RestoreWorld extends JavaPlugin {
    public DataBase dataBase;
    public Store store;
    private static RestoreWorld instance;
    public BlockDataManager blockDataManager;
    public ContainerDataManager containerDataManager;

    @Override
    public void onEnable() {
        //Коннкетимся к базе данных
        instance = this;
        store = new Store();
        store.loadStore();
        HikariConfig config = new HikariConfig();
        blockDataManager = new BlockDataManager();
        containerDataManager = new ContainerDataManager();
        config.setJdbcUrl("jdbc:sqlite:" + getDataFolder() + "/database.db");
        dataBase = new DataBase(config);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RestoreWorld getInstance(){
        return instance;
    }
}