package dev.kubabin.openmap;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.WaterFluid;

import static net.minecraft.world.level.material.MapColor.GRASS;
import static net.minecraft.world.level.material.MapColor.WATER;

public class ChunkSnapshot {
    public int centerX, centerZ; // Block position the snapshot is centered on
    public int size;
    public int[] colorData;
    //public short[] topoMap;

    private ChunkSnapshot() {
        colorData = new int[Config.getMapSize() * Config.getMapSize()];
        //topoMap = new short[Config.mapSize*Config.mapSize];
    }
    public static ChunkSnapshot createSnapshot(Level level, BlockPos playerPos, int size) {
        ChunkSnapshot newSnapshot = new ChunkSnapshot();
        newSnapshot.size = size;
        newSnapshot.centerX = playerPos.getX();
        newSnapshot.centerZ = playerPos.getZ();
        newSnapshot.updateSnapshot(level, playerPos);
        return newSnapshot;
    }
    public void updateSnapshot(Level level, BlockPos playerPos){
        this.centerX = playerPos.getX();
        this.centerZ = playerPos.getZ();
        int[] heightData = new int[size * size];
        MapColor[] mapColors = new MapColor[size * size];

        int startX = centerX - size / 2;
        int startZ = centerZ - size / 2;

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int index = (x * size) + z;
                int worldX = startX + x;
                int worldZ = startZ + z;
                BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos(worldX, playerPos.getY(), worldZ);
                if (!level.hasChunkAt(samplePos)) {
                    // Keep previous pixel data until the chunk is available.
                    continue;
                }
                int highestY;
                if (level.dimensionType().hasCeiling()){
                    highestY = findCeilingDimensionSurfaceY(level, worldX, worldZ, playerPos.getY());
                } else {
                    highestY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                }
                BlockPos blockPos = new BlockPos(worldX, highestY - 1, worldZ);
                BlockState state = level.getBlockState(blockPos);

                heightData[index] = highestY;
                mapColors[index] = state.getMapColor(level, blockPos);

                if (!state.getFluidState().isEmpty() && state.getFluidState().is(FluidTags.WATER) && !level.dimensionType().hasCeiling()){
                    int floorY;
                    for (floorY = highestY - 1; floorY >= level.getMinBuildHeight(); floorY--) {
                        blockPos = new BlockPos(worldX, floorY, worldZ);
                        state = level.getBlockState(blockPos);
                        if (state.getFluidState().isEmpty()) {
                            break;
                        }
                        heightData[index] = floorY;
                    }
                    blockPos = new BlockPos(worldX, floorY -2, worldZ);
                    state = level.getBlockState(blockPos);
                    mapColors[index] = state.getMapColor(level, blockPos);
                    int diff = highestY - floorY;
                    // Pack the diff into the heightData
                    if (diff > 0) {
                        heightData[index] = floorY | (diff << 16);
                    }
                }



            }
        }

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int index = (x * size) + z;
                MapColor mapColor = mapColors[index];
                if (mapColor == null) {
                    // Unloaded chunk sample: keep previous colorData value.
                    continue;
                }
                int worldX = startX + x;
                int worldZ = startZ + z;
                this.colorData[index] = mapColor.col;
                // Apply water tint if applicable
                int heightInfo = heightData[index] & 0xFFFF;
                int waterDiff = (heightData[index] >> 16) & 0xFF;

                if (mapColor.id == GRASS.id){
                    int tint = BiomeColors.getAverageGrassColor(level, new BlockPos(worldX, heightInfo, worldZ));
                    this.colorData[index] = tintColor(0.5, this.colorData[index],tint);
                } else if (mapColor.id == MapColor.PLANT.id){
                    int tint = BiomeColors.getAverageFoliageColor(level, new BlockPos(worldX, heightInfo, worldZ));
                    this.colorData[index] = tintColor(0.5, this.colorData[index],tint);
                }

                if (Config.underwaterHillshading){
                    this.colorData[index] = multColor(
                            getBrightness(heightData, size, x, z),
                            colorData[index]
                    ) | 0xFF000000;
                }

                if (waterDiff > 0) {
                    int tint = BiomeColors.getAverageWaterColor(level, new BlockPos(worldX, heightInfo, worldZ));
                    // Swap R and B channels
                    tint = ((tint & 0xFF) << 16) | (tint & 0xFF00) | ((tint >> 16) & 0xFF);
                    // Determine the tint factor based on the water depth
                    double tintFactor = Math.clamp(waterDiff / 10.0, 0.7, 0.9); // Cap the tint factor at 0.9
                    this.colorData[index] = tintColor(tintFactor, this.colorData[index],tint);
                }
                if (!Config.underwaterHillshading && waterDiff == 0) {
                    this.colorData[index] = multColor(
                            getBrightness(heightData, size, x, z),
                            colorData[index]
                    ) | 0xFF000000;
                }
            }
        }
    }

    private static int findCeilingDimensionSurfaceY(Level level, int worldX, int worldZ, int playerY) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int clampedPlayerY = Math.clamp(playerY, minY, maxY);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(worldX, clampedPlayerY, worldZ);
        BlockState current = level.getBlockState(pos);

        if (isMapAir(current)) {
            while (pos.getY() > minY && isMapAir(level.getBlockState(pos))) {
                pos.move(Direction.DOWN);
            }
            return pos.getY() + 1;
        }

        while (pos.getY() < maxY && !isMapAir(level.getBlockState(pos))) {
            pos.move(Direction.UP);
        }

        while (pos.getY() > minY && isMapAir(level.getBlockState(pos))) {
            pos.move(Direction.DOWN);
        }

        return pos.getY() + 1;
    }

    private static boolean isMapAir(BlockState state) {
        return state.isAir() || state.is(Blocks.STRUCTURE_VOID);
    }

    // Vanilla map-style slope shading: brighten uphill, darken downhill,
    // with checkerboard dithering to avoid banding on flat terrain.
    private static double getBrightness(int[] heightData, int size, int x, int z) {
        int y = heightData[(x * size) + z] & 0xFFFF;
        int westY = x > 0 ? (heightData[((x - 1) * size) + z] & 0xFFFF) : y;
        int eastY = x + 1 < size ? (heightData[((x + 1) * size) + z] & 0xFFFF) : y;
        int northY = z > 0 ? (heightData[(x * size) + (z - 1)] & 0xFFFF) : y;
        int southY = z + 1 < size ? (heightData[(x * size) + (z + 1)] & 0xFFFF) : y;

        // Use 2D terrain gradient so shading is stable regardless of travel direction.
        double slope = ((eastY - westY) + (southY - northY)) * 0.4;
        // Anchor dithering to world coordinates so shading stays stable while the map scrolls.
        //slope += (((worldX + worldZ) & 1) - 0.5) * 0.4;
        //slope *= 220 / 255f * 2;
        slope = (Math.clamp(slope/4, -1f, 1f) + 1f) / 2f;
        // Adjust the brightness, we think that 0.5 is middle, but MC devs think that 220/255 is middle.
        slope *= 220 / 255f * 2;
        slope = Math.clamp(slope, 0.01, 1);
        return slope;

    }
    static int tintColor(double tintStrength, int inputColor, int tintColor) {
        int r1 = (inputColor >> 16) & 0xFF;
        int g1 = (inputColor >> 8) & 0xFF;
        int b1 = inputColor & 0xFF;

        int r2 = (tintColor >> 16) & 0xFF;
        int g2 = (tintColor >> 8) & 0xFF;
        int b2 = tintColor & 0xFF;

        int r = (int) ((1 - tintStrength) * r1 + tintStrength * r2);
        int g = (int) ((1 - tintStrength) * g1 + tintStrength * g2);
        int b = (int) ((1 - tintStrength) * b1 + tintStrength * b2);

        return (r << 16) | (g << 8) | b;
    }
    static int multColor(double brightness, int inputColor){
        int r = (inputColor >> 16) & 0xFF;
        int g = (inputColor >> 8) & 0xFF;
        int b = inputColor & 0xFF;
        r = (int) (r * brightness);
        g = (int) (g * brightness);
        b = (int) (b * brightness);
        return (b << 16) | (g << 8) | r;
    }
}
