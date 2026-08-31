package com.sss.InventoryGUIExpansion.SmeltingGUI;

import com.sss.InventoryGUIExpansion.Item.BlockCompressedSmelting;
import com.sss.InventoryGUIExpansion.Playermodel.ModelArmorOverride;
import com.sss.InventoryGUIExpansion.Playermodel.ModelPreviewOverride;
import com.sss.InventoryGUIExpansion.PotionDisableCrafting.PotionDisableCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSmelting extends GuiContainer {
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("inventoryguiexpansion", "textures/gui/inventory_smelting.png");
    private static final ResourceLocation COAL_ICON = new ResourceLocation("inventoryguiexpansion", "textures/gui/inventory_coal_icon.png");
    private static final ResourceLocation IRON_ICON = new ResourceLocation("inventoryguiexpansion", "textures/gui/inventory_iron_icon.png");
    private static final ResourceLocation LOCKED_SLOT_TEXTURE = new ResourceLocation("inventoryguiexpansion", "textures/gui/locked_slot.png");

    // 自定义模型实例
    private final ModelPreviewOverride customModelNormal;
    private final ModelPreviewOverride customModelSlim;
    // 自定义护甲模型 (修复材质错乱)
    private final ModelArmorOverride customArmorModelBig;
    private final ModelArmorOverride customArmorModelSmall;

    public GuiSmelting(EntityPlayer player) {
        super(new ContainerSmelting(player));
        this.xSize = 176;
        this.ySize = 166;

        // 初始化模型
        this.customModelNormal = new ModelPreviewOverride(0.0F, false);
        this.customModelSlim = new ModelPreviewOverride(0.0F, true);
        this.customArmorModelBig = new ModelArmorOverride(1.0F);
        this.customArmorModelSmall = new ModelArmorOverride(0.5F);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float ticks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, ticks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
        boolean isHovering = super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
        if (!this.mc.player.isPotionActive(PotionDisableCrafting.INSTANCE)) {
            return isHovering;
        }
        if (isHovering) {
            // 槽位 3 (修复输出)
            if (rectX == 155 && rectY == 14) return false;
            // 槽位 6 (烧炼输出)
            if (rectX == 155 && rectY == 43) return false;
        }
        return isHovering;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float ticks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        int x = this.guiLeft;
        int y = this.guiTop;

        // 1. 背景
        this.mc.getTextureManager().bindTexture(BG_TEXTURE);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, this.xSize, this.ySize, 176, 166);

        // 2. 幽灵图标
        renderGhostIcon(2, IRON_ICON, x + 118, y + 22);
        renderGhostIcon(5, COAL_ICON, x + 118, y + 42);

        // 3. 药水锁定图标
        if (this.mc.player.isPotionActive(PotionDisableCrafting.INSTANCE)) {
            this.mc.getTextureManager().bindTexture(LOCKED_SLOT_TEXTURE);
            GlStateManager.enableBlend();
            drawModalRectWithCustomSizedTexture(x + 154, y + 13, 0, 0, 18, 18, 18, 18);
            drawModalRectWithCustomSizedTexture(x + 154, y + 42, 0, 0, 18, 18, 18, 18);
            GlStateManager.disableBlend();
        }

        // 4. 渲染抱着压缩熔炉的玩家实体 (使用高级渲染技术)
        renderPlayerWithStation(x, y, mouseX, mouseY);
    }

    /**
     * 封装后的玩家渲染逻辑，负责模型替换、物品模拟、静音处理和护甲修复
     */
    private void renderPlayerWithStation(int x, int y, int mouseX, int mouseY) {
        // 创建要抱着的物品：压缩熔炉
        ItemStack displayStack = new ItemStack(BlockCompressedSmelting.INSTANCE);

        // --- 备份原始状态 ---
        float originalSwingProgress = this.mc.player.swingProgress;
        float originalPrevSwingProgress = this.mc.player.prevSwingProgress;
        boolean originalSneaking = this.mc.player.isSneaking();
        float originalBodyYaw = this.mc.player.renderYawOffset;
        float originalHeadYaw = this.mc.player.rotationYawHead;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderPlayer renderPlayer = renderManager.getSkinMap().get(this.mc.player.getSkinType());
        ModelBase originalModel = renderPlayer.getMainModel();

        // 渲染层变量
        List<LayerRenderer<?>> layerRenderers = null;
        LayerHeldItem removedHeldItemLayer = null;
        int removedLayerIndex = -1;

        LayerArmorBase armorLayer = null;
        ModelBiped originalArmorBig = null;
        ModelBiped originalArmorSmall = null;

        try {
            // --- 设置视觉状态 ---
            this.mc.player.swingProgress = 0.0F;
            this.mc.player.prevSwingProgress = 0.0F;
            this.mc.player.setSneaking(false);

            // 计算视线位置
            float lookX = (float)(x + 51) - mouseX;
            float lookY = (float)(y + 75 - 50) - mouseY;

            // --- 准备模型 ---
            ModelPreviewOverride overrideModel = this.mc.player.getSkinType().equals("slim") ? customModelSlim : customModelNormal;

            // 设置模型拿着熔炉
            overrideModel.heldStack = displayStack;
            overrideModel.setCustomArmPitch(-0.5F); // 手臂抬起角度
            overrideModel.isSneak = false;
            overrideModel.isChild = originalModel.isChild;
            overrideModel.swingProgress = 0.0F;

            // 关键：同步原版模型，让 Mod 帽子跟着动
            if (originalModel instanceof ModelBiped) {
                overrideModel.originalModelToSync = (ModelBiped) originalModel;
            }

            // 使用反射强制注入自定义模型
            ObfuscationReflectionHelper.setPrivateValue(RenderLivingBase.class, renderPlayer, overrideModel, "field_77045_g");

            // --- 移除手持物品层 (消除噪音和重叠) ---
            try {
                layerRenderers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class, renderPlayer, "field_177097_h");
                if (layerRenderers != null) {
                    for (int i = 0; i < layerRenderers.size(); i++) {
                        Object layer = layerRenderers.get(i);
                        if (layer instanceof LayerHeldItem) {
                            removedHeldItemLayer = (LayerHeldItem) layer;
                            removedLayerIndex = i;
                            layerRenderers.remove(i);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- 替换护甲模型 (修复材质错乱) ---
            try {
                if (layerRenderers != null) {
                    for (Object layer : layerRenderers) {
                        if (layer instanceof LayerBipedArmor) {
                            armorLayer = (LayerArmorBase) layer;
                            break;
                        }
                    }
                }

                if (armorLayer != null) {
                    originalArmorBig = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, armorLayer, "field_177186_d");
                    originalArmorSmall = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, armorLayer, "field_177189_c");

                    customArmorModelBig.heldStack = displayStack;
                    customArmorModelBig.setCustomArmPitch(-0.5F);
                    customArmorModelBig.isSneak = false;

                    customArmorModelSmall.heldStack = displayStack;
                    customArmorModelSmall.setCustomArmPitch(-0.5F);
                    customArmorModelSmall.isSneak = false;

                    ObfuscationReflectionHelper.setPrivateValue(LayerArmorBase.class, armorLayer, customArmorModelBig, "field_177186_d");
                    ObfuscationReflectionHelper.setPrivateValue(LayerArmorBase.class, armorLayer, customArmorModelSmall, "field_177189_c");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- 执行渲染 ---
            GuiInventory.drawEntityOnScreen(x + 51, y + 75, 30, lookX, lookY, this.mc.player);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // --- 还原原始状态 ---

            this.mc.player.swingProgress = originalSwingProgress;
            this.mc.player.prevSwingProgress = originalPrevSwingProgress;
            this.mc.player.setSneaking(originalSneaking);
            this.mc.player.renderYawOffset = originalBodyYaw;
            this.mc.player.rotationYawHead = originalHeadYaw;

            // 清理引用
            customModelNormal.heldStack = ItemStack.EMPTY;
            customModelSlim.heldStack = ItemStack.EMPTY;
            customModelNormal.originalModelToSync = null;
            customModelSlim.originalModelToSync = null;

            // 还原主模型
            ObfuscationReflectionHelper.setPrivateValue(RenderLivingBase.class, renderPlayer, originalModel, "field_77045_g");

            // 还原手持层
            if (layerRenderers != null && removedHeldItemLayer != null) {
                LayerRenderer layerToAdd = (LayerRenderer) removedHeldItemLayer;
                if (removedLayerIndex >= 0 && removedLayerIndex <= layerRenderers.size()) {
                    layerRenderers.add(removedLayerIndex, layerToAdd);
                } else {
                    layerRenderers.add(layerToAdd);
                }
            }

            // 还原护甲模型
            if (armorLayer != null) {
                try {
                    if (originalArmorBig != null) {
                        ObfuscationReflectionHelper.setPrivateValue(LayerArmorBase.class, armorLayer, originalArmorBig, "field_177186_d");
                    }
                    if (originalArmorSmall != null) {
                        ObfuscationReflectionHelper.setPrivateValue(LayerArmorBase.class, armorLayer, originalArmorSmall, "field_177189_c");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void renderGhostIcon(int slotId, ResourceLocation icon, int xPos, int yPos) {
        Slot specializedSlot = this.inventorySlots.getSlot(slotId);
        if (specializedSlot != null && !specializedSlot.getHasStack()) {
            this.mc.getTextureManager().bindTexture(icon);
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.6F);
            drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, 18, 18, 18, 18);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }
}