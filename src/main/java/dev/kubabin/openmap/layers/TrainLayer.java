package dev.kubabin.openmap.layers;

import com.simibubi.create.compat.trainmap.TrainMapManager;
import com.simibubi.create.compat.trainmap.TrainMapSyncClient;
import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.Optional;

public class TrainLayer extends LayerProvider {
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip) {
        Rect2i bounds = new Rect2i((int) WorldmapScreen.screenToWorldX(0),
                (int) WorldmapScreen.screenToWorldZ(0),
                (int) WorldmapScreen.screenToWorldX(guiGraphics.guiWidth() * Minecraft.getInstance().options.guiScale().get()),
                (int) WorldmapScreen.screenToWorldZ(guiGraphics.guiHeight() * Minecraft.getInstance().options.guiScale().get()));
        mouseX = (int) WorldmapScreen.screenToWorldX(mouseX);
        mouseY = (int) WorldmapScreen.screenToWorldZ(mouseY);
        List<FormattedText> texts = TrainMapManager.renderAndPick(guiGraphics, mouseX, mouseY, false, bounds);
        if (texts == null || texts.isEmpty()) return;
        for (FormattedText text : texts) {
            MutableComponent tooltipText = Component.literal("");
            text.visit((Style style, String segment) -> {
                tooltipText.append(Component.literal(segment).withStyle(style));
                return Optional.empty();
            }, Style.EMPTY);
            tooltip.add(tooltipText);
        }

    }

    @Override
    public boolean clicked(int x, int y, int button) {
        return false;
    }

    @Override
    public void updateInitialData() {}

    @Override
    public void updateData() {
        TrainMapManager.tick();
        TrainMapSyncClient.requestData();
    }

    @Override
    public Component getName() {
        return Component.translatable("key.openmap.trains");
    }

    @Override
    public void onMapClose() {
        TrainMapSyncClient.stopRequesting();
    }
}
