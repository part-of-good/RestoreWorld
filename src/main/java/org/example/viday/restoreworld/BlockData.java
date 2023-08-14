package org.example.viday.restoreworld;

import org.bukkit.Location;

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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlockData)) return false;
        final BlockData other = (BlockData) o;
        if (!other.canEqual(this)) return false;
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        final Object this$meta = this.getMeta();
        final Object other$meta = other.getMeta();
        if (this$meta == null ? other$meta != null : !this$meta.equals(other$meta)) return false;
        if (this.getAction() != other.getAction()) return false;
        final Object this$strMaterial = this.strMaterial;
        final Object other$strMaterial = other.strMaterial;
        if (this$strMaterial == null ? other$strMaterial != null : !this$strMaterial.equals(other$strMaterial))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlockData;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        final Object $meta = this.getMeta();
        result = result * PRIME + ($meta == null ? 43 : $meta.hashCode());
        result = result * PRIME + this.getAction();
        final Object $strMaterial = this.strMaterial;
        result = result * PRIME + ($strMaterial == null ? 43 : $strMaterial.hashCode());
        return result;
    }
}
