package dev.kubabin.openmap.widgets;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.MenuItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class MenuWidget extends BetterAbstractWidget {
    static final int ENTRY_HEIGHT = 20;
    static final int MENU_WIDTH = 100;
    static final int ENTRY_PADDING = 4;
    private final HashMap<String, MenuItem> menuItems;
    public MenuWidget(int x, int y, HashMap<String, MenuItem> menuItems) {
        super(x, y, MENU_WIDTH, menuItems.size(), Component.empty());
        this.menuItems = menuItems;
        this.height = menuItems.size()*ENTRY_HEIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.fill(getX(),getY(),getX()+width,getY()+height, 0xFF302f00);
        guiGraphics.fill(getX()+2,getY()+2,
                getX()+width-2, getY()+height-2,
                0xFF424000);

        /*guiGraphics.blitSprite(
                ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "menu"),
                getX(), getY(),
                getWidth(), getHeight()
        );*/
        int x = getX()+ENTRY_PADDING;
        int y = getY();
        for (String key : menuItems.keySet()){
            MenuItem item = menuItems.get(key);
            guiGraphics.blit(item.icon(), x,y+1, 0,0,
                    ENTRY_HEIGHT-ENTRY_PADDING, ENTRY_HEIGHT-ENTRY_PADDING,
                    16, 16);
            guiGraphics.drawString(mc.font, item.text(), x+ENTRY_HEIGHT+ENTRY_PADDING,
                    y+6, 0xFFFFFFFF);
            y += ENTRY_HEIGHT;
        }
    }

    @Override
    protected boolean clicked(double p_93681_, double p_93682_) {
        if(!super.clicked(p_93681_, p_93682_)){
            // Close this menu
            Minecraft mc = Minecraft.getInstance();
            WorldmapScreen screen = (WorldmapScreen) mc.screen;
            screen.closeMenu();
            return false;
        }
        return true;
    }

    @Override
    public boolean onLeftClick(double mouseX, double mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        double relativeY = mouseY - this.getY();
        int entry = (int) (relativeY / ENTRY_HEIGHT);
        if (entry < 0 || entry >= menuItems.size()) {
            return false;
        }

        MenuItem[] items = menuItems.values().toArray(new MenuItem[0]);
        MenuItem item = items[entry];
        item.onClick().run(mouseX, mouseY);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
