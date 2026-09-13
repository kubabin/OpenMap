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

    public boolean childClicked(double mouseX, double mouseY, int button){
        if (child == null){
            return false;
        }
        return child.mouseClicked(mouseX,mouseY,button);
    }
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (child == null || !child.mouseClicked(x,y,button)){
            return super.mouseClicked(x,y,button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (child == null || !child.keyPressed(p_96552_, p_96553_, p_96554_)){
            return super.keyPressed(p_96552_, p_96553_, p_96554_);
        }
        return false;
    }

    @Override
    public boolean keyReleased(int p_94715_, int p_94716_, int p_94717_) {
        if (child == null || !child.keyReleased(p_94715_, p_94716_, p_94717_)){
            return super.keyPressed(p_94715_, p_94716_, p_94717_);
        }
        return false;
    }

    @Override
    public boolean charTyped(char p_94683_, int p_94684_) {
        if (child == null || !child.charTyped(p_94683_, p_94684_)){
            return super.charTyped(p_94683_, p_94684_);
        }
        return false;
    }

    public void renderChild(@NotNull GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_){
        if (child != null)
            child.render(p_281549_, p_281550_, p_282878_, p_282465_);
    }
    @Override
    public void render(@NotNull GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        super.render(p_281549_, p_281550_, p_282878_, p_282465_);

    }

    public void openScreen(NestedScreen screen){
        this.child = screen;
        this.child.init();
    }
    public void closeScreen(){
        if (this.child == null) return;
        this.child.onClose();
        this.child = null;
    }

    @Override
    public void onClose() {
        this.closeScreen();
        super.onClose();
    }
}
