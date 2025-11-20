package com.myaddon.iuaddon.container;

import com.denfop.container.ContainerBaseMolecular;
import com.denfop.tiles.base.TileEntityMolecularTransformer;
import com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerImprovedMolecularTransformer extends ContainerBaseMolecular {

    public ContainerImprovedMolecularTransformer(EntityPlayer entityPlayer, TileEntityMolecularTransformer tileEntity1) {
        super(entityPlayer, tileEntity1);
        
        // Clear existing slots added by super (if possible, otherwise we might have duplicates or need to ignore them)
        // Since we can't easily clear private lists in super, we might have issues. 
        // However, ContainerBase usually adds slots to 'inventorySlots' list which is protected.
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        TileEntityImprovedMolecularTransformer te = (TileEntityImprovedMolecularTransformer) tileEntity1;

        // Input slots (4x3 grid)
        // Start X: 13 (12 + 1), Start Y: 17 (16 + 1)
        // Slot size 18, Gap 3 -> Step 21
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                addSlotToContainer(new SlotInvSlot(te.inputSlots[index], 0, 13 + col * 21, 17 + row * 21));
            }
        }

        // Output slots (4x3 grid)
        // Input ends at X = 13 + 3*21 + 18 = 94.
        // Gap 34 pixels. Start X = 94 + 34 = 128.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                addSlotToContainer(new SlotInvSlot(te.outputSlots[index], 0, 128 + col * 21, 17 + row * 21));
            }
        }

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(
                        new Slot(entityPlayer.inventory, k + i * 9 + 9, 8 + k * 18, 84 + i * 18 + 15)); // Adjusted Y
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(entityPlayer.inventory, j, 8 + j * 18, 142 + 15)); // Adjusted Y
        }
    }
}
