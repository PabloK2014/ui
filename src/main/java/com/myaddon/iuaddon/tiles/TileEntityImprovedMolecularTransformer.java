package com.myaddon.iuaddon.tiles;

import com.denfop.api.Recipes;
import com.denfop.api.recipe.MachineRecipe;
import com.denfop.tiles.base.TileEntityMolecularTransformer;
import com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer;
import com.myaddon.iuaddon.client.gui.GuiImprovedMolecularTransformer;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileEntityImprovedMolecularTransformer extends TileEntityMolecularTransformer {

    // Shadowing parent's inputSlot and outputSlot
    public InvSlot inputSlot; 
    public InvSlotOutput outputSlot;
    
    // Parallel processing state
    private MachineRecipe[] outputs = new MachineRecipe[12];
    private double[] progress = new double[12];
    private boolean[] active = new boolean[12];

    public TileEntityImprovedMolecularTransformer() {
        super(); // Initializes parent slots
        
        // Initialize our shadowed slots with 12 slots each
        this.outputSlot = new InvSlotOutput(this, "output", 12);
        this.inputSlot = new InvSlot(this, "input", InvSlot.Access.I, 12);
    }

    @Override
    public ContainerBase<? extends TileEntityMolecularTransformer> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerImprovedMolecularTransformer(entityPlayer, this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiImprovedMolecularTransformer(new ContainerImprovedMolecularTransformer(entityPlayer, this));
    }

    @Override
    public String getInventoryName() {
        return "Improved Molecular Transformer";
    }

    // ISidedInventory implementation
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        int[] slots = new int[24];
        for (int i = 0; i < 24; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return index >= 0 && index < 12; // Input slots 0-11
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index >= 12 && index < 24; // Output slots 12-23
    }
    
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index >= 0 && index < 12) return true; // Input
        if (index >= 12 && index < 24) return false; // Output
        return false;
    }
    
    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && 
               player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    // Logic

    @Override
    public void updateEntityServer() {
        boolean anyActive = false;

        // 1. Scan and update state for all slots
        for (int i = 0; i < 12; i++) {
            if (!active[i]) {
                // Try to start processing
                ItemStack stack = inputSlot.get(i);
                if (!stack.isEmpty()) {
                    MachineRecipe recipe = Recipes.recipes.getRecipeMachineOutput("molecular", false, stack);
                    if (recipe != null) {
                        // Check input count requirement
                        int requiredInput = getRequiredInputCount(recipe);
                        if (stack.getCount() < requiredInput) continue;

                        ItemStack result = recipe.getRecipe().output.items.get(0);
                        if (canAddToSlot(i, result)) {
                            outputs[i] = recipe;
                            active[i] = true;
                            anyActive = true;
                        }
                    }
                }
            } else {
                // Already processing
                ItemStack stack = inputSlot.get(i);
                MachineRecipe recipe = outputs[i];
                
                // Validate
                if (stack.isEmpty() || recipe == null) {
                    active[i] = false;
                    progress[i] = 0;
                    outputs[i] = null;
                    continue;
                }
                
                // Check input count requirement again
                int requiredInput = getRequiredInputCount(recipe);
                if (stack.getCount() < requiredInput) {
                    active[i] = false;
                    progress[i] = 0;
                    outputs[i] = null;
                    continue;
                }
                
                // Check output space again
                ItemStack result = recipe.getRecipe().output.items.get(0);
                if (!canAddToSlot(i, result)) {
                     // Output full, stall but keep progress? 
                     // Or reset? Usually stall.
                     continue;
                }

                anyActive = true;
                
                // Energy Logic
                // ... (handled below)
            }
        }
        
        setActive(anyActive);
        
        if (anyActive) {
            markDirty(); 
            
            int activeCount = 0;
            for(boolean b : active) if(b) activeCount++;
            
            if (activeCount > 0) {
                double availableEnergy = this.energy.getEnergy();
                double energyPerSlot = availableEnergy / activeCount;
                
                for (int i = 0; i < 12; i++) {
                    if (active[i]) {
                        MachineRecipe recipe = outputs[i];
                        double recipeTotalEnergy = recipe.getRecipe().output.metadata.getDouble("energy");
                        
                        double energyToUse = energyPerSlot; 
                        double remaining = recipeTotalEnergy - progress[i];
                        if (energyToUse > remaining) {
                            energyToUse = remaining;
                        }
                        
                        if (energyToUse > 0) {
                            this.energy.useEnergy(energyToUse);
                            progress[i] += energyToUse;
                            
                            if (progress[i] >= recipeTotalEnergy) {
                                int requiredInput = getRequiredInputCount(recipe);
                                operateOnce(i, recipe.getRecipe().output.items, requiredInput);
                                progress[i] = 0;
                                
                                // Check if we can continue
                                ItemStack stack = inputSlot.get(i);
                                if (stack.isEmpty() || stack.getCount() < requiredInput) {
                                    active[i] = false;
                                    outputs[i] = null;
                                }
                            }
                        }
                    }
                }
            }
        } else {
             if (this.energy.getEnergy() > 0) {
                 this.energy.setCapacity(2_000_000_000); 
             }
        }
    }
    
    private int getRequiredInputCount(MachineRecipe recipe) {
        try {
            return recipe.getRecipe().input.getInputs().get(0).getInputs().get(0).getCount();
        } catch (Exception e) {
            return 1; // Default to 1 if structure is unexpected
        }
    }
    
    private boolean canAddToSlot(int slotIndex, ItemStack stackToAdd) {
        ItemStack existing = outputSlot.get(slotIndex);
        if (existing.isEmpty()) return true;
        
        if (existing.isItemEqual(stackToAdd) && ItemStack.areItemStackTagsEqual(existing, stackToAdd)) {
            int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
            return existing.getCount() + stackToAdd.getCount() <= limit;
        }
        return false;
    }

    // Modified operateOnce to take slot index and input consume amount
    public void operateOnce(int slotIndex, List<ItemStack> processResult, int inputConsumeAmount) {
        ItemStack input = inputSlot.get(slotIndex);
        if (!input.isEmpty()) {
            input.shrink(inputConsumeAmount); 
            if (input.getCount() <= 0) {
                inputSlot.put(slotIndex, ItemStack.EMPTY);
            }
        }

        List<ItemStack> outputCopy = new ArrayList<>();
        for (ItemStack stack : processResult) {
            outputCopy.add(stack.copy());
        }
        
        ItemStack toAdd = outputCopy.get(0); 
        ItemStack existing = outputSlot.get(slotIndex);
        
        if (existing.isEmpty()) {
            outputSlot.put(slotIndex, toAdd);
        } else if (existing.isItemEqual(toAdd) && ItemStack.areItemStackTagsEqual(existing, toAdd)) {
            // Enforce limit strictly
            int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
            int newCount = Math.min(limit, existing.getCount() + toAdd.getCount());
            existing.setCount(newCount);
        }
    }
    
    @Override
    public void setOverclockRates() {
        // Set a very high capacity to allow massive energy input for parallel processing
        this.energy.setCapacity(2_000_000_000); // 2 Billion EU capacity
    }
    
    // IInventory overrides
    
    @Override
    public int getSizeInventory() {
        return 24; // 12 input + 12 output
    }

    @Override
    public boolean isEmpty() {
        return inputSlot.isEmpty() && outputSlot.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= 0 && index < 12) return inputSlot.get(index);
        if (index >= 12 && index < 24) return outputSlot.get(index - 12);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index >= 0 && index < 12) {
             ItemStack stack = inputSlot.get(index);
             if (!stack.isEmpty()) {
                 if (stack.getCount() <= count) {
                     inputSlot.put(index, ItemStack.EMPTY);
                     return stack;
                 } else {
                     return stack.splitStack(count);
                 }
             }
        } else if (index >= 12 && index < 24) {
             ItemStack stack = outputSlot.get(index - 12);
             if (!stack.isEmpty()) {
                 if (stack.getCount() <= count) {
                     outputSlot.put(index - 12, ItemStack.EMPTY);
                     return stack;
                 } else {
                     return stack.splitStack(count);
                 }
             }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index >= 0 && index < 12) {
            ItemStack stack = inputSlot.get(index);
            inputSlot.put(index, ItemStack.EMPTY);
            return stack;
        } else if (index >= 12 && index < 24) {
            ItemStack stack = outputSlot.get(index - 12);
            outputSlot.put(index - 12, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < 12) inputSlot.put(index, stack);
        else if (index >= 12 && index < 24) outputSlot.put(index - 12, stack);
    }
    
    @Override
    public void clear() {
        inputSlot.clear();
        outputSlot.clear();
    }
    
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }
    
    // NBT Handling for arrays
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        // We need to read our custom arrays. 
        // But MachineRecipe is not easily serializable?
        // Usually we just re-scan inputs on load or save progress.
        // Saving 'progress' array is important.
        // 'outputs' can be re-derived from inputs if we don't save it, 
        // but we might lose progress if recipe changed?
        // For simplicity, let's just save 'progress' array.
        // On load, 'outputs' will be null, so first update will re-scan.
        // If input is same, it resumes.
        
        for(int i=0; i<12; i++) {
            progress[i] = nbt.getDouble("progress" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        for(int i=0; i<12; i++) {
            nbt.setDouble("progress" + i, progress[i]);
        }
        return nbt;
    }
    
    // GUI Progress
    // We only have one 'guiProgress' field in parent.
    // We can use it to show average progress? Or max?
    // Or just cycle through active slots?
    // For now, let's show the progress of the first active slot we find, or 0.
    @Override
    public double getProgress() {
        for(int i=0; i<12; i++) {
            if (active[i] && outputs[i] != null) {
                double total = outputs[i].getRecipe().output.metadata.getDouble("energy");
                return total > 0 ? progress[i] / total : 0;
            }
        }
        return 0;
    }
}
