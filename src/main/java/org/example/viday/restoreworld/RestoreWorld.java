package org.example.viday.restoreworld;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class RestoreWorld extends JavaPlugin {
    private final DataBase dataBase = new DataBase(this);
    private static RestoreWorld instance;

    @Override
    public void onEnable() {
        //Коннкетимся к базе данных
        instance = this;
        try {
            dataBase.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RestoreWorld getInstance(){
        return instance;
    }
}