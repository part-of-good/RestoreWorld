package org.example.viday.restoreworld;

import org.bukkit.Location;

public class BlockData {

    private Location location;
    private String meta;
    private long time;
    private String strMaterial;

    public BlockData(Location location, String meta, String strMaterial, long time) {
        this.location = location;
        this.meta = meta;
        this.time = time;
        this.strMaterial = strMaterial;
    }

    public Location getLocation() {
        return location;
    }

    public String getMeta() {
        return meta;
    }

    public long getTime() {
        return time;
    }

    public String getMaterial() {
        return strMaterial;
    }
}
