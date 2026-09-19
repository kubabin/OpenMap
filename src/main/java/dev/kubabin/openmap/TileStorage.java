package dev.kubabin.openmap;


import java.util.*;

/**
 * A class for managing currently used tiles.
 */
public class TileStorage {
    private final HashMap<String, CachedTile> tiles = new HashMap<>(5);
    public boolean cleanup_regions = true;

    public CachedTile openRegionFile(int regionX, int regionZ) {
        String regionId = "%d;%d".formatted(regionX, regionZ);
        if (tiles.size() > 4 && cleanup_regions) {
            // Clean up far away regions
            HashSet<String> dirtyRegions = new HashSet<>(1);
            for (String key : tiles.keySet()) {
                int keyX = Integer.parseInt(key.split(";")[0]);
                int keyY = Integer.parseInt(key.split(";")[1]);
                if (Math.abs(regionX - keyX) > 2 ||
                        Math.abs(regionZ - keyY) > 2) {
                    tiles.get(key).saveToDisk();
                    dirtyRegions.add(key);
                }
            }
            for (String region : dirtyRegions){
                tiles.remove(region);
            }
        }
        if (!tiles.containsKey(regionId)) {
            if (tiles.size() >= 25) return null;
            CachedTile tile = new CachedTile(regionX, regionZ);
            tiles.put(regionId, tile);
            return tile;
        }
        return tiles.get(regionId);
    }

    /**
     *
     * @param x     Global world X coordinate
     * @param z     Global world Y coordinate
     * @param color Block/pixel color
     */
    public void writePixel(int x, int z, int color /*, short topo*/) {
        int regionX = Math.floorDiv(x, CachedTile.WIDTH);
        int regionZ = Math.floorDiv(z, CachedTile.HEIGHT);
        CachedTile tile = openRegionFile(regionX, regionZ);
        int localX = Math.floorMod(x, CachedTile.WIDTH);
        int localZ = Math.floorMod(z, CachedTile.HEIGHT);
        tile.setPixel(localX, localZ, color);
        //tile.setTopo(x, z, topo);
    }

    public void saveAll(){
        for (String tileKey : tiles.keySet()){
            CachedTile tile = tiles.get(tileKey);
            tile.saveToDisk();
        }
    }
    public void cleanup() {
        for (CachedTile tile : tiles.values()){
            tile.saveToDisk();
        }
        tiles.clear();
    }

}
