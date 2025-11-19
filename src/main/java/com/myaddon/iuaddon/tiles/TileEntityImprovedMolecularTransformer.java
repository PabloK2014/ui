package com.myaddon.iuaddon.tiles;

import com.denfop.tiles.base.TileEntityMolecularTransformer;
import net.minecraft.util.ITickable;

public class TileEntityImprovedMolecularTransformer extends TileEntityMolecularTransformer {

    public TileEntityImprovedMolecularTransformer() {
        super();
        // Increase operations per tick or other stats
        this.operationsPerTick = 2; 
    }

    @Override
    public String getInventoryName() {
        return "Improved Molecular Transformer";
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public net.minecraft.client.gui.GuiScreen getGui(net.minecraft.entity.player.EntityPlayer entityPlayer, boolean isAdmin) {
        return new com.myaddon.iuaddon.client.gui.GuiImprovedMolecularTransformer(new com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer(entityPlayer, this));
    }

    @Override
    public ic2.core.ContainerBase<? extends TileEntityMolecularTransformer> getGuiContainer(net.minecraft.entity.player.EntityPlayer entityPlayer) {
        return new com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer(entityPlayer, this);
    }
}
