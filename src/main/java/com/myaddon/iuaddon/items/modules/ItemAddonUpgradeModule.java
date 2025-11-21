package com.myaddon.iuaddon.items.modules;

import com.myaddon.iuaddon.IUAddon;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        // Логика генерации воды будет в EventHandler
        return false;
    }

    @Override
    public java.util.Collection<ItemStack> onProcessEnd(ItemStack stack, IUpgradableBlock machine, java.util.Collection<ItemStack> output) {
        // Просто возвращаем исходный вывод без изменений
        return output;
    }
}