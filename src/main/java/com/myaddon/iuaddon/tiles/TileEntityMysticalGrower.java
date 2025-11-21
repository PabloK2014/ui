package com.myaddon.iuaddon.tiles;

import com.myaddon.iuaddon.container.ContainerMysticalGrower;
import com.denfop.tiles.base.TileEntityMolecularTransformer;
import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IC2;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.blakebr0.mysticalagriculture.items.ItemSeed;
import com.blakebr0.mysticalagriculture.blocks.BlockStorage;
import net.minecraft.block.Block;
import java.util.List;

public class TileEntityMysticalGrower extends TileEntityMolecularTransformer {

    public final InvSlot seedSlot;
    public final InvSlot acceleratorSlot;
    public int progress = 0;
    public int maxProgress = 1500;
    public final int baseMaxProgress = 100; // Уменьшили в 15 раз (1500 -> 100)
    public final int energyPerTick = 100;
    
    public TileEntityMysticalGrower() {
        super(); // Call parent constructor
        
        System.out.println("TileEntityMysticalGrower CONSTRUCTOR START");
        
        // Parent already creates energy component and outputSlot
        
        // Add custom slots with unique names
        this.seedSlot = new InvSlot(this, "mystical_seed", InvSlot.Access.I, 1);
        this.acceleratorSlot = new InvSlot(this, "mystical_accelerator", InvSlot.Access.I, 1);
        
        System.out.println("TileEntityMysticalGrower CONSTRUCTOR END");
    }





    @Override
    protected void onLoaded() {
        System.out.println("TileEntityMysticalGrower onLoaded START");
        super.onLoaded();
        System.out.println("TileEntityMysticalGrower onLoaded END");
    }
    
    @Override
    public List<String> getNetworkedFields() {
        List<String> ret = super.getNetworkedFields();
        ret.add("guiProgress");
        ret.add("progress");
        ret.add("maxProgress");
        return ret;
    }

    @Override
    public void updateEntityServer() {
        // НЕ вызываем super.updateEntityServer() - он мешает нашей логике
        
        // Обновляем энергетический компонент вручную
        if (this.energy != null) {
            this.energy.onWorldTick();
            this.guiChargeLevel = this.energy.getFillRatio();
        }
        
        ItemStack seed = seedSlot.get();
        ItemStack accelerator = acceleratorSlot.get();
        
        // Отладочная информация (можно убрать позже)
        // System.out.println("=== MYSTICAL GROWER UPDATE ===");
        // System.out.println("Seed: " + (!seed.isEmpty() ? seed.getDisplayName() : "EMPTY"));
        // System.out.println("Accelerator: " + (!accelerator.isEmpty() ? accelerator.getDisplayName() : "EMPTY"));
        
        // Проверяем входные предметы
        if (seed.isEmpty() || !isMASeed(seed)) {
            progress = 0;
            setActive(false);
            return;
        }
        
        if (accelerator.isEmpty() || !isEssenceBlock(accelerator)) {
            progress = 0;
            setActive(false);
            return;
        }
        
        ItemStack result = getEssenceForSeed(seed);
        if (result.isEmpty()) {
            progress = 0;
            setActive(false);
            return;
        }
        
        if (!outputSlot.canAdd(result)) {
            setActive(false);
            return;
        }
        
        // Вычисляем параметры процесса
        double speedMultiplier = getSpeedMultiplier(accelerator);
        maxProgress = (int) (baseMaxProgress / speedMultiplier);
        
        // Устанавливаем фиксированную емкость энергии (как в родительском классе)
        if (energy.getCapacity() == 0) {
            energy.setCapacity(150000); // Фиксированная емкость как у других машин IU
        }
        
        // Проверяем, есть ли энергия для работы
        if (energy.getEnergy() >= energyPerTick) {
            // Потребляем энергию и увеличиваем прогресс
            energy.useEnergy(energyPerTick);
            progress++;
            setActive(true);
            
            // System.out.println("Progress: " + progress + "/" + maxProgress + " Energy: " + energy.getEnergy());
            
            if (progress >= maxProgress) {
                // Процесс завершен
                progress = 0;
                
                ItemStack essenceDrop = result.copy();
                essenceDrop.setCount(1 + world.rand.nextInt(3));
                
                outputSlot.add(essenceDrop);
                
                // НЕ потребляем входные предметы - они работают как шаблоны
                // Семена и блоки эссенции остаются в слотах
            }
        } else {
            // Недостаточно энергии
            setActive(false);
        }
        
        // Обновляем прогресс для GUI
        if (maxProgress > 0) {
            guiProgress = (double) progress / maxProgress;
        } else {
            guiProgress = 0;
        }
        
        // Принудительно обновляем GUI
        if (IC2.platform.isSimulating()) {
            IC2.network.get(true).updateTileEntityField(this, "guiProgress");
            IC2.network.get(true).updateTileEntityField(this, "progress");
            IC2.network.get(true).updateTileEntityField(this, "maxProgress");
        }
    }
    
    private boolean isMASeed(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemSeed;
    }
    
    private boolean isEssenceBlock(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Block block = Block.getBlockFromItem(stack.getItem());
        return block instanceof BlockStorage;
    }
    
