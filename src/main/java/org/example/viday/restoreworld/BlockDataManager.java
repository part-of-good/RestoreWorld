package org.example.viday.restoreworld;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockDataManager {
    private List<BlockData> locationDataList = new ArrayList<>();

    public void addLocationData(Location location, String meta, String strMaterial, long time) {
        locationDataList.add(new BlockData(location, meta, strMaterial, time));
    }

    public List<BlockData> getLocationDataList() {
        return locationDataList;
    }

    public void removeBlockData(Location location, String meta, String strMaterial, long time) {
        Iterator<BlockData> iterator = locationDataList.iterator();
        while (iterator.hasNext()) {
            BlockData BlockData = iterator.next();
            if (BlockData.getLocation() == location &&
                    BlockData.getMeta().equals(meta) &&
                    BlockData.getTime() == time &&
                    BlockData.getMaterial().equals(strMaterial)) {
                iterator.remove();
                break;
            }
        }
    }
}
