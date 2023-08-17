package org.example.viday.restoreworld;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContainerDataManager {
    private List<ContainerData> containerDataList = new ArrayList<>();

    public void addLocationData(Location location, String meta, String strMaterial) {
        containerDataList.add(new ContainerData(location, meta, strMaterial));
    }

    public List<ContainerData> getContainerDataList() {
        return containerDataList;
    }

    public void removeContainerData(Location location, String meta, String strMaterial) {
        Iterator<ContainerData> iterator = containerDataList.iterator();
        while (iterator.hasNext()) {
            ContainerData containerData = iterator.next();
            if (containerData.getLocation() == location &&
                    containerData.getMeta().equals(meta) &&
                    containerData.getMaterial().equals(strMaterial)) {
                iterator.remove();
                break;
            }
        }
    }
}
