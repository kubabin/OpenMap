package dev.kubabin.openmap;

import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class CachedTile {
    public static final Path TILE_DIR = Path.of(FMLPaths.GAMEDIR.get().toString(), "openmap_tiles_cache");
    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;
    public static boolean saveToDisk = true;
    public ByteBuffer data = ByteBuffer.allocateDirect(WIDTH * HEIGHT * 3);
    private final File file;
    public final int x;
    public final int z;
    private boolean doesActuallyExistOnFs = false;

    public CachedTile(int x, int z) {
        this.x = x;
        this.z = z;
        // Ensure the file exists.
        file = new File(Path.of(TILE_DIR.toString(), "region_" + x + "_" + z + ".dat").toUri());
        if (!file.exists()) {
            try {
                doesActuallyExistOnFs = file.createNewFile();
            } catch (IOException e) {
                Openmap.LOGGER.error("Couldn't create tile file {}, {}: {}", x, z, e.getMessage());
            }
        } else {
            if (file.canRead()) {
                try (InputStream is = new FileInputStream(file)) {
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
    }

    public void saveToDisk() {
        if (!doesActuallyExistOnFs || !saveToDisk) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            data.rewind();
            byte[] buf = new byte[data.capacity()];
            data.get(buf);
            fos.write(buf);
        } catch (Exception e) {
            Openmap.LOGGER.error("Couldn't write to tile cache file at {}, {}: {}", x, z, e.getMessage());
        }
    }

    public void setPixel(int x, int y, int color) {
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
}
