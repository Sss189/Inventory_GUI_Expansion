package com.sss.InventoryGUIExpansion.PotionDisableCrafting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = "inventoryguiexpansion")
public class PotionDisableCrafting extends Potion {

    public static final PotionDisableCrafting INSTANCE = new PotionDisableCrafting();

    // ====================== 图标配置 (Client-Side) ======================
    private static final int ICON_WIDTH = 18;
    private static final int ICON_HEIGHT = 18;


    private static final ResourceLocation ICON_TEXTURE =
            new ResourceLocation("inventoryguiexpansion", "textures/potions/burnout.png");
    // ===================================================================

    private PotionDisableCrafting() {
        // true: 是不良效果; 0xFF4500: 橙红色
        super(true, 0xFF4500);
        this.setPotionName("effect.disable_crafting");
        this.setRegistryName(new ResourceLocation("inventoryguiexpansion", "disable_crafting"));
    }

    /**
     * 渲染在玩家背包/效果列表中的图标
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(ICON_TEXTURE);

        // 强制设为白色，避免药水颜色影响贴图原色
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        gui.drawModalRectWithCustomSizedTexture(
                x + 6, y + 7,
                0, 0,
                ICON_WIDTH, ICON_HEIGHT,
                (float)ICON_WIDTH, (float)ICON_HEIGHT
        );
    }

    /**
     * 渲染在 HUD（屏幕右上角）的图标
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float partialTicks, float alpha) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(ICON_TEXTURE);

        // 传入 alpha 值，确保图标随 HUD 渐隐效果一致
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

        gui.drawModalRectWithCustomSizedTexture(
                x + 3, y + 3,
                0, 0,
                ICON_WIDTH, ICON_HEIGHT,
                (float)ICON_WIDTH, (float)ICON_HEIGHT
        );

        // 恢复颜色状态
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onPotionRegistry(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(INSTANCE);
    }
}