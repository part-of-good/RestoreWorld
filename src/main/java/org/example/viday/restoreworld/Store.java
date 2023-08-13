package org.example.viday.restoreworld;

import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Store {
    private final RestoreWorld restoreWorld = RestoreWorld.getInstance();

    public void loadStore(){
        if (!restoreWorld.getDataFolder().exists()) {
            restoreWorld.getDataFolder().mkdir();
        }
        final File file = new File(restoreWorld.getDataFolder(), "config.yml");
        if (!file.exists()) {
            restoreWorld.getConfig().options().copyDefaults(true);
            restoreWorld.saveDefaultConfig();
        }
    }

    public boolean checkExists(Location location) {
        return restoreWorld.getConfig().getStringList("loc").contains(location.getWorld() + "," + location.getBlockX()
                + "," + location.getBlockY() + "," + location.getBlockZ());
    }

    public void addLocation(Location location) {
        restoreWorld.getConfig().getStringList("loc").add(location.getWorld() + "," + location.getBlockX()
                + "," + location.getBlockY() + "," + location.getBlockZ());
        try {
            restoreWorld.getConfig().save(new File("plugins/RestoreWorld/", "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}