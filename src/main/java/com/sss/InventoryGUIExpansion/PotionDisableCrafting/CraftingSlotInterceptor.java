package com.sss.InventoryGUIExpansion.PotionDisableCrafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = "inventoryguiexpansion")
public class CraftingSlotInterceptor {

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        replaceSlot(event.getEntityPlayer(), event.getContainer());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 针对背包合成，因为它可能在玩家进入游戏时就存在，不触发 Open 事件
            replaceSlot(event.player, event.player.openContainer);
        }
    }

    private static void replaceSlot(EntityPlayer player, Container container) {
        if (container instanceof ContainerPlayer || container instanceof ContainerWorkbench) {
            // 获取槽位 0 (合成产物槽)
            Slot slot0 = container.getSlot(0);

            // 如果它还不是代理槽位，就替换它
            if (!(slot0 instanceof SlotCraftingProxy)) {
                // 将 Slot 0 替换为我们的代理类
                // inventorySlots 是 List<Slot>，它是 public 的
                container.inventorySlots.set(0, new SlotCraftingProxy(slot0, player));
            }
        }
    }
}