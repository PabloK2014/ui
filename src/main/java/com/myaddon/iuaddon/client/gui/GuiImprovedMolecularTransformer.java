package com.myaddon.iuaddon.client.gui;

import com.denfop.container.ContainerBaseMolecular;
import com.denfop.gui.GuiMolecularTransformer;
import com.myaddon.iuaddon.IUAddon;
import net.minecraft.util.ResourceLocation;

public class GuiImprovedMolecularTransformer extends GuiMolecularTransformer {

    public GuiImprovedMolecularTransformer(ContainerBaseMolecular container1) {
        super(container1);
        this.xSize = 210;
        this.ySize = 193;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        this.bindTexture();
        int xStart = (this.width - this.xSize) / 2;
        int yStart = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(xStart, yStart, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public ResourceLocation getTexture() {
        // Use our own texture
        return new ResourceLocation(IUAddon.MOD_ID, "textures/gui/improved_molecular_transformer.png");
    }
    
    @Override
    public String getName() {
        return "Improved Molecular Transformer";
    }
}
