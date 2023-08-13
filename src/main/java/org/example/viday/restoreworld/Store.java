package org.example.viday.restoreworld;

import java.io.File;
import java.util.ArrayList;

public class Store {
    private final RestoreWorld restoreWorld = RestoreWorld.getInstance();

    public void loadStore(){
        if (!restoreWorld.getDataFolder().exists()) {
            restoreWorld.getDataFolder().mkdir();
        }
        final File file = new File(restoreWorld.getDataFolder(), "config.yml");
        if (!file.exists()) {
            restoreWorld.saveDefaultConfig();
        }
        if (!restoreWorld.getConfig().contains("loc")) {
            restoreWorld.getConfig().set("loc", new ArrayList<>());
        }
    }

    public boolean checkExists(int world, int x, int y, int z) {
        return restoreWorld.getConfig().getStringList("loc").contains(world + "," + x + "," + y + "," + z);
    }

    public void addLocation(int world, int x, int y, int z) {
        restoreWorld.getConfig().getStringList("loc").add(world + "," + x + "," + y + "," + z);
    }

}