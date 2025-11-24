package com.myaddon.iuaddon.mixins;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "com.denfop.tiles.mechanism.TileEntitySunnariumMaker")
public abstract class MixinTileEntitySunnariumMaker extends TileEntity {

    /**
     * @author MyAddon
     * @reason Fix AE2 crafting stuck by ensuring inventory changes are synced
     */
    @Overwrite(remap = false)
    public void onUpdate() {
        this.markDirty();
    }
}
