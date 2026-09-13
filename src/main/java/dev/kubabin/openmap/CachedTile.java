package dev.kubabin.openmap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CachedTile {
    public static final Path TILE_DIR = Path.of(FMLPaths.GAMEDIR.get().toString(), "openmap_tiles_cache");
    public static String worldName = "";
    public static String dimensionId = "";
    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;
    public static boolean saveToDisk = true;
    public ByteBuffer data = ByteBuffer.allocateDirect(WIDTH * HEIGHT * 3);
    //public ByteBuffer topoData = ByteBuffer.allocateDirect(WIDTH*HEIGHT*2);
    private final File file;
    public final int x;
    public final int z;
    private boolean doesActuallyExistOnFs = false;
    private boolean dirty = false;

    public CachedTile(int x, int z) {
        this.x = x;
        this.z = z;
        // Ensure the file exists.
        file = new File(Path.of(TILE_DIR.toString(), worldName, dimensionId, "region_" + x + "_" + z + ".tile").toUri());
        if (!file.exists()) {
            try {
                if (saveToDisk) {
                    doesActuallyExistOnFs = file.createNewFile();
                }
            } catch (IOException e) {
                Openmap.LOGGER.error("Couldn't create tile file {}, {}: {}", x, z, e.getMessage());
            }
        } else {
            if (file.canRead()) {
                try (InputStream is = new InflaterInputStream(new FileInputStream(file))) {
                    byte[] fileBytes = is.readAllBytes();
                    int copyLength = Math.min(fileBytes.length, data.capacity());
                    data.put(fileBytes, 0, copyLength);
                    /*topoData.put(fileBytes,
                            copyLength,
                            Math.min(fileBytes.length-data.capacity(), topoData.capacity())
                    );*/
                    doesActuallyExistOnFs = true;
                    data.clear();
                } catch (Exception e) {
                    Openmap.LOGGER.error("Couldn't read cached tile data in {}, {}: {}", x, z, e.getMessage());
                }
            }
        }
    }
    public static void ensureLevelDir(ClientLevel level){
        ResourceLocation location = level.dimension().location();
        CachedTile.dimensionId = CachedTile.getSafeFolderName(location.getNamespace()+":"+location.getPath());
        CachedTile.ensureDir();
    }
    public static String getSafeFolderName(String input) {
        if (input == null || input.isBlank()) {
            return "unknown_session";
        }
        String sanitized = input.replaceAll("[\\\\/:*?\"<>|\\s]", "_");

        sanitized = sanitized.replaceAll("[. ]+$", "");

        return sanitized.toLowerCase();
    }
    public static @NotNull String getSessionIdentifier() {
        Minecraft mc = Minecraft.getInstance();
        String sessionIdentifier;

        if (mc.getSingleplayerServer() != null) {
            // Singleplayer folder name (e.g., "New_World")
            sessionIdentifier = mc.getSingleplayerServer().getWorldData().getLevelName();
        } else {
            // Multiplayer Server IP/Domain (e.g., "play.hypixel.net_25565")
            ServerData serverData = mc.getCurrentServer();
            sessionIdentifier = (serverData != null) ? serverData.ip : "multiplayer_unknown";
        }
        return sessionIdentifier;
    }
    public static void ensureDir(){
        try {
            Files.createDirectories(TILE_DIR.resolve(worldName).resolve(dimensionId));
        } catch (IOException e) {
            Openmap.LOGGER.error("Couldn't create tiles directory for world: {}", e.getMessage());
        }
    }

    public void saveToDisk() {
        if (!doesActuallyExistOnFs || !saveToDisk || !dirty) {
            return;
        }
        try (DeflaterOutputStream fos = new DeflaterOutputStream(new FileOutputStream(file))) {
            data.rewind();
            byte[] buf = new byte[data.capacity()];
            data.get(buf);
            fos.write(buf);
            /*topoData.get(buf);
            fos.write(buf, 0, topoData.capacity());*/
        } catch (Exception e) {
            Openmap.LOGGER.error("Couldn't write to tile cache file at {}, {}: {}", x, z, e.getMessage());
        }
    }

    public void setPixel(int x, int y, int color) {
        this.dirty = true;
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) (color & 0xFF);

        this.setPixel(x, y, r, g, b);
    }

    public synchronized void setPixel(int x, int y, byte r, byte g, byte b) {
        int offset = (y*WIDTH*3) + (x*3);
        data.put(offset, b);
        data.put(offset + 1, g);
        data.put(offset + 2, r);
    }
    public void setTopo(int x, int y, short height){
        int offset = (y*WIDTH*2) + (x*2);
        //topoData.putShort(offset, height);
    }
}
