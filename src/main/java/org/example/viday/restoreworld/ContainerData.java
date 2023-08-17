package org.example.viday.restoreworld;

import org.bukkit.Location;

public class ContainerData {
    private Location location;
    private String meta;
    private String strMaterial;

    public ContainerData(Location location, String meta, String strMaterial) {
        this.location = location;
        this.meta = meta;
        this.strMaterial = strMaterial;
    }

    public Location getLocation() {
        return location;
    }

    public String getMeta() {
        return meta;
    }


    public String getMaterial() {
        return strMaterial;
    }
}
