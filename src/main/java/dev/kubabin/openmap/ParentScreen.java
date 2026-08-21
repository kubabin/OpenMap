package dev.kubabin.openmap;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class ParentScreen extends Screen {
    private NestedScreen child;
    protected ParentScreen(Component title) {
        super(title);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (!child.mouseClicked(x,y,button)){
            return super.mouseClicked(x,y,button);
        }
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        super.render(p_281549_, p_281550_, p_282878_, p_282465_);
        if (child != null)
            child.render(p_281549_, p_281550_, p_282878_, p_282465_);
    }

    private void openScreen(NestedScreen screen){
        this.child = screen;
        this.child.init();
    }
    public void closeScreen(){
        this.child.onClose();
        this.child = null;
    }

    @Override
    public void onClose() {
        this.closeScreen();
        super.onClose();
    }
}
