package org.example.viday.restoreworld;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockDataManager {
    private List<BlockData> locationDataList = new ArrayList<>();

    public void addLocationData(Location location, String meta, int action, String strMaterial) {
        locationDataList.add(new BlockData(location, meta, action, strMaterial));
    }

    public List<BlockData> getLocationDataList() {
        return locationDataList;
    }

    public void removeBlockData(Location location, String meta, int action, String strMaterial) {
        Iterator<BlockData> iterator = locationDataList.iterator();
        while (iterator.hasNext()) {
            BlockData BlockData = iterator.next();
            if (BlockData.getLocation() == location &&
                    BlockData.getMeta().equals(meta) &&
                    BlockData.getAction() == action &&
                    BlockData.getMaterial().equals(strMaterial)) {
                iterator.remove();
                break;
            }
        }
    }
}
