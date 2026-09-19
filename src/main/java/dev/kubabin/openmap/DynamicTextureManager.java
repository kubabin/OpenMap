package dev.kubabin.openmap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class DynamicTextureManager {
    public static final ResourceLocation DYNAMIC_TEXTURE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "minimap_texture");

    private static DynamicTexture dynamicTexture;
    protected static NativeImage nativeImage;
    protected static DynamicTexture worldmapTexture;
    public static boolean readyToUpload = false;
    private static final Object IMAGE_LOCK = new Object();
    public static boolean loaded = false;
    public static void initTexture() {
        nativeImage = new NativeImage(Config.getMapSize(), Config.getMapSize(), false);

        dynamicTexture = new DynamicTexture(nativeImage);
        // Linear filtering so sub-pixel map scrolling looks smooth
        dynamicTexture.setFilter(true, false);

        // Register it into Minecraft's texture engine
        Minecraft.getInstance().getTextureManager().register(DYNAMIC_TEXTURE_LOCATION, dynamicTexture);
        loaded = true;

        worldmapTexture = new DynamicTexture(512, 512, false);
        worldmapTexture.setFilter(false, false);
    }
    public static void reinitTexture() {
        if (!loaded) return;
        nativeImage = new NativeImage(Config.getMapSize(), Config.getMapSize(), false);
        dynamicTexture.setPixels(nativeImage);
    }
    public static void setPixel(int x, int z, int argbColor) {
        synchronized (IMAGE_LOCK) {
            if (dynamicTexture == null) return;
            if (nativeImage == null) return;
            nativeImage.setPixelRGBA(x, z, argbColor);
        }
    }

    public static void replaceImageAndUpload(NativeImage image) {
        synchronized (IMAGE_LOCK) {
            nativeImage = image;
            if (dynamicTexture == null) {
                return;
            }
            dynamicTexture.setPixels(nativeImage);
            dynamicTexture.upload();
            readyToUpload = false;
        }
    }

    public static DynamicTexture getTexture() {
        return dynamicTexture;
    }
}
