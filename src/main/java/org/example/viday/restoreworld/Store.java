package org.example.viday.restoreworld;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Store {
    private final RestoreWorld restoreWorld = RestoreWorld.getInstance();

    public void loadStore(){
        if (!restoreWorld.getDataFolder().exists()) {
            restoreWorld.getDataFolder().mkdir();
        }
        final File file = new File(restoreWorld.getDataFolder(), "config.yml");
    }

    public boolean checkExists(Location location) {
        return restoreWorld.getConfig().getStringList("Locations").contains(location.getWorld().getName() + "," + location.getBlockX()
                + "," + location.getBlockY() + "," + location.getBlockZ());
    }

    public void addLocation(Location location) {
        List<String> list = restoreWorld.getConfig().getStringList("Locations");
        list.add(location.getWorld().getName() + "," + location.getBlockX()
                + "," + location.getBlockY() + "," + location.getBlockZ());
        restoreWorld.getConfig().set("Locations", list);
        restoreWorld.saveConfig();
    }

}