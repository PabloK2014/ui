package com.myaddon.iuaddon.client.gui;

import com.denfop.container.ContainerBaseMolecular;
import com.denfop.gui.GuiMolecularTransformer;
import com.myaddon.iuaddon.IUAddon;
import net.minecraft.util.ResourceLocation;

public class GuiImprovedMolecularTransformer extends GuiMolecularTransformer {

    public GuiImprovedMolecularTransformer(ContainerBaseMolecular container1) {
        super(container1);
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
