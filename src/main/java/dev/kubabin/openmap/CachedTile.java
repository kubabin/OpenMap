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
    public static final Path DATA_DIR = Path.of(FMLPaths.GAMEDIR.get().toString(), "openmap_data");
    public static String worldName = "";
    public static String dimensionId = "";
    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;
    public static boolean saveToDisk = true;
    public ByteBuffer data = ByteBuffer.allocateDirect(WIDTH * HEIGHT * 4);
    private final File file;
    public final int x;
    public final int z;
    private boolean doesActuallyExistOnFs = false;
    private boolean dirty = false;

    public CachedTile(int x, int z) {
        this.x = x;
        this.z = z;
        // Ensure the file exists.
        file = new File(Path.of(getWorldTileDir().toString(), x + "_" + z + ".tile").toUri());
        if (!file.exists()) {
            try {
                if (saveToDisk) {
                    doesActuallyExistOnFs = file.createNewFile();
                }
            } catch (IOException e) {
                Openmap.LOGGER.error("Couldn't create tile file {}, {}: {}", x, z, e.getMessage());
            }
        } else {
            loadData();
        }
    }
    private CachedTile(int x, int z, File file){
        this.x = x;
        this.z = z;
        this.file = file;
        loadData();
    }
    // Tries to open a tile. If it doesn't exist, returns null.
    public static CachedTile tryOpen(int x, int z){
        File file = new File(Path.of(getWorldTileDir().toString(), x + "_" + z + ".tile").toUri());
        if (!file.exists()) {
            return null;
        } else {
            return new CachedTile(x, z, file);
        }
    }
    private void loadData(){
        if (file.exists() && file.canRead()) {
            try (InputStream is = new InflaterInputStream(new FileInputStream(file))) {
                byte[] fileBytes = is.readAllBytes();
                int copyLength = Math.min(fileBytes.length, data.capacity());
                data.put(fileBytes, 0, copyLength);
                doesActuallyExistOnFs = true;
                data.clear();
            } catch (Exception e) {
                Openmap.LOGGER.error("Couldn't read cached tile data in {}, {}: {}", x, z, e.getMessage());
            }
        }
    }
    public static Path getWorldDataDir(){
        return Path.of(DATA_DIR.toString(), worldName, dimensionId);
    }
    public static Path getWorldTileDir(){
        return Path.of(getWorldDataDir().toString(), "tiles");
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
            if (mc.getConnection() == null || mc.getConnection().getServerData() == null) {
                return "multiplayer_unknown";
            }
            ServerData serverData = mc.getConnection().getServerData();
            sessionIdentifier = serverData.ip;
        }
        return sessionIdentifier;
    }
    public static void ensureDir(){
        try {
            Files.createDirectories(getWorldTileDir());
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
        } catch (Exception e) {
            Openmap.LOGGER.error("Couldn't write to tile cache file at {}, {}: {}", x, z, e.getMessage());
        }
    }

    public void setPixel(int x, int y, int color, byte a) {
        this.dirty = true;
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) (color & 0xFF);

        this.setPixel(x, y, r, g, b, a);
    }

    public synchronized void setPixel(int x, int y, byte r, byte g, byte b, byte a) {
        int offset = (y*WIDTH*4) + (x*4);
        data.put(offset, b);
        data.put(offset + 1, g);
        data.put(offset + 2, r);
        data.put(offset + 3, a);
    }
}
