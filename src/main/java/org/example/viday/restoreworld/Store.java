package org.example.viday.restoreworld;

import java.util.ArrayList;
import java.util.List;

public class Store {
    private final RestoreWorld restoreWorld;
    private static final List<String> defaultValue = new ArrayList<>();

    public Store(RestoreWorld restoreWorld) {
        this.restoreWorld = restoreWorld;
        if (!restoreWorld.getDataFolder().exists()) {
            restoreWorld.getDataFolder().mkdirs();
        }
        restoreWorld.saveConfig();

        if (restoreWorld.getConfig().contains("loc")) {
            List<String> defaultValue = new ArrayList<>();
            restoreWorld.getConfig().set("loc", defaultValue);
        }
    }

    public boolean checkExists(int world, int x, int y, int z) {
        return restoreWorld.getConfig().getStringList("loc").contains(world + "," + x + "," + y + "," + z);
    }

    public void addLocation(int world, int x, int y, int z) {
        List<String> current = restoreWorld.getConfig().getStringList("loc");
        current.add(world + "," + x + "," + y + "," + z);
        restoreWorld.getConfig().set("loc", current);
    }

}
