package com.sss.InventoryGUIExpansion.Network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    // 通道名称建议简短，避免超过 20 字符限制
    // 如果你有主类存 MODID，也可以用 MODID，但确保它不长
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("invguiexpansion");

    private static int packetId = 0;

    public static void init() {
        // 注册同步包：由服务端发送 -> 客户端接收 (Side.CLIENT)
        // 这就是用来解决 GUI 警告不消失问题的关键包
        registerMessage(PacketSyncCraftingData.Handler.class, PacketSyncCraftingData.class, Side.CLIENT);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            Class<? extends net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler<REQ, REPLY>> handlerClass,
            Class<REQ> messageClass,
            Side side) {
        INSTANCE.registerMessage(handlerClass, messageClass, packetId++, side);
    }

    /**
     * 发送给指定玩家 (用于同步 Capability 数据)
     */
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }

    /**
     * 发送给服务器 (如果有其他功能需要)
     */
    public static void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }
}