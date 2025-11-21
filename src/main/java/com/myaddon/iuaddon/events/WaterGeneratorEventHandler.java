package com.myaddon.iuaddon.events;

import com.denfop.tiles.base.TileEntityBaseLiquedMachine;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import com.myaddon.iuaddon.utils.WaterGeneratorUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;

public class WaterGeneratorEventHandler {
    
    // Кэш машин для оптимизации
    private static final List<TileEntityBaseLiquedMachine> liquidMachines = new ArrayList<>();
    private static final List<TileEntityOreWashing> ic2Machines = new ArrayList<>();
    private static int tickCounter = 0;
    private static boolean initialized = false;
    
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        
        // Отладка - показываем что EventHandler работает
        if (!initialized) {
            System.out.println("IU Addon: WaterGeneratorEventHandler is working!");
            initialized = true;
        }
        
        // Обновляем кэш каждые 100 тиков (5 секунд)
        tickCounter++;
        if (tickCounter >= 100) {
            tickCounter = 0;
            updateMachineCache(event.world);
            System.out.println("IU Addon: Found " + liquidMachines.size() + " IU liquid machines and " + ic2Machines.size() + " IC2 machines");
        }
        
        // Обрабатываем все жидкостные машины IU
        for (TileEntityBaseLiquedMachine machine : liquidMachines) {
            if (machine.isInvalid() || machine.getWorld() != event.world) {
                continue;
            }
            
            try {
                processWaterGeneration(machine);
            } catch (Exception e) {
                // Игнорируем ошибки чтобы не крашить игру
                System.err.println("IU Addon: Error processing water generation for IU machine at " + 
                    machine.getPos() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Обрабатываем все IC2 машины
        for (TileEntityOreWashing machine : ic2Machines) {
            if (machine.isInvalid() || machine.getWorld() != event.world) {
                continue;
            }
            
            try {
                processIC2WaterGeneration(machine);
            } catch (Exception e) {
                // Игнорируем ошибки чтобы не крашить игру
                System.err.println("IU Addon: Error processing water generation for IC2 machine at " + 
                    machine.getPos() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void updateMachineCache(net.minecraft.world.World world) {
        liquidMachines.clear();
        ic2Machines.clear();
        
        // Собираем все загруженные жидкостные машины
        for (TileEntity te : world.loadedTileEntityList) {
            if (te instanceof TileEntityBaseLiquedMachine) {
                liquidMachines.add((TileEntityBaseLiquedMachine) te);
            } else if (te instanceof TileEntityOreWashing) {
                ic2Machines.add((TileEntityOreWashing) te);
            }
        }
    }
    
    private void processWaterGeneration(TileEntityBaseLiquedMachine machine) {
        if (machine.fluidTank == null) {
            return;
        }
        
        // Проверяем наличие нашего модуля в машине
        if (!WaterGeneratorUtils.hasWaterGeneratorModule(machine)) {
            return;
        }
        
        // Отладочное сообщение только раз в 20 тиков
        if (tickCounter % 20 == 0) {
            System.out.println("IU Addon: Found water generator module in machine at " + machine.getPos());
        }
        
        // Ищем резервуар с водой или пустой резервуар
        for (int i = 0; i < machine.fluidTank.length; i++) {
            FluidTank tank = machine.fluidTank[i];
            if (tank != null && WaterGeneratorUtils.canAcceptWater(tank)) {
                // Проверяем что это резервуар для воды
                if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
                    boolean success = WaterGeneratorUtils.generateWater(tank, machine);
                    if (success && tickCounter % 20 == 0) {
                        System.out.println("IU Addon: Generated water in tank " + i + 
                            " (current: " + (tank.getFluid() != null ? tank.getFluid().amount : 0) + 
                            "/" + tank.getCapacity() + ")");
                    }
                    break; // Генерируем только в один резервуар
                }
            }
        }
    }
    
    private void processIC2WaterGeneration(TileEntityOreWashing machine) {
        // Проверяем наличие нашего модуля в инвентаре машины
        if (!hasWaterGeneratorModuleInIC2Machine(machine)) {
            return;
        }
        
        try {
            // Получаем доступ к флюид танку через рефлексию
            java.lang.reflect.Field fluidTankField = machine.getClass().getDeclaredField("fluidTank");
            fluidTankField.setAccessible(true);
            Object fluidTankObj = fluidTankField.get(machine);
            
            if (fluidTankObj instanceof FluidTank) {
                FluidTank tank = (FluidTank) fluidTankObj;
                
                // Проверяем что танк может принять воду
                if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
                    if (tank.getFluidAmount() < tank.getCapacity()) {
                        // Генерируем воду согласно конфигу
                        net.minecraftforge.fluids.FluidStack water = new net.minecraftforge.fluids.FluidStack(FluidRegistry.WATER, com.myaddon.iuaddon.Config.waterGeneratorRate);
                        int filled = tank.fill(water, true);
                        
                        if (filled > 0 && tickCounter % 100 == 0) {
                            System.out.println("IU Addon: Generated " + filled + " mB water in IC2 Ore Washing Plant at " + machine.getPos() + " (has module)");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки рефлексии
        }
    }
    
    private boolean hasWaterGeneratorModuleInIC2Machine(TileEntityOreWashing machine) {
        try {
            // Пытаемся получить доступ к инвентарю машины
            // IC2 машины обычно имеют поле inventory или implements IInventory
            
            if (machine instanceof net.minecraft.inventory.IInventory) {
                net.minecraft.inventory.IInventory inventory = (net.minecraft.inventory.IInventory) machine;
                
                // Проверяем все слоты на наличие нашего модуля
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
    
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Очищаем кэш при выгрузке мира
        liquidMachines.clear();
        ic2Machines.clear();
        tickCounter = 0;
    }
}