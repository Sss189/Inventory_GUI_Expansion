package com.sss.InventoryGUIExpansion.CraftingGUI;

import com.sss.InventoryGUIExpansion.Network.NetworkHandler; // 确保引用你刚才写的 NetworkHandler
import com.sss.InventoryGUIExpansion.Network.PacketSyncCraftingData; // 确保引用同步包

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventoryData implements ICapabilitySerializable<NBTTagCompound> {

    @CapabilityInject(InventoryData.class)
    public static final Capability<InventoryData> CRAFTING_DATA_CAP = null;

    // 持有玩家引用，以便发包时知道发给谁
    private final EntityPlayer player;

    // 构造函数：接收玩家对象
    public InventoryData(EntityPlayer player) {
        this.player = player;
    }

    // 默认构造 (用于特殊情况，一般 Attach 时都用上面的)
    public InventoryData() {
        this.player = null;
    }

    // --- 核心 Handler ---
    public final ItemStackHandler handler = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            // 当物品发生变动（被放入、被取出、被 PassHelper 清空）时自动触发
            sync();
        }
    };

    /**
     * 同步方法：将当前数据发送给客户端
     */
    public void sync() {
        // 只有服务端才需要发包给客户端
        // 且 player 必须有效
        if (player instanceof EntityPlayerMP) {
            NetworkHandler.sendTo(new PacketSyncCraftingData(this.serializeNBT()), (EntityPlayerMP) player);
        }
    }

    // --- 静态桥梁 (供 PassHelper 反射调用) ---
    public static IItemHandler getInventory(EntityPlayer player) {
        if (player == null) return null;
        InventoryData data = player.getCapability(CRAFTING_DATA_CAP, null);
        return data != null ? data.handler : null;
    }

    // --- Capability 标准接口实现 ---
    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == CRAFTING_DATA_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        return cap == CRAFTING_DATA_CAP ? CRAFTING_DATA_CAP.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        handler.deserializeNBT(nbt);
    }

    // --- 事件注册 ---
    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void onAttach(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityPlayer) {
                // 注册时传入 EntityPlayer 实例
                event.addCapability(
                        new ResourceLocation("inventoryguiexpansion", "crafting_inv"),
                        new InventoryData((EntityPlayer) event.getObject())
                );
            }
        }

        // 建议添加：玩家登录时同步一次，防止刚进服时数据为空
        @SubscribeEvent
        public static void onPlayerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
            InventoryData data = event.player.getCapability(CRAFTING_DATA_CAP, null);
            if (data != null) data.sync();
        }

        // 建议添加：跨维度时同步 (部分版本 MC 跨维度会重置客户端实体数据)
        @SubscribeEvent
        public static void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
            InventoryData data = event.player.getCapability(CRAFTING_DATA_CAP, null);
            if (data != null) data.sync();
        }
    }
}