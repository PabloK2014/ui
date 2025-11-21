package com.myaddon.iuaddon.utils;

import com.myaddon.iuaddon.Config;
import com.myaddon.iuaddon.items.modules.EnumAddonUpgradeModules;
import com.myaddon.iuaddon.items.modules.ItemAddonUpgradeModule;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotUpgrade;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class WaterGeneratorUtils {
    
    /**
     * Проверяет наличие модуля генерации воды в любом слоте машины
     */
    public static boolean hasWaterGeneratorModule(TileEntity machine) {
        if (!(machine instanceof IInventory)) {
            return false;
        }
        
        IInventory inventory = (IInventory) machine;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                // Отладка - показываем все предметы в машине
                if (stack.getItem() instanceof ItemAddonUpgradeModule) {
                    System.out.println("IU Addon: Found water generator module in slot " + i + " of machine at " + machine.getPos());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Проверяет есть ли модуль генерации воды в слотах улучшений машины
     */
    public static boolean hasWaterGeneratorModule(InvSlotUpgrade upgradeSlot) {
        if (upgradeSlot == null) return false;
        
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack stack = upgradeSlot.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemAddonUpgradeModule) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Считает количество модулей генерации воды
     */
    public static int getWaterGeneratorModuleCount(InvSlotUpgrade upgradeSlot) {
        if (upgradeSlot == null) return 0;
        
        int count = 0;
        for (int i = 0; i < upgradeSlot.size(); i++) {
            ItemStack stack = upgradeSlot.get(i);
            if (!stack.isEmpty()) {
                // Проверяем наш модуль
                if (stack.getItem() instanceof ItemAddonUpgradeModule) {
                    if (stack.getItemDamage() == EnumAddonUpgradeModules.WATER_GENERATOR.id) {
                        count += stack.getCount();
                    }
                }
                // Также проверяем оригинальные модули IU
                if (stack.getItem() instanceof com.denfop.items.modules.ItemUpgradeModule) {
                    if (stack.hasDisplayName() && stack.getDisplayName().contains("Water Generator")) {
                        count += stack.getCount();
                    }
                }
            }
        }
        return count;
    }
    
    /**
     * Считает количество модулей генерации воды в любом слоте машины
     */
    public static int getWaterGeneratorModuleCount(TileEntity machine) {
        if (!(machine instanceof IInventory)) {
            return 0;
        }
        
        int count = 0;
        IInventory inventory = (IInventory) machine;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemAddonUpgradeModule) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Генерирует воду в резервуаре машины (универсальный метод)
     */
    public static boolean generateWater(FluidTank waterTank, TileEntity machine) {
        if (waterTank == null || !hasWaterGeneratorModule(machine)) {
            return false;
        }
        
        int moduleCount = getWaterGeneratorModuleCount(machine);
        if (moduleCount <= 0) {
            return false;
        }
        
        // Генерируем воду
        int waterToGenerate = Config.waterGeneratorRate * moduleCount;
        FluidStack water = new FluidStack(FluidRegistry.WATER, waterToGenerate);
        
        int filled = waterTank.fill(water, true);
        
        return filled > 0;
    }

    /**
     * Генерирует воду в резервуаре машины (старый метод для совместимости)
     */
    public static boolean generateWater(FluidTank waterTank, InvSlotUpgrade upgradeSlot, TileEntityInventory machine) {
        // Используем новый универсальный метод
        return generateWater(waterTank, machine);
    }
    
    /**
     * Проверяет может ли резервуар принять воду
     */
    public static boolean canAcceptWater(FluidTank tank) {
        if (tank == null) return false;
        
        FluidStack fluid = tank.getFluid();
        if (fluid == null) return true; // Пустой резервуар
        
        // Проверяем что в резервуаре вода и есть место
        return fluid.getFluid() == FluidRegistry.WATER && 
               tank.getFluidAmount() < tank.getCapacity();
    }
}