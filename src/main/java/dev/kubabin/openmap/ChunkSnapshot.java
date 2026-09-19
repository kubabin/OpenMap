package dev.kubabin.openmap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

public class ChunkSnapshot {
    public int centerX, centerZ; // Block position the snapshot is centered on
    public int size;
    public int[] colorData;
    //public short[] topoMap;
    public static double[] waterTints = {
            0.0,
            0.5, 0.6, 0.7, 0.8, 1.0
    };

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
                //index++;
                /*int depth = 0;
                while (!state.getFluidState().isEmpty()){
                    state = level.getBlockState(blockPos);
                    blockPos = blockPos.below();
                    depth++;
                }
                depth = Math.min(depth/2, waterTints.length-1);
                MapColor mapColor = state.getMapColor(level, blockPos);
                highestY = blockPos.getY();
                int color = mapColor.calculateRGBColor(getBrightness(highestY, prevY, x, z));
                double waterTint = waterTints[depth];
                color = multiplyARGB(color, 1.0, 1-waterTint, 1-waterTint,
                        waterTint == 0 ? 1.0 : waterTint);*/
                heightData[index] = highestY;
                mapColors[index] = state.getMapColor(level, blockPos);
            }
        }

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int index = (x * size) + z;
                //this.topoMap[index] = (short) heightData[index];
                MapColor mapColor = mapColors[index];
                if (mapColor == null) {
                    // Unloaded chunk sample: keep previous colorData value.
                    continue;
                }
                int worldX = startX + x;
                int worldZ = startZ + z;
                this.colorData[index] = mapColor.calculateRGBColor(getBrightness(heightData, size, x, z, worldX, worldZ));
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
    private static MapColor.Brightness getBrightness(int[] heightData, int size, int x, int z, int worldX, int worldZ) {
        int y = heightData[(x * size) + z];
        int westY = x > 0 ? heightData[((x - 1) * size) + z] : y;
        int eastY = x + 1 < size ? heightData[((x + 1) * size) + z] : y;
        int northY = z > 0 ? heightData[(x * size) + (z - 1)] : y;
        int southY = z + 1 < size ? heightData[(x * size) + (z + 1)] : y;

        // Use 2D terrain gradient so shading is stable regardless of travel direction.
        double slope = ((eastY - westY) + (southY - northY)) * 0.4;
        // Anchor dithering to world coordinates so shading stays stable while the map scrolls.
        slope += (((worldX + worldZ) & 1) - 0.5) * 0.4;
        if (slope > 0.6) {
            return MapColor.Brightness.HIGH;
        } else if (slope < -0.6) {
            return MapColor.Brightness.LOW;
        }
        return MapColor.Brightness.NORMAL;
    }
    static int multiplyARGB(int argb, double a, double r, double g, double b) {
        int A = (argb >>> 24) & 0xFF;
        int R = (argb >>> 16) & 0xFF;
        int G = (argb >>>  8) & 0xFF;
        int B =  argb        & 0xFF;

        A = (int) Math.min(255, A * a);
        R = (int) Math.min(255, R * r);
        G = (int) Math.min(255, G * g);
        B = (int) Math.min(255, B * b);

        return (A << 24) | (R << 16) | (G << 8) | B;
    }
}
