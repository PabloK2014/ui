package com.myaddon.iuaddon.container;

import com.denfop.container.ContainerBaseMolecular;
import com.denfop.tiles.base.TileEntityMolecularTransformer;
import com.myaddon.iuaddon.tiles.TileEntityMysticalGrower;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import com.blakebr0.mysticalagriculture.items.ItemSeed;
import com.blakebr0.mysticalagriculture.blocks.BlockStorage;
import net.minecraft.block.Block;

public class ContainerMysticalGrower extends ContainerBaseMolecular {

    public ContainerMysticalGrower(EntityPlayer entityPlayer, TileEntityMolecularTransformer tileEntity1) {
        super(entityPlayer, tileEntity1);
        
        // Clear existing slots added by super
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        TileEntityMysticalGrower te = (TileEntityMysticalGrower) tileEntity1;

        // Seed slot: только семена MA
        addSlotToContainer(new SlotInvSlot(te.seedSlot, 0, 62, 33) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemSeed;
            }
        });
        
        // Accelerator slot: только блоки эссенции MA
        addSlotToContainer(new SlotInvSlot(te.acceleratorSlot, 0, 62, 56) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && Block.getBlockFromItem(stack.getItem()) instanceof BlockStorage;
            }
        });
        
        // Output slot: начало x113 y43, размер 19x19 (132-113=19, 62-43=19)
        // Центр слота: x113 + 9.5 ≈ 122, y43 + 9.5 ≈ 52
        addSlotToContainer(new SlotInvSlot(te.outputSlot, 0, 113, 43));

        // Player inventory (main 3 rows) - сдвигаем на 1 пиксель ниже и левее
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(
                        new Slot(entityPlayer.inventory, k + i * 9 + 9, 26 + k * 18, 121 + i * 18));
            }
        }
        
        // Player hotbar (bottom row) - тоже сдвигаем на 1 пиксель ниже и левее
        for (int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(entityPlayer.inventory, j, 26 + j * 18, 179));
        }
    }
}