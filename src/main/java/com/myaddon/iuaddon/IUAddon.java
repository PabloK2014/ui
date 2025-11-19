package com.myaddon.iuaddon;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(
        modid = IUAddon.MOD_ID,
        name = IUAddon.MOD_NAME,
        version = IUAddon.VERSION
)
public class IUAddon {

    public static final String MOD_ID = "iu_addon";
    public static final String MOD_NAME = "Industrial Upgrade Addon";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static IUAddon INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("IUADDON: PreInit starting");
        try {
            System.out.println("IUADDON: Testing ContainerBaseMolecular loading...");
            Class<?> containerClazz = Class.forName("com.denfop.container.ContainerBaseMolecular");
            System.out.println("IUADDON: ContainerBaseMolecular found: " + containerClazz.getName());

            System.out.println("IUADDON: Testing GuiMolecularTransformer loading...");
            Class<?> guiClazz = Class.forName("com.denfop.gui.GuiMolecularTransformer");
            System.out.println("IUADDON: GuiMolecularTransformer found: " + guiClazz.getName());
            
            System.out.println("IUADDON: Testing ContainerImprovedMolecularTransformer loading...");
            Class<?> myContainerClazz = Class.forName("com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer");
            System.out.println("IUADDON: My Container found: " + myContainerClazz.getName());

            System.out.println("IUADDON: Testing GuiImprovedMolecularTransformer loading...");
            Class<?> myGuiClazz = Class.forName("com.myaddon.iuaddon.client.gui.GuiImprovedMolecularTransformer");
            System.out.println("IUADDON: My GUI found: " + myGuiClazz.getName());

            System.out.println("IUADDON: Testing TileEntityImprovedMolecularTransformer loading...");
            Class<?> myClazz = Class.forName("com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer");
            System.out.println("IUADDON: My Class found: " + myClazz.getName());
            // Object instance = myClazz.newInstance(); // Skip instantiation for now, just check loading
            // System.out.println("IUADDON: Instance created: " + instance);
        } catch (Throwable t) {
            System.out.println("IUADDON: Failed to load class: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    public static class GuiHandler implements net.minecraftforge.fml.common.network.IGuiHandler {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) {
                return new com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer(player, (com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) te);
            }
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) {
                return new com.myaddon.iuaddon.client.gui.GuiImprovedMolecularTransformer(new com.myaddon.iuaddon.container.ContainerImprovedMolecularTransformer(player, (com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer) te));
            }
            return null;
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        
    }

    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new net.minecraft.item.ItemBlock(ModBlocks.IMPROVED_MOLECULAR_TRANSFORMER).setRegistryName(ModBlocks.IMPROVED_MOLECULAR_TRANSFORMER.getRegistryName()));
        }

        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(ModBlocks.IMPROVED_MOLECULAR_TRANSFORMER);
            GameRegistry.registerTileEntity(com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer.class, new net.minecraft.util.ResourceLocation(MOD_ID, "improved_molecular_transformer"));
        }
    }

    public static class ModBlocks {
        public static final Block IMPROVED_MOLECULAR_TRANSFORMER = new com.myaddon.iuaddon.blocks.BlockImprovedMolecularTransformer();
    }
}
