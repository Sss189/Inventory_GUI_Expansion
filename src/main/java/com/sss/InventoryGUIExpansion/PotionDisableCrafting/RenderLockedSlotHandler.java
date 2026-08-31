package com.sss.InventoryGUIExpansion.PotionDisableCrafting; // 建议放在 client 包下


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "inventoryguiexpansion", value = Side.CLIENT)
public class RenderLockedSlotHandler {

    private static final ResourceLocation LOCKED_SLOT_TEXTURE = new ResourceLocation("inventoryguiexpansion", "textures/gui/locked_slot.png");

    /**
     * DrawForeground 事件在绘制完背景和槽位内容后触发。
     * 这里的坐标 (0,0) 是 GUI 的左上角，不需要像 DrawScreen 那样计算 guiLeft/Top。
     */
    @SubscribeEvent
    public static void onDrawForeground(GuiContainerEvent.DrawForeground event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || !player.isPotionActive(PotionDisableCrafting.INSTANCE)) {
            return;
        }

        GuiContainer gui = event.getGuiContainer();

        // 遍历当前打开容器的所有槽位
        for (Slot slot : gui.inventorySlots.inventorySlots) {
            // 核心逻辑：自动识别所有被你替换成了 Proxy 的槽位
            // 只要你是通过 SlotCraftingProxy 锁住的，贴图就会自动跟过去
            if (slot instanceof SlotCraftingProxy) {
                drawLockOverlay(gui, slot);
            }
        }
    }

    private static void drawLockOverlay(GuiContainer gui, Slot slot) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(LOCKED_SLOT_TEXTURE);

        GlStateManager.enableBlend();
        GlStateManager.disableLighting(); // 关闭光照，防止图标变暗
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // slot.xPos 和 slot.yPos 是物品渲染的位置 (16x16)
        // 槽位背景通常比物品大一圈 (18x18)，所以我们要偏移 -1
        int x = slot.xPos - 1;
        int y = slot.yPos - 1;

        // 绘制 18x18 的锁图标
        // 假设你的图片是 18x18 像素
        GuiContainer.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 18, 18);

        GlStateManager.disableBlend();
    }
}