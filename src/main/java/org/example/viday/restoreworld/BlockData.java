package org.example.viday.restoreworld;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockData {

    private Location location;
    private String meta;
    private int action;
    private String strMaterial;

    public BlockData(Location location, String meta, int action, String strMaterial) {
        this.location = location;
        this.meta = meta;
        this.action = action;
        this.strMaterial = strMaterial;
    }

    public Location getLocation() {
        return location;
    }
    public String getMeta() {
        return meta;
    }
    public int getAction() {
        return action;
    }
    public String getMaterial() {
        return strMaterial;
    }
}
