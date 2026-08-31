package com.sss.InventoryGUIExpansion.Item;

import com.sss.InventoryGUIExpansion.InventoryGUIExpansion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal; // [新增] 用于水平方向控制
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection; // [新增] 方向属性
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer; // [新增] 状态容器
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase; // [新增] 用于获取放置者的朝向
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror; // [可选] 这里的方块通常不需要镜像
import net.minecraft.util.Rotation; // [可选] 用于结构生成时的旋转
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

public class BlockCompressedTable extends Block {

    // [关键修复] 必须先定义 FACING，再定义 INSTANCE
    // 因为 INSTANCE 初始化时会调用构造函数，构造函数会用到 FACING
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    // 然后再定义 INSTANCE
    public static final BlockCompressedTable INSTANCE = new BlockCompressedTable();

    public static final int GUI_ID = 0;
    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5D, 0.75D);

    public BlockCompressedTable() {
        super(Material.WOOD);
        this.setRegistryName("compressed_crafting_table");
        this.setTranslationKey("compressed_crafting_table");
        this.setCreativeTab(CreativeTabs.MISC);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        // 这里会用到 FACING，现在它已经被初始化了，不会报错了
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    // --- [新增 3] 放置逻辑：获取玩家朝向并取反 (面对玩家) ---
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // placer.getHorizontalFacing() 获取玩家看的方向
        // getOpposite() 取反，让方块正面朝向玩家
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    // --- [新增 4] 状态与元数据 (Meta) 的相互转换 ---
    // 1.12.2 需要手动将 EnumFacing 转换为 int (0-3) 存储
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

    // 告诉游戏这个方块有哪些属性
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    // [可选] 支持用扳手或指令旋转
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    // --- 保持原有的 GUI 打开逻辑 ---
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(InventoryGUIExpansion.instance, InventoryGUIExpansion.GUI_ID_CRAFTING_NOSLOT, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    // --- 保持原有的渲染属性 ---
    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    // --- 注册类 ---
    @Mod.EventBusSubscriber(modid = InventoryGUIExpansion.MODID)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) { event.getRegistry().register(INSTANCE); }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) { event.getRegistry().register(new ItemBlock(INSTANCE).setRegistryName(INSTANCE.getRegistryName())); }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void registerModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(INSTANCE), 0, new ModelResourceLocation(INSTANCE.getRegistryName(), "inventory"));
        }
    }
}