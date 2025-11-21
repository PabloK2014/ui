package com.myaddon.iuaddon;

import com.myaddon.iuaddon.blocks.BlockImprovedMolecular;
import com.myaddon.iuaddon.blocks.BlockMysticalGrower;
import com.myaddon.iuaddon.items.modules.ItemAddonUpgradeModule;
import ic2.api.event.TeBlockFinalCallEvent;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TeBlockRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.denfop.IUItem;
import com.denfop.Ic2Items;
import ic2.api.recipe.Recipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import com.myaddon.iuaddon.events.WaterGeneratorEventHandler;

@Mod.EventBusSubscriber  // КРИТИЧНО: Это нужно для автоматической регистрации @SubscribeEvent методов
@Mod(
        modid = IUAddon.MOD_ID,
        name = IUAddon.MOD_NAME,
        version = IUAddon.VERSION,
        dependencies = "required-after:industrialupgrade;required-after:mysticalagriculture"
)
public class IUAddon {

    public static final String MOD_ID = "iu_addon";
    public static final String MOD_NAME = "Industrial Upgrade Addon";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static IUAddon INSTANCE;
    
    // Блок будет получен из TeBlockRegistry после регистрации
    public static BlockTileEntity improvedMolecularTransformer;
    public static BlockTileEntity mysticalGrower;
    
    // Модули
    public static ItemAddonUpgradeModule addonUpgradeModule;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("IU Addon: PreInit started");
        
        // Загружаем конфигурацию
        Config.init(event);
        
        System.out.println("IU Addon: PreInit complete");
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        System.out.println("IU Addon: Registering items...");
        
        // Создаем и регистрируем модуль здесь
        addonUpgradeModule = new ItemAddonUpgradeModule();
        event.getRegistry().register(addonUpgradeModule);
        System.out.println("IU Addon: Registered addon upgrade module!");
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(net.minecraftforge.client.event.ModelRegistryEvent event) {
        System.out.println("IU Addon: Registering models...");
        
        if (addonUpgradeModule != null) {
            addonUpgradeModule.registerModels();
            System.out.println("IU Addon: Registered addon upgrade module models!");
        }
    }

    // СТАТИЧЕСКИЙ метод для обработки события IC2
    @SubscribeEvent
    public static void onTeBlockFinalCall(TeBlockFinalCallEvent event) {
        System.out.println("IU Addon: TeBlockFinalCallEvent received!");
        
        // Register Improved Molecular Transformer
        TeBlockRegistry.addAll(BlockImprovedMolecular.class, BlockImprovedMolecular.IDENTITY);
        TeBlockRegistry.setDefaultMaterial(BlockImprovedMolecular.IDENTITY, Material.IRON);
        TeBlockRegistry.addCreativeRegisterer((list, block, itemblock, tab) -> {
            if (tab == CreativeTabs.SEARCH || tab == com.denfop.IUCore.SSPTab) {
                block.getAllTypes().forEach(type -> {
                    if (type.hasItem()) {
                        list.add(block.getItemStack(type));
                    }
                });
            }
        }, BlockImprovedMolecular.IDENTITY);
        
        // Register Mystical Grower
        TeBlockRegistry.addAll(BlockMysticalGrower.class, BlockMysticalGrower.IDENTITY);
        TeBlockRegistry.setDefaultMaterial(BlockMysticalGrower.IDENTITY, Material.IRON);
        TeBlockRegistry.addCreativeRegisterer((list, block, itemblock, tab) -> {
            if (tab == CreativeTabs.SEARCH || tab == com.denfop.IUCore.SSPTab) {
                block.getAllTypes().forEach(type -> {
                    if (type.hasItem()) {
                        list.add(block.getItemStack(type));
                    }
                });
            }
        }, BlockMysticalGrower.IDENTITY);
        
        System.out.println("IU Addon: Registered blocks with IC2 TeBlockRegistry!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("IU Addon: Init started");
        
        // Получаем блоки из TeBlockRegistry (IC2 уже создал их)
        improvedMolecularTransformer = TeBlockRegistry.get(BlockImprovedMolecular.IDENTITY);
        if (improvedMolecularTransformer != null) {
            improvedMolecularTransformer.setCreativeTab(com.denfop.IUCore.SSPTab);
            System.out.println("IU Addon: Retrieved Improved Molecular Transformer successfully!");
        } else {
            System.err.println("IU Addon: ERROR - Failed to retrieve Improved Molecular Transformer!");
        }
        
        mysticalGrower = TeBlockRegistry.get(BlockMysticalGrower.IDENTITY);
        if (mysticalGrower != null) {
            mysticalGrower.setCreativeTab(com.denfop.IUCore.SSPTab);
            System.out.println("IU Addon: Retrieved Mystical Grower successfully!");
        } else {
            System.err.println("IU Addon: ERROR - Failed to retrieve Mystical Grower!");
        }
        
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        
        // Регистрируем обработчик событий для генерации воды
        MinecraftForge.EVENT_BUS.register(new WaterGeneratorEventHandler());
        System.out.println("IU Addon: Registered Water Generator Event Handler");
        
        // Регистрируем команду для тестирования
        net.minecraftforge.fml.common.event.FMLServerStartingEvent.class.cast(null); // Заглушка для импорта
        
        // Добавляем крафты
        addRecipes();
        
        System.out.println("IU Addon: Water Generator Module configured - Rate: " + Config.waterGeneratorRate + " mB/tick, Energy: " + Config.waterGeneratorEnergyConsumption + " EU/tick");
    }

    public static class GuiHandler implements net.minecraftforge.fml.common.network.IGuiHandler {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) {
                return new com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer(player, (com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) te);
            }
            if (te instanceof com.myaddon.iuaddon.tiles.TileEntityMysticalGrower) {
                return new com.myaddon.iuaddon.container.ContainerMysticalGrower(player, (com.myaddon.iuaddon.tiles.TileEntityMysticalGrower) te);
            }
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) {
                return new com.myaddon.iuaddon.client.gui.GuiImprovedMolecularTransformer(new com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer(player, (com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) te));
            }
            if (te instanceof com.myaddon.iuaddon.tiles.TileEntityMysticalGrower) {
                return new com.myaddon.iuaddon.client.gui.GuiMysticalGrower(new com.myaddon.iuaddon.container.ContainerMysticalGrower(player, (com.myaddon.iuaddon.tiles.TileEntityMysticalGrower) te));
            }
            return null;
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("IU Addon: Post-initialization complete!");
    }
    
    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        event.registerServerCommand(new com.myaddon.iuaddon.commands.WaterGeneratorCommand());
        System.out.println("IU Addon: Registered /watergen command");
    }
    
    private void addRecipes() {
        System.out.println("IU Addon: Adding recipes...");
        
        // Крафт Мистического Ростителя
        // Центр: улучшенный блок механизма (IUItem.basemachine, meta 3)
        // По бокам: 4 эссенции супремиума
        // Остальные слоты: 2 сжатых углепластика и 2 улучшенных электросхемы
        if (mysticalGrower != null) {
            Recipes.advRecipes.addRecipe(mysticalGrower.getItemStack(BlockMysticalGrower.mystical_grower),
                "ABA",
                "CDC", 
                "ABA",
                'A', OreDictionary.getOres("essenceSupremium"), // Эссенция супремиума по бокам
                'B', new ItemStack(IUItem.compresscarbon), // Сжатый углепластик сверху и снизу
                'C', Ic2Items.advancedCircuit, // Улучшенные электросхемы слева и справа
                'D', new ItemStack(IUItem.basemachine, 1, 3) // Улучшенный блок механизма в центре
            );
            System.out.println("IU Addon: Added Mystical Grower recipe");
        }
        
        // Крафт Улучшенного Молекулярного Преобразователя
        // Основа: обычный молекулярный трансформер в центре
        // Углы: 4 иридиевые пластины
        // Стороны: 2 квантовые схемы сверху/снизу, 2 сжатых углепластика слева/справа
        if (improvedMolecularTransformer != null) {
            Recipes.advRecipes.addRecipe(improvedMolecularTransformer.getItemStack(BlockImprovedMolecular.improved_molecular),
                "ABA",
                "CDC",
                "ABA", 
                'A', Ic2Items.iridiumPlate, // Иридиевые пластины по углам
                'B', IUItem.QuantumItems9, // Квантовые схемы сверху и снизу
                'C', new ItemStack(IUItem.compresscarbon), // Сжатый углепластик слева и справа
                'D', new ItemStack(IUItem.blockmolecular) // Обычный молекулярный трансформер в центре
            );
            System.out.println("IU Addon: Added Improved Molecular Transformer recipe");
        }
        
        // Крафт модуля генерации воды
        // Центр: ведро воды
        // Углы: 4 водяные ячейки
        // Стороны: 2 улучшенные схемы, 2 улучшенных теплообменника
        if (addonUpgradeModule != null) {
            Recipes.advRecipes.addRecipe(new ItemStack(addonUpgradeModule, 1, 0),
                "ABA",
                "CDC",
                "ABA",
                'A', Ic2Items.waterCell, // Водяные ячейки по углам
                'B', new ItemStack(IUItem.compresscarbon), // Сжатый углепластик сверху и снизу
                'C', Ic2Items.advancedCircuit, // Улучшенные схемы слева и справа
                'D', net.minecraft.init.Items.WATER_BUCKET // Ведро воды в центре
            );
            System.out.println("IU Addon: Added Water Generator Module recipe");
        }
        
        System.out.println("IU Addon: All recipes added successfully!");
    }
}
