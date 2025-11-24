package com.myaddon.iuaddon.mixins;

import com.denfop.api.recipe.MachineRecipe;
import com.denfop.tiles.base.TileEntityDoubleElectricMachine;

import ic2.core.block.invslot.InvSlotOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.item.ItemStack;
import ic2.core.IC2;
import java.lang.reflect.Method;

@Mixin(targets = "com.denfop.tiles.mechanism.TileEntitySunnariumPanelMaker")
public abstract class MixinTileEntitySunnariumPanelMaker extends TileEntityDoubleElectricMachine {

    @Shadow(remap = false)
    public Object sunenergy;

    public MixinTileEntitySunnariumPanelMaker() {
        super(0, 0, 0, "", null);
    }
    
    @Overwrite(remap = false)
    protected void updateEntityServer() {
        boolean needsInvUpdate = false;

        MachineRecipe output = this.output;
        
        // Use reflection to access sunenergy methods
        double sunEnergyValue = 0;
        try {
            if (this.sunenergy != null) {
                Method getEnergy = this.sunenergy.getClass().getMethod("getEnergy");
                sunEnergyValue = ((Number)getEnergy.invoke(this.sunenergy)).doubleValue();
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }

        // FIX: Added this.outputSlot.canAdd(output.getRecipe().output.items) check
        if (output != null && this.outputSlot.canAdd(output.getRecipe().output.items) && this.energy.getEnergy() >= this.energyConsume && sunEnergyValue >= 5) {
            setActive(true);
            if (this.progress == 0) {
                IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
            }
            this.progress = (short) (this.progress + 1);
            this.energy.useEnergy(this.energyConsume);
            
            // Use reflection to use sunenergy
            try {
                if (this.sunenergy != null) {
                    Method useEnergy = this.sunenergy.getClass().getMethod("useEnergy", double.class);
                    useEnergy.invoke(this.sunenergy, 5.0);
                }
            } catch (Exception e) {
                // Try int if double fails
                try {
                    Method useEnergy = this.sunenergy.getClass().getMethod("useEnergy", int.class);
                    useEnergy.invoke(this.sunenergy, 5);
                } catch (Exception ex) {
                    // ex.printStackTrace();
                }
            }

            double k = this.progress;

            this.guiProgress = (k / this.operationLength);
            if (this.progress >= this.operationLength) {
                this.guiProgress = 0;
                operate(output);
                needsInvUpdate = true;
                this.progress = 0;
                IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
            }
        } else {
            if (this.progress != 0 && getActive()) {
                IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
            }
            if (output == null) {
                this.progress = 0;
            }
            setActive(false);
        }
        for (int i = 0; i < this.upgradeSlot.size(); i++) {
            ItemStack stack = this.upgradeSlot.get(i);
            if (stack != null && stack.getItem() instanceof ic2.api.upgrade.IUpgradeItem) {
                if (((ic2.api.upgrade.IUpgradeItem) stack.getItem()).onTick(stack, this)) {
                    needsInvUpdate = true;
                }
            }
        }

        if (needsInvUpdate) {
            super.markDirty();
        }
    }
}