    public double getSpeedMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 1.0;
        
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof BlockStorage)) {
            return 1.0;
        }
        
        int meta = stack.getMetadata();
        
        switch (meta) {
            case 0: return 1.0;
            case 1: return 1.2;
            case 2: return 1.4;
            case 3: return 1.6;
            case 4: return 2.0;
            default: return 1.0;
        }
    }
    
    public int getProgressPercent() {
        if (maxProgress <= 0) return 0;
        return Math.min(100, (progress * 100) / maxProgress);
    }
    
    public double getGuiProgress() {
        if (maxProgress <= 0) return 0;
        return (double) progress / maxProgress;
    }
    
    public int getCurrentProgress() {
        return progress;
    }
    
    public int getMaxProgress() {
        return maxProgress;
    }
    
    private ItemStack getEssenceForSeed(ItemStack seedStack) {
        if (seedStack.isEmpty() || !(seedStack.getItem() instanceof ItemSeed)) {
            return ItemStack.EMPTY;
        }

        ItemSeed seed = (ItemSeed) seedStack.getItem();
        String seedRegistryName = seed.getRegistryName().toString();
        
        // Специальная обработка для семян инферия (tier1, tier2, tier3, tier4, tier5)
        if (seedRegistryName.contains("inferium_seeds")) {
            try {
                // Все эссенции инферия - это один предмет "crafting" с разными meta значениями
                Item craftingItem = net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValue(new ResourceLocation("mysticalagriculture", "crafting"));
                
                if (craftingItem != null) {
                    ItemStack essenceStack = null;
                    
                    if (seedRegistryName.contains("tier1")) {
                        essenceStack = new ItemStack(craftingItem, 1, 0); // meta 0 = inferium_essence
                    } else if (seedRegistryName.contains("tier2")) {
                        essenceStack = new ItemStack(craftingItem, 1, 1); // meta 1 = prudentium_essence
                    } else if (seedRegistryName.contains("tier3")) {
                        essenceStack = new ItemStack(craftingItem, 1, 2); // meta 2 = intermedium_essence
                    } else if (seedRegistryName.contains("tier4")) {
                        essenceStack = new ItemStack(craftingItem, 1, 3); // meta 3 = superium_essence
                    } else if (seedRegistryName.contains("tier5")) {
                        essenceStack = new ItemStack(craftingItem, 1, 4); // meta 4 = supremium_essence
                    } else {
                        // Обычные семена инферия без tier - возвращаем базовую эссенцию
                        essenceStack = new ItemStack(craftingItem, 1, 0); // meta 0 = inferium_essence
                    }
                    
                    if (essenceStack != null) {
                        return essenceStack;
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки
            }
        }
        
        // Обычная обработка для других семян через CropType.Type
        for (com.blakebr0.mysticalagriculture.lib.CropType.Type type : com.blakebr0.mysticalagriculture.lib.CropType.Type.values()) {
            Item typeSeed = type.getSeed();
            
            if (typeSeed != null && typeSeed.getRegistryName() != null) {
                if (seedRegistryName.equals(typeSeed.getRegistryName().toString())) {
                    return new ItemStack(type.getCrop());
                }
            }
        }
        
        return ItemStack.EMPTY;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getInteger("progress");
        this.maxProgress = nbt.getInteger("maxProgress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", this.progress);
        nbt.setInteger("maxProgress", this.maxProgress);
        return nbt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return new com.myaddon.iuaddon.client.gui.GuiMysticalGrower(new ContainerMysticalGrower(player, this));
    }

    @Override
    public ContainerBase<? extends TileEntityMolecularTransformer> getGuiContainer(EntityPlayer player) {
        return new ContainerMysticalGrower(player, this);
    }

    @Override
    public void onGuiClosed(EntityPlayer player) {
        // No special handling needed when GUI closes
    }



    public String getInventoryName() {
        return "Mystical Grower";
    }
    
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) return isMASeed(stack);
        if (index == 1) return isEssenceBlock(stack);
        return false;
    }
    
    // ISidedInventory implementation
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { 0, 1, 2 }; // 0=seed, 1=accelerator, 2=output
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        if (index == 0) return isMASeed(itemStackIn);
        if (index == 1) return isEssenceBlock(itemStackIn);
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == 2; // Only extract from output
    }

    @Override
    public void clear() {
        this.seedSlot.clear();
        this.acceleratorSlot.clear();
        this.outputSlot.clear();
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public String getName() {
        return "Mystical Grower";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return this.seedSlot.get().isEmpty() && this.acceleratorSlot.get().isEmpty() && this.outputSlot.get().isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index == 0) {
            return this.seedSlot.get();
        } else if (index == 1) {
            return this.acceleratorSlot.get();
        } else if (index == 2) {
            return this.outputSlot.get();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack result;
        if (stack.getCount() <= count) {
            result = stack.copy();
            if (index == 0) {
                this.seedSlot.clear();
            } else if (index == 1) {
                this.acceleratorSlot.clear();
            } else if (index == 2) {
                this.outputSlot.clear();
            }
        } else {
            result = stack.splitStack(count);
            if (index == 0) {
                this.seedSlot.put(stack);
            } else if (index == 1) {
                this.acceleratorSlot.put(stack);
            } else if (index == 2) {
                this.outputSlot.put(stack);
            }
        }
        
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index == 0) {
            ItemStack stack = this.seedSlot.get();
            this.seedSlot.clear();
            return stack;
        } else if (index == 1) {
            ItemStack stack = this.acceleratorSlot.get();
            this.acceleratorSlot.clear();
            return stack;
        } else if (index == 2) {
            ItemStack stack = this.outputSlot.get();
            this.outputSlot.clear();
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == 0) {
            this.seedSlot.put(stack);
        } else if (index == 1) {
            this.acceleratorSlot.put(stack);
        } else if (index == 2) {
            this.outputSlot.put(stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && 
               player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }
}
