package dev.kubabin.openmap;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Openmap.MODID, value = Dist.CLIENT)
public class MinimapShaderHandler {
    private static ShaderInstance minimapShader;
    private static ShaderInstance worldmapShader;

    public static ShaderInstance getMinimapShader() {
        return minimapShader;
    }
    public static ShaderInstance getWorldmapShader() {
        return worldmapShader;
    }

    public static void setMinimapShader(ShaderInstance shader) {
        minimapShader = shader;
    }
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "minimap_shader"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shaderInstance -> minimapShader = shaderInstance
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "worldmap"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shaderInstance -> worldmapShader = shaderInstance
        );
    }
}
