package com.myaddon.iuaddon.commands;

import com.denfop.tiles.base.TileEntityBaseLiquedMachine;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import com.myaddon.iuaddon.utils.WaterGeneratorUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class WaterGeneratorCommand extends CommandBase {

    @Override
    public String getName() {
        return "watergen";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/watergen - Diagnose and test water generator modules";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("This command can only be used by players"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        World world = player.world;
        
        player.sendMessage(new TextComponentString("§6=== Water Generator Diagnostic ==="));
        
        // Проверяем регистрацию модуля
        try {
            if (com.myaddon.iuaddon.IUAddon.addonUpgradeModule != null) {
                player.sendMessage(new TextComponentString("§a✓ Water Generator Module is registered"));
                player.sendMessage(new TextComponentString("§7  Registry name: " + com.myaddon.iuaddon.IUAddon.addonUpgradeModule.getRegistryName()));
            } else {
                player.sendMessage(new TextComponentString("§c✗ Water Generator Module is null!"));
                return;
            }
        } catch (Exception e) {
            player.sendMessage(new TextComponentString("§c✗ Water Generator Module registration failed!"));
            player.sendMessage(new TextComponentString("§c  Error: " + e.getMessage()));
            return;
        }
        
        // Ищем все жидкостные машины в радиусе 10 блоков
        BlockPos playerPos = player.getPosition();
        int found = 0;
        int withModules = 0;
        int waterAdded = 0;
        
        player.sendMessage(new TextComponentString("§7Scanning 21x21x21 area around player..."));
        
        // Сначала покажем ВСЕ TileEntity для отладки
        int totalTileEntities = 0;
        
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    TileEntity te = world.getTileEntity(pos);
                    
                    if (te != null) {
                        totalTileEntities++;
                        String className = te.getClass().getSimpleName();
                        String fullClassName = te.getClass().getName();
                        
                        // Показываем все TileEntity для отладки
                        player.sendMessage(new TextComponentString("§9Debug: TileEntity at " + pos + " - " + className));
                        player.sendMessage(new TextComponentString("§8  Full class: " + fullClassName));
                        
                        // Проверяем является ли это жидкостной машиной
                        boolean isLiquidMachine = false;
                        
                        if (te instanceof TileEntityBaseLiquedMachine) {
                            found++;
                            isLiquidMachine = true;
                            TileEntityBaseLiquedMachine machine = (TileEntityBaseLiquedMachine) te;
                            
                            player.sendMessage(new TextComponentString("§a✓ This is an IU liquid machine!"));
                            
                            if (WaterGeneratorUtils.hasWaterGeneratorModule(machine)) {
                                withModules++;
                                player.sendMessage(new TextComponentString("§a  ✓ Has water generator module!"));
                                
                                // Пытаемся сгенерировать воду
                                if (machine.fluidTank != null && machine.fluidTank.length > 0) {
                                    for (int i = 0; i < machine.fluidTank.length; i++) {
                                        if (machine.fluidTank[i] != null) {
                                            // Показываем текущее состояние танка
                                            FluidStack current = machine.fluidTank[i].getFluid();
                                            int capacity = machine.fluidTank[i].getCapacity();
                                            
                                            if (current != null) {
                                                player.sendMessage(new TextComponentString("§7  Tank " + i + ": " + current.amount + "/" + capacity + " mB of " + current.getLocalizedName()));
                                            } else {
                                                player.sendMessage(new TextComponentString("§7  Tank " + i + ": empty (" + capacity + " mB capacity)"));
                                            }
                                            
                                            // Принудительно добавляем воду для теста
                                            FluidStack water = new FluidStack(FluidRegistry.WATER, 1000);
                                            int filled = machine.fluidTank[i].fill(water, true);
                                            
                                            if (filled > 0) {
                                                waterAdded += filled;
                                                player.sendMessage(new TextComponentString("§a  ✓ Added " + filled + " mB water to tank " + i));
                                            } else {
                                                player.sendMessage(new TextComponentString("§c  ✗ Could not add water (tank full or incompatible)"));
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    player.sendMessage(new TextComponentString("§c  ✗ Machine has no fluid tanks!"));
                                }
                            } else {
                                player.sendMessage(new TextComponentString("§c  ✗ No water generator module"));
                            }
                        } else if (te instanceof TileEntityOreWashing) {
                            found++;
                            isLiquidMachine = true;
                            TileEntityOreWashing oreWashing = (TileEntityOreWashing) te;
                            
                            player.sendMessage(new TextComponentString("§a✓ This is an IC2 Ore Washing Plant!"));
                            
                            // Проверяем наличие нашего модуля в инвентаре IC2 машины
                            boolean hasModule = checkIC2MachineForModule(oreWashing);
                            if (hasModule) {
                                withModules++;
                                player.sendMessage(new TextComponentString("§a  ✓ Has water generator module in inventory!"));
                            } else {
                                player.sendMessage(new TextComponentString("§c  ✗ No water generator module found in inventory"));
                            }
                            
                            // Добавляем воду принудительно для теста (независимо от модуля)
                            try {
                                // Пытаемся получить доступ к флюид танку через рефлексию
                                java.lang.reflect.Field fluidTankField = oreWashing.getClass().getDeclaredField("fluidTank");
                                fluidTankField.setAccessible(true);
                                Object fluidTankObj = fluidTankField.get(oreWashing);
                                
                                if (fluidTankObj instanceof net.minecraftforge.fluids.FluidTank) {
                                    net.minecraftforge.fluids.FluidTank tank = (net.minecraftforge.fluids.FluidTank) fluidTankObj;
                                    
                                    FluidStack current = tank.getFluid();
                                    int capacity = tank.getCapacity();
                                    
                                    if (current != null) {
                                        player.sendMessage(new TextComponentString("§7  Tank: " + current.amount + "/" + capacity + " mB of " + current.getLocalizedName()));
                                    } else {
                                        player.sendMessage(new TextComponentString("§7  Tank: empty (" + capacity + " mB capacity)"));
                                    }
                                    
                                    // Принудительно добавляем воду для теста
                                    FluidStack water = new FluidStack(FluidRegistry.WATER, 1000);
                                    int filled = tank.fill(water, true);
                                    
                                    if (filled > 0) {
                                        waterAdded += filled;
                                        player.sendMessage(new TextComponentString("§a  ✓ Added " + filled + " mB water to IC2 machine"));
                                    } else {
                                        player.sendMessage(new TextComponentString("§c  ✗ Could not add water (tank full or incompatible)"));
                                    }
                                } else {
                                    player.sendMessage(new TextComponentString("§c  ✗ Could not access fluid tank"));
                                }
                            } catch (Exception e) {
                                player.sendMessage(new TextComponentString("§c  ✗ Error accessing IC2 machine: " + e.getMessage()));
                            }
                        } else {
                            // Проверяем другие возможные типы жидкостных машин
                            if (fullClassName.toLowerCase().contains("liquid") || 
                                fullClassName.toLowerCase().contains("fluid") ||
                                fullClassName.toLowerCase().contains("tank") ||
                                fullClassName.toLowerCase().contains("wash")) {
                                player.sendMessage(new TextComponentString("§e  ? Might be a liquid machine (contains liquid/fluid/tank/wash in name)"));
                            }
                        }
                    }
                }
            }
        }
        
        player.sendMessage(new TextComponentString("§7Total TileEntities found: " + totalTileEntities));
        
        player.sendMessage(new TextComponentString("§6=== Results ==="));
        player.sendMessage(new TextComponentString("§7Found " + found + " liquid machines total"));
        player.sendMessage(new TextComponentString("§7Found " + withModules + " machines with water generator modules"));
        player.sendMessage(new TextComponentString("§7Added " + waterAdded + " mB water total"));
        
        if (found == 0) {
            player.sendMessage(new TextComponentString("§eNo liquid machines found nearby. Try placing an Ore Washing Plant or similar machine."));
        } else if (withModules == 0) {
            player.sendMessage(new TextComponentString("§eNo water generator modules found. Make sure to install the module in a machine."));
        }
    }
    
    private boolean checkIC2MachineForModule(TileEntityOreWashing machine) {
        try {
            // Проверяем через IInventory интерфейс
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
            
            // Альтернативный способ через рефлексию
            try {
                java.lang.reflect.Field inventoryField = machine.getClass().getDeclaredField("inventory");
                inventoryField.setAccessible(true);
                Object inventoryObj = inventoryField.get(machine);
                
                if (inventoryObj instanceof net.minecraft.item.ItemStack[]) {
                    net.minecraft.item.ItemStack[] inventory = (net.minecraft.item.ItemStack[]) inventoryObj;
                    
                    for (net.minecraft.item.ItemStack stack : inventory) {
                        if (stack != null && !stack.isEmpty()) {
                            if (stack.getItem() == com.myaddon.iuaddon.IUAddon.addonUpgradeModule) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки рефлексии
            }
            
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        return false;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Любой игрок может использовать
    }
}