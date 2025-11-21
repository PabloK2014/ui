package com.myaddon.iuaddon.tiles;

import com.denfop.tiles.base.TileEntityMolecularTransformer;
import com.denfop.api.recipe.InvSlotRecipes;
import com.denfop.api.recipe.MachineRecipe;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;

public class TileEntityImprovedMolecularTransformer extends TileEntityMolecularTransformer {

    public InvSlotRecipes[] inputSlots = new InvSlotRecipes[12];
    public InvSlotOutput[] outputSlots = new InvSlotOutput[12];
    public double[] progressPerSlot = new double[12];
    public MachineRecipe[] currentRecipes = new MachineRecipe[12];
    public double[] guiProgressPerSlot = new double[12];

    public TileEntityImprovedMolecularTransformer() {
        super();
        
        System.out.println("TileEntityImprovedMolecularTransformer CONSTRUCTOR START");
        this.operationsPerTick = 2;
        
        // Parent already creates energy component via:
        // this.energy = this.addComponent(AdvEnergy.asBasicSink(this, 0, 14).addManagedSlot(this.dischargeSlot));
        
        // Initialize 12 input and output slots
        for (int i = 0; i < 12; i++) {
            this.inputSlots[i] = new InvSlotRecipes(this, "molecular", this);
            // Size 1 for output slots
            this.outputSlots[i] = new InvSlotOutput(this, "output" + i, 1);
            this.progressPerSlot[i] = 0;
            this.guiProgressPerSlot[i] = 0;
        }
        
        // CRITICAL: Do NOT link this.inputSlot to our slots. 
        // The parent class uses this.inputSlot in its updateEntityServer logic (which we are bypassing),
        // but we want to ensure no accidental interference.
        // this.inputSlot is already initialized by parent constructor, so we leave it alone (it will be empty).
        
        System.out.println("TileEntityImprovedMolecularTransformer CONSTRUCTOR END");
    }

    @Override
    protected void onLoaded() {
        System.out.println("TileEntityImprovedMolecularTransformer onLoaded START");
        super.onLoaded();
        
        if (IC2.platform.isSimulating()) {
            // Load all input slots
            for (int i = 0; i < 12; i++) {
                this.inputSlots[i].load();
            }
            this.setOverclockRates();
        }
        
        System.out.println("TileEntityImprovedMolecularTransformer onLoaded END");
    }

    @Override
    public void setOverclockRates() {
        // Calculate total energy capacity needed for all slots with valid recipes
        double totalCapacity = 150000; // Minimum base capacity
        
        for (int i = 0; i < 12; i++) {
            try {
                MachineRecipe recipe = this.inputSlots[i].process();
                this.currentRecipes[i] = recipe;
                
                // If recipe exists and output can accept it, add to capacity requirement
                if (recipe != null && this.outputSlots[i].canAdd(recipe.getRecipe().output.items)) {
                    double energyNeeded = recipe.getRecipe().output.metadata.getDouble("energy");
                    totalCapacity += energyNeeded;
                }
            } catch (Exception e) {
                // Skip problematic slots
                this.currentRecipes[i] = null;
            }
        }
        
        // Set capacity dynamically, but ensure it's never less than base
        if (this.energy != null) {
            this.energy.setCapacity(Math.max(totalCapacity, 150000));
        }
        
        // Log capacity changes for debugging
        // System.out.println("TileEntityImprovedMolecularTransformer: setOverclockRates - capacity set to " + Math.max(totalCapacity, 150000));
    }

    @Override
    public void updateEntityServer() {
        // CRITICAL: Do NOT call super.updateEntityServer()
        // The parent TileEntityMolecularTransformer drains all energy if its own (empty) inputSlot has no recipe.
        // We must manually update the energy component and handle logic ourselves.
        
        // 1. Update Energy Component (handles discharge slot and internal state)
        if (this.energy != null) {
            this.energy.onWorldTick();
            this.guiChargeLevel = this.energy.getFillRatio();
        }

        // 2. Ensure capacity is correct (dynamic based on recipes)
        setOverclockRates();
        
        boolean needsUpdate = false;
        
        // Energy tracking removed for cleaner logs

        // 4. Process recipes for all 12 slots
        for (int i = 0; i < 12; i++) {
            MachineRecipe recipe = this.inputSlots[i].process();
            
            if (recipe != null) {
                if (this.outputSlots[i].canAdd(recipe.getRecipe().output.items)) {
                    double energyNeeded = recipe.getRecipe().output.metadata.getDouble("energy");
                    
                    if (this.energy.getEnergy() >= energyNeeded) {
                        // CRITICAL FIX: Check everything BEFORE consuming anything
                        try {
                            // Validate input slot has items to consume
                            if (this.inputSlots[i].isEmpty()) {
                                continue; // Skip if no input
                            }
                            
                            // Validate output can actually accept the items
                            if (!this.outputSlots[i].canAdd(recipe.getRecipe().output.items)) {
                                continue; // Skip if output full
                            }
                            
                            // All checks passed - now consume energy and process
                            this.energy.useEnergy(energyNeeded);
                            this.inputSlots[i].consume();
                            this.outputSlots[i].add(recipe.getRecipe().output.items);
                            needsUpdate = true;
                            
                        } catch (Exception e) {
                            // Log the error for debugging
                            System.err.println("Error processing slot " + i + " in ImprovedMolecularTransformer: " + e.getMessage());
                            e.printStackTrace();
                            // Don't restore energy here - if we got here, something is seriously wrong
                        }
                    }
                }
            }
        }
        
        if (needsUpdate) {
            markDirty();
            setOverclockRates();
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        try {
            for (int i = 0; i < 12; i++) {
                if (nbt.hasKey("inputSlot" + i)) {
                    this.inputSlots[i].readFromNbt(nbt.getCompoundTag("inputSlot" + i));
                }
                if (nbt.hasKey("outputSlot" + i)) {
                    this.outputSlots[i].readFromNbt(nbt.getCompoundTag("outputSlot" + i));
                }
                if (nbt.hasKey("progress" + i)) {
                    this.progressPerSlot[i] = nbt.getDouble("progress" + i);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading NBT for ImprovedMolecularTransformer: " + e.getMessage());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        try {
            for (int i = 0; i < 12; i++) {
                NBTTagCompound inputTag = new NBTTagCompound();
                this.inputSlots[i].writeToNbt(inputTag);
                nbt.setTag("inputSlot" + i, inputTag);
                
                NBTTagCompound outputTag = new NBTTagCompound();
                this.outputSlots[i].writeToNbt(outputTag);
                nbt.setTag("outputSlot" + i, outputTag);
                
                nbt.setDouble("progress" + i, this.progressPerSlot[i]);
            }
        } catch (Exception e) {
            System.err.println("Error writing NBT for ImprovedMolecularTransformer: " + e.getMessage());
        }
        return nbt;
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
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            setOverclockRates();
        }
    }
}
