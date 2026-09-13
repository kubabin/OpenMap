package dev.kubabin.openmap;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.kubabin.openmap.ClientModEvents.CAPTURE_SIZE;

public class MinimapThreadManager {
    private static final ExecutorService TEXTURE_WORKER = Executors.newSingleThreadExecutor();
    private static final ExecutorService FILE_WORKER = Executors.newSingleThreadExecutor();
    private static final ExecutorService COORDINATOR = Executors.newSingleThreadExecutor();
    public static final TileStorage tileStorage = new TileStorage();
    private static ChunkSnapshot chunkSnapshot;
    public static boolean pause = false;

    public static void updateMap(){
        Minecraft mc = Minecraft.getInstance();
        Entity player = Minecraft.getInstance().getCameraEntity();
        processSnapshotAsync(ChunkSnapshot.createSnapshot(mc.level, player.blockPosition(),
                CAPTURE_SIZE));
    }
    public static void processSnapshotAsync(ChunkSnapshot snapshot) {
        if (pause)
            return;
        TEXTURE_WORKER.submit(() -> {
            DynamicTextureManager.getTexture().getPixels().applyToAllPixels((inp) -> 0);
            int index = 0;
            int offset = 130/2 - (snapshot.size / 2);
            for (int z = 0; z < snapshot.size; z++) {
                for (int x = 0; x < snapshot.size; x++) {
                    int color = snapshot.colorData[index++];

                    DynamicTextureManager.setPixel(offset+z, offset+x, 0xFF000000 | color);
                }
            }
            DynamicTextureManager.readyToUpload = true;
        });
        FILE_WORKER.submit(() -> {
            final int regionX = snapshot.centerX-(snapshot.size/2);
            final int regionZ = snapshot.centerZ-(snapshot.size/2);
            for (int x = 0; x < snapshot.size; x++) {
                for (int z = 0; z < snapshot.size; z++) {
                    int offset = (x * snapshot.size) + z;
                    int color = snapshot.colorData[offset];
                    //short height = snapshot.topoMap[offset];
                    //if (color == 0) return;
                    tileStorage.writePixel(regionX + x, regionZ + z, color /*, height*/);
                }
            }
        });
    }
    public static void process(Minecraft mc){
        COORDINATOR.submit(() -> {
            if (chunkSnapshot == null){
                chunkSnapshot = ChunkSnapshot.createSnapshot(mc.cameraEntity.level(), mc.cameraEntity.blockPosition(), CAPTURE_SIZE);
            }
            chunkSnapshot.updateSnapshot(mc.cameraEntity.level(), mc.cameraEntity.blockPosition());
            MinimapThreadManager.processSnapshotAsync(chunkSnapshot);
        });

    }
    public static void process(Player player, Level level){
        COORDINATOR.submit(()->{
            if (chunkSnapshot == null){
                chunkSnapshot = ChunkSnapshot.createSnapshot(level, player.blockPosition(), CAPTURE_SIZE);
            } else {
                // Keep full capture resolution even at high speed to preserve terrain shading/depth.
                chunkSnapshot.size = CAPTURE_SIZE;
                chunkSnapshot.updateSnapshot(level, player.blockPosition());
            }
            MinimapThreadManager.processSnapshotAsync(chunkSnapshot);
        });
    }
}
