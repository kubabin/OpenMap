package dev.kubabin.openmap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MinimapThreadManager {
    public static final TileStorage tileStorage = new TileStorage();
    private static ChunkSnapshot chunkSnapshot;
    public static boolean pause = false;
    public static boolean updateMinimap = true;
    public static Thread mapperThread;
    public static double lastPlayerX = 0;
    public static double lastPlayerZ = 0;

    public static void processSnapshotAsync(ChunkSnapshot snapshot) {
        int index = 0;
        if (updateMinimap)
        {
            for (int z = 0; z < snapshot.size; z++) {
                for (int x = 0; x < snapshot.size; x++) {
                    int color = snapshot.colorData[index++];
                    DynamicTextureManager.setPixel(z, x, 0xFF000000 | color);
                }
            }
            lastPlayerX = snapshot.centerX;
            lastPlayerZ = snapshot.centerZ;
            DynamicTextureManager.readyToUpload = true;
        }
        final int regionX = snapshot.centerX - (snapshot.size / 2);
        final int regionZ = snapshot.centerZ - (snapshot.size / 2);
        // Why am I shortening this by 2, you might ask?
        // Well, this is a fix for the messed up map. Edges are funky, but the center-ish data is correct.
        for (int x = 2; x < snapshot.size-2; x++) {
            for (int z = 2; z < snapshot.size-2; z++) {
                int offset = (x * snapshot.size) + z;
                int color = snapshot.colorData[offset];
                //short height = snapshot.topoMap[offset];
                //if (color == 0) return;
                tileStorage.writePixel(regionX + x, regionZ + z, color /*, height*/);
            }
        }
    }

    public static void threadRun() {
        Entity playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null) return;
        Level world = Minecraft.getInstance().level;
        if (world == null) return;
        ChunkSnapshot snapshot = ChunkSnapshot.createSnapshot(world, playerEntity.blockPosition(), Config.getMapSize());
        while (true) {
            try {
                if (!pause) {
                    BlockPos playerPos = playerEntity.blockPosition();
                    snapshot.updateSnapshot(world, playerPos);
                    processSnapshotAsync(snapshot);

                }
                Thread.sleep(25);
            } catch (InterruptedException e) {
                return;
            }
            catch (Exception e){
                Openmap.LOGGER.error("Error in OpenMap Mapper Thread: {}", e.getMessage());
                break;
            }
        }
    }

    public static void startThread() {
        mapperThread = new Thread(MinimapThreadManager::threadRun);
        mapperThread.setName("OpenMap Mapper Thread");
        mapperThread.start();
    }
    public static void stop(){
        if (mapperThread != null && mapperThread.isAlive()) {
            mapperThread.interrupt();
        }
    }
}
