package dev.kubabin.openmap;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MinimapThreadManager {
    private static final ExecutorService TEXTURE_WORKER = Executors.newSingleThreadExecutor();
    private static final ExecutorService FILE_WORKER = Executors.newSingleThreadExecutor();
    public static final TileStorage tileStorage = new TileStorage();
    public static boolean pause = false;

    public static void updateMap(){
        Minecraft mc = Minecraft.getInstance();
        Entity player = Minecraft.getInstance().getCameraEntity();
        processSnapshotAsync(ChunkSnapshot.createSnapshot(mc.level, player.blockPosition(),
                ClientModEvents.CAPTURE_SIZE));
    }
    public static void processSnapshotAsync(ChunkSnapshot snapshot) {
        if (pause)
            return;
        TEXTURE_WORKER.submit(() -> {
            int index = 0;
            for (int z = 0; z < snapshot.size; z++) {
                for (int x = 0; x < snapshot.size; x++) {
                    int color = snapshot.colorData[index++];
                    DynamicTextureManager.setPixel(z, x, 0xFF000000 | color);
                }
            }
            DynamicTextureManager.readyToUpload = true;
        });
        FILE_WORKER.submit(() -> {
            final int regionX = snapshot.centerX-(snapshot.size/2);
            final int regionZ = snapshot.centerZ-(snapshot.size/2);
            for (int x = 0; x < snapshot.size; x++) {
                for (int z = 0; z < snapshot.size; z++) {
                    int color = snapshot.colorData[(x*snapshot.size)+z];
                    tileStorage.writePixel(regionX + x, regionZ + z, color);
                }
            }
        });
    }
}
