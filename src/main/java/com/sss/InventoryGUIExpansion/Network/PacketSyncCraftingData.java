package com.sss.InventoryGUIExpansion.Network; // 确保包名正确

import com.sss.InventoryGUIExpansion.CraftingGUI.InventoryData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSyncCraftingData implements IMessage {

    private NBTTagCompound nbt;

    // 必须保留无参构造函数
    public PacketSyncCraftingData() {}

    // 发送时用的构造函数
    public PacketSyncCraftingData(NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // 使用 ByteBufUtils 读取 NBT
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // 使用 ByteBufUtils 写入 NBT
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler implements IMessageHandler<PacketSyncCraftingData, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncCraftingData message, MessageContext ctx) {
            // 这是客户端收到的消息
            // 必须推迟到主线程执行
            Minecraft.getMinecraft().addScheduledTask(() -> {
                handleClientSide(message);
            });
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleClientSide(PacketSyncCraftingData message) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null) {
                // 获取客户端玩家的 Capability
                InventoryData cap = player.getCapability(InventoryData.CRAFTING_DATA_CAP, null);
                if (cap != null && message.nbt != null) {
                    // 反序列化，更新客户端数据
                    cap.deserializeNBT(message.nbt);
                }
            }
        }
    }
}