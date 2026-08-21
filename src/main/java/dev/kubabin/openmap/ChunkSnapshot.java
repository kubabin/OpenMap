package dev.kubabin.openmap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

public class ChunkSnapshot {
    public int centerX, centerZ; // Block position the snapshot is centered on
    public int size;
    public int[] colorData;

    private ChunkSnapshot() {
        colorData = new int[Config.mapSize*Config.mapSize];
    }
    public ChunkSnapshot(ChunkSnapshot snapshot){
        this.colorData = new int[Config.mapSize*Config.mapSize];
        System.arraycopy(snapshot.colorData, 0, this.colorData, 0, colorData.length);
        this.centerX = snapshot.centerX;
        this.centerZ = snapshot.centerZ;
        this.size = snapshot.size;
    }
    public static ChunkSnapshot createSnapshot(Level level, BlockPos playerPos, int size) {
        ChunkSnapshot newSnapshot = new ChunkSnapshot();
        newSnapshot.size = size;
        newSnapshot.centerX = playerPos.getX();
        newSnapshot.centerZ = playerPos.getZ();
        int index = 0;

        int startX = playerPos.getX() - size / 2;
        int startZ = playerPos.getZ() - size / 2;

        for (int x = 0; x < size; x++) {
            int prevY = Integer.MIN_VALUE;
            for (int z = 0; z < size; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                int highestY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                BlockPos blockPos = new BlockPos(worldX, highestY - 1, worldZ);
                BlockState state = level.getBlockState(blockPos);
                MapColor mapColor = state.getMapColor(level, blockPos);

                newSnapshot.colorData[index++] = mapColor.calculateRGBColor(getBrightness(highestY, prevY, x, z));
                prevY = highestY;
            }
        }
        return newSnapshot;
    }

    // Vanilla map-style slope shading: brighten uphill, darken downhill,
    // with checkerboard dithering to avoid banding on flat terrain.
    private static MapColor.Brightness getBrightness(int y, int prevY, int x, int z) {
        if (prevY == Integer.MIN_VALUE) {
            return MapColor.Brightness.NORMAL;
        }
        double slope = (y - prevY) * 4.0 / 5.0 + (((x + z) & 1) - 0.5) * 0.4;
        if (slope > 0.6) {
            return MapColor.Brightness.HIGH;
        } else if (slope < -0.6) {
            return MapColor.Brightness.LOW;
        }
        return MapColor.Brightness.NORMAL;
    }
}
