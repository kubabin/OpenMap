package dev.kubabin.openmap.sidebuttons;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.tools.MapTool;

public class ToolSideButton extends SideButton {
    private final MapTool tool;

    public ToolSideButton(MapTool tool) {
        super(tool.getIcon(), tool.getTooltip());
        this.tool = tool;
    }

    @Override
    public void onClick() {
        if (WorldmapScreen.activeTool == tool) {
            WorldmapScreen.activeTool = null;
        } else {
            WorldmapScreen.activeTool = tool;
        }
    }
}
