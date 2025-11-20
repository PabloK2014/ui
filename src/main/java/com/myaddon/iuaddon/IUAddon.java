package com.myaddon.iuaddon;

import com.myaddon.iuaddon.blocks.BlockImprovedMolecular;
import ic2.api.event.TeBlockFinalCallEvent;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TeBlockRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mod.EventBusSubscriber  // КРИТИЧНО: Это нужно для автоматической регистрации @SubscribeEvent методов
@Mod(
        modid = IUAddon.MOD_ID,
        name = IUAddon.MOD_NAME,
        version = IUAddon.VERSION,
        dependencies = "required-after:industrialupgrade"
)
public class IUAddon {

    public static final String MOD_ID = "iu_addon";
    public static final String MOD_NAME = "Industrial Upgrade Addon";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static IUAddon INSTANCE;
    
    // Блок будет получен из TeBlockRegistry после регистрации
    public static BlockTileEntity improvedMolecularTransformer;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("IU Addon: PreInit started");
    }

    // СТАТИЧЕСКИЙ метод для обработки события IC2
    @SubscribeEvent
    public static void onTeBlockFinalCall(TeBlockFinalCallEvent event) {
        System.out.println("IU Addon: TeBlockFinalCallEvent received!");
        
        // Register our block with IC2's TeBlockRegistry
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
        
        System.out.println("IU Addon: Registered BlockImprovedMolecular with IC2 TeBlockRegistry!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("IU Addon: Init started");
        
        // Получаем блок из TeBlockRegistry (IC2 уже создал его)
        improvedMolecularTransformer = TeBlockRegistry.get(BlockImprovedMolecular.IDENTITY);
        if (improvedMolecularTransformer != null) {
            improvedMolecularTransformer.setCreativeTab(com.denfop.IUCore.SSPTab);
            System.out.println("IU Addon: Retrieved block from TeBlockRegistry successfully!");
        } else {
            System.err.println("IU Addon: ERROR - Failed to retrieve block from TeBlockRegistry!");
        }
        
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
        System.out.println("IU Addon: Post-initialization complete!");
    }
}
