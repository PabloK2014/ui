package com.myaddon.iuaddon.events;

import com.denfop.tiles.base.TileEntityBaseLiquedMachine;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import com.myaddon.iuaddon.utils.WaterGeneratorUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;

public class WaterGeneratorEventHandler {
    
    // Reflection field for FluidTank capacity
    private static Field capacityField;
    
    static {
        try {
            capacityField = FluidTank.class.getDeclaredField("capacity");
            capacityField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        
        // Iterate directly over loaded tile entities to avoid caching issues across dimensions
        for (TileEntity te : event.world.loadedTileEntityList) {
            if (te instanceof TileEntityBaseLiquedMachine) {
                processWaterGeneration((TileEntityBaseLiquedMachine) te);
            } else if (te instanceof TileEntityOreWashing) {
                processIC2WaterGeneration((TileEntityOreWashing) te);
            }
        }
    }
    
    private void processWaterGeneration(TileEntityBaseLiquedMachine machine) {
        if (machine.fluidTank == null) return;
        
        if (!WaterGeneratorUtils.hasWaterGeneratorModule(machine)) return;
        
        for (FluidTank tank : machine.fluidTank) {
            if (tank != null) {
                // Check if it's a water tank or empty
                if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
                    
                    // 1. Force Capacity to 30,000
                    if (tank.getCapacity() < 300000) {
                        try {
                            if (capacityField != null) {
                                capacityField.setInt(tank, 300000);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    // 2. Instant Fill
                    if (tank.getFluidAmount() < tank.getCapacity()) {
                        FluidStack water = new FluidStack(FluidRegistry.WATER, tank.getCapacity());
                        tank.setFluid(water); // Force set fluid
                    }
                }
            }
        }
    }
    
    private void processIC2WaterGeneration(TileEntityOreWashing machine) {
        if (!hasWaterGeneratorModuleInIC2Machine(machine)) return;
        
        try {
            java.lang.reflect.Field fluidTankField = machine.getClass().getDeclaredField("fluidTank");
            fluidTankField.setAccessible(true);
            Object fluidTankObj = fluidTankField.get(machine);
            
            if (fluidTankObj instanceof FluidTank) {
                FluidTank tank = (FluidTank) fluidTankObj;
                
                if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
                    
                    // 1. Force Capacity to 30,000
                    if (tank.getCapacity() < 30000) {
                        try {
                            if (capacityField != null) {
                                capacityField.setInt(tank, 30000);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    // 2. Instant Fill
                    if (tank.getFluidAmount() < tank.getCapacity()) {
                        FluidStack water = new FluidStack(FluidRegistry.WATER, tank.getCapacity());
                        tank.setFluid(water);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private boolean hasWaterGeneratorModuleInIC2Machine(TileEntityOreWashing machine) {
        try {
            if (machine instanceof net.minecraft.inventory.IInventory) {
                net.minecraft.inventory.IInventory inventory = (net.minecraft.inventory.IInventory) machine;
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    net.minecraft.item.ItemStack stack = inventory.getStackInSlot(i);
                    if (stack != null && !stack.isEmpty()) {
                        if (stack.getItem() == com.myaddon.iuaddon.IUAddon.addonUpgradeModule) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
}