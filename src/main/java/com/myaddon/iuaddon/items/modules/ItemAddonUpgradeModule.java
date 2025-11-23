package com.myaddon.iuaddon.items.modules;

import com.myaddon.iuaddon.IUAddon;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ItemAddonUpgradeModule extends Item implements IUpgradeItem {

    protected static final String NAME = "water_generator_module";

    public ItemAddonUpgradeModule() {
        this.setCreativeTab(com.denfop.IUCore.tabssp1);
        this.setRegistryName(IUAddon.MOD_ID, NAME);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public void addInformation(
            final ItemStack stack,
            @Nullable final World worldIn,
            final List<String> tooltip,
            @Nonnull final ITooltipFlag flagIn
    ) {
        tooltip.add("§7Генерирует воду в жидкостных машинах");
        tooltip.add("§9Генерирует: §f" + (com.myaddon.iuaddon.Config.waterGeneratorRate / 1000) + " ведер/тик");
        tooltip.add("§cПотребление: §f+" + com.myaddon.iuaddon.Config.waterGeneratorEnergyConsumption + " EU/тик");
        tooltip.add("§6Установите в слот улучшений машины");
        
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public String getTranslationKey() {
        return "item." + IUAddon.MOD_ID + "." + NAME;
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            this,
            0,
            new ModelResourceLocation(IUAddon.MOD_ID + ":water_generator_module", null)
        );
    }

    // Методы интерфейса IUpgradeItem
    @Override
    public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> properties) {
        // Наш модуль подходит для всех машин с жидкостными процессами
        return properties.contains(UpgradableProperty.Processing) || 
               properties.contains(UpgradableProperty.Augmentable);
    }

    @Override
    public boolean onTick(ItemStack stack, IUpgradableBlock machine) {
        if (machine instanceof TileEntity) {
            TileEntity tile = (TileEntity) machine;
            
            // Try to find fluid tank via reflection or known interfaces
            try {
                // Check for standard 'fluidTank' field (common in IC2 and IU)
                java.lang.reflect.Field fluidTankField = null;
                try {
                    fluidTankField = tile.getClass().getField("fluidTank");
                } catch (NoSuchFieldException e) {
                    try {
                        fluidTankField = tile.getClass().getDeclaredField("fluidTank");
                    } catch (NoSuchFieldException e2) {
                        // Field not found
                    }
                }

                if (fluidTankField != null) {
                    fluidTankField.setAccessible(true);
                    Object tankObj = fluidTankField.get(tile);

                    if (tankObj instanceof net.minecraftforge.fluids.FluidTank) {
                        processTank((net.minecraftforge.fluids.FluidTank) tankObj);
                    } else if (tankObj instanceof net.minecraftforge.fluids.FluidTank[]) {
                        for (net.minecraftforge.fluids.FluidTank tank : (net.minecraftforge.fluids.FluidTank[]) tankObj) {
                            processTank(tank);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore errors to prevent crash
            }
        }
        return true; // Keep processing
    }

    private void processTank(net.minecraftforge.fluids.FluidTank tank) {
        if (tank == null) return;
        
        // Check if it's water or empty
        if (tank.getFluid() == null || tank.getFluid().getFluid() == net.minecraftforge.fluids.FluidRegistry.WATER) {
            
            // 1. Force Capacity to 300,000 (300 buckets)
            if (tank.getCapacity() < 300000) {
                try {
                    java.lang.reflect.Field capacityField = net.minecraftforge.fluids.FluidTank.class.getDeclaredField("capacity");
                    capacityField.setAccessible(true);
                    capacityField.setInt(tank, 300000);
                } catch (Exception e) {
                    // Ignore
                }
            }

            // 2. Instant Fill
            if (tank.getFluidAmount() < tank.getCapacity()) {
                tank.setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, tank.getCapacity()));
            }
        }
    }

    @Override
    public java.util.Collection<ItemStack> onProcessEnd(ItemStack stack, IUpgradableBlock machine, java.util.Collection<ItemStack> output) {
        // Просто возвращаем исходный вывод без изменений
        return output;
    }
}