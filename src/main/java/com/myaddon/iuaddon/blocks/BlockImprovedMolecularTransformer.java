package com.myaddon.iuaddon.blocks;

import com.denfop.blocks.mechanism.BlockMolecular;
import com.denfop.tiles.base.TileEntityMolecularTransformer;
import com.myaddon.iuaddon.IUAddon;
import com.myaddon.iuaddon.tiles.TileEntityImprovedMolecularTransformer;
import ic2.core.block.ITeBlock;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.TeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class BlockImprovedMolecularTransformer extends Block {

    public BlockImprovedMolecularTransformer() {
        super(Material.IRON);
        setRegistryName(new ResourceLocation(IUAddon.MOD_ID, "improved_molecular_transformer"));
        setTranslationKey(IUAddon.MOD_ID + ".improved_molecular_transformer");
        setHardness(3.0f);
        setCreativeTab(ic2.core.IC2.tabIC2); // Or create our own tab
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityImprovedMolecularTransformer();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityImprovedMolecularTransformer) {
             playerIn.openGui(IUAddon.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}
