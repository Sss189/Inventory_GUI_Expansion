package com.sss.InventoryGUIExpansion.Item;

import com.sss.InventoryGUIExpansion.InventoryGUIExpansion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCompressedSmelting extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final BlockCompressedSmelting INSTANCE = new BlockCompressedSmelting();
    public static final int GUI_ID = 1;

    // ================== 碰撞箱定义 ==================
    // 依据 JSON 模型计算:
    // 高度: 0-10 像素 -> 0.0D 到 0.625D (10/16)
    // 原始朝向 (南北): X宽(2-14像素), Z深(4-12像素)
    // 旋转朝向 (东西): X深(4-12像素), Z宽(2-14像素)

    // 南北向 (North/South) - X轴较宽
    // X: 2/16(0.125) ~ 14/16(0.875)
    // Z: 4/16(0.25)  ~ 12/16(0.75)
    protected static final AxisAlignedBB AABB_NS = new AxisAlignedBB(0.125D, 0.0D, 0.25D, 0.875D, 0.625D, 0.75D);

    // 东西向 (East/West) - Z轴较宽 (旋转90度)
    // X: 4/16(0.25)  ~ 12/16(0.75)
    // Z: 2/16(0.125) ~ 14/16(0.875)
    protected static final AxisAlignedBB AABB_EW = new AxisAlignedBB(0.25D, 0.0D, 0.125D, 0.75D, 0.625D, 0.875D);
    // ===============================================

    public BlockCompressedSmelting() {
        super(Material.WOOD);
        this.setRegistryName("compressed_smelting_station");
        this.setTranslationKey("compressed_smelting_station");
        this.setCreativeTab(CreativeTabs.MISC);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(InventoryGUIExpansion.instance, GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * 根据方块的朝向 (FACING) 返回对应的碰撞箱
     */
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        // 如果朝向是东或西，使用 EW 碰撞箱，否则使用 NS 碰撞箱
        if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
            return AABB_EW;
        } else {
            return AABB_NS;
        }
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        // 由于底部不是完整的 16x16，严格来说这里也可以设为 UNDEFINED，
        // 但如果想要允许把火把插在上面，或者让它放在下面，保留 CENTER 或 SOLID 视需求而定。
        // 鉴于模型底部是 8x8 (Element 1)，设为 CENTER 比较安全，或者保持 SOLID 如果你想让它上面能放东西。
        return face == EnumFacing.DOWN ? BlockFaceShape.CENTER : BlockFaceShape.UNDEFINED;
    }

    @Mod.EventBusSubscriber(modid = InventoryGUIExpansion.MODID)
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(INSTANCE);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new ItemBlock(INSTANCE).setRegistryName(INSTANCE.getRegistryName()));
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void registerModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(INSTANCE),
                    0,
                    new ModelResourceLocation(INSTANCE.getRegistryName(), "inventory")
            );
        }
    }
}