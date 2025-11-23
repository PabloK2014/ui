package com.myaddon.iuaddon.container;

import com.denfop.container.ContainerBaseMolecular;
import com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import java.util.List;

public class ContainerImprovedMolecularTransformer extends ContainerBaseMolecular {

    public ContainerImprovedMolecularTransformer(EntityPlayer entityPlayer, TileEntityImprovedMolecularTransformer tileEntity1) {
        super(entityPlayer, tileEntity1); // This adds default slots

        // Clear default slots added by super
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        // Re-add our custom slots
        
        // Input Slots (12) - 4 columns x 3 rows
        // Start X: 8, Start Y: 20 (Adjusted to be more standard?)
        // User didn't give coords, sticking to previous: 20, 20
        for (int i = 0; i < 12; i++) {
            int x = 20 + (i % 4) * 18;
            int y = 20 + (i / 4) * 18;
            addSlotToContainer(new SlotInvSlot(tileEntity1.inputSlot, i, x, y));
        }

        // Output Slots (12) - 4 columns x 3 rows
        // Start X: 110, Start Y: 20
        for (int i = 0; i < 12; i++) {
            int x = 110 + (i % 4) * 18;
            int y = 20 + (i / 4) * 18;
            addSlotToContainer(new SlotInvSlot(tileEntity1.outputSlot, i, x, y));
        }

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(
                        new Slot(entityPlayer.inventory, k + i * 9 + 9, 18 + k * 18, 111 + i * 18)); // Adjusted Y to 111 (193 - 82)
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(entityPlayer.inventory, j, 18 + j * 18, 169)); // Adjusted Y to 169
        }
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return ((TileEntityImprovedMolecularTransformer)this.base).isUsableByPlayer(player);
    }
}
