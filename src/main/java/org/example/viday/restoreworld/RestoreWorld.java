package org.example.viday.restoreworld;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class RestoreWorld extends JavaPlugin {
    private final List<ChuckData> locationDataList = new ArrayList<>();
    private DataBase dataBase = new DataBase(this);
    private Store store = new Store(this);
    @Override
    public void onEnable() {
        // Получение всех загруженных миров на сервере

//        Location location = new Location(Bukkit.getWorld("world"), 1000, 100, 1000);
//        location.getBlock().setType(Material.BEDROCK);

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

    public void addLocationData(int x, int y, int z) {
        locationDataList.add(new ChuckData(x, y, z));
    }
}
