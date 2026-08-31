package com.sss.InventoryGUIExpansion.CraftingGUI;

import com.sss.InventoryGUIExpansion.Item.BlockCompressedTable;
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
public class GuiCrafting extends GuiContainer {
    private static final ResourceLocation TEX_NORMAL = new ResourceLocation("inventoryguiexpansion", "textures/gui/inventory_crafting.png");
    private static final ResourceLocation TEX_SLOTLESS = new ResourceLocation("inventoryguiexpansion", "textures/gui/inventory_crafting_slotless.png");

    private static final ResourceLocation ICON = new ResourceLocation("inventoryguiexpansion", "textures/gui/inventory_crafting_icon.png");
    private static final ResourceLocation LOCKED_SLOT_TEXTURE = new ResourceLocation("inventoryguiexpansion", "textures/gui/locked_slot.png");

    private final ModelPreviewOverride customModelNormal;
    private final ModelPreviewOverride customModelSlim;
    private final ModelArmorOverride customArmorModelBig;
    private final ModelArmorOverride customArmorModelSmall;

    private final boolean hasCustomSlot;

    public GuiCrafting(EntityPlayer player, boolean hasCustomSlot) {
        super(new ContainerCrafting(player, hasCustomSlot));
        this.hasCustomSlot = hasCustomSlot;
        this.xSize = 176;
        this.ySize = 166;

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
            if (rectX == 155 && rectY == 43) {
                return false;
            }
        }
        return isHovering;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float ticks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.mc.getTextureManager().bindTexture(hasCustomSlot ? TEX_NORMAL : TEX_SLOTLESS);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, this.xSize, this.ySize, 176, 166);

        ItemStack displayStack;

        if (hasCustomSlot) {
            Slot specializedSlot = this.inventorySlots.getSlot(10);
            displayStack = specializedSlot.getStack();

            if (displayStack.isEmpty()) {
                this.mc.getTextureManager().bindTexture(ICON);
                GlStateManager.enableBlend();
                drawModalRectWithCustomSizedTexture(x + 155, y + 7, 0, 0, 16, 16, 16, 16);
                GlStateManager.disableBlend();
            }
        } else {
            displayStack = new ItemStack(BlockCompressedTable.INSTANCE);
        }

        if (this.mc.player.isPotionActive(PotionDisableCrafting.INSTANCE)) {
            this.mc.getTextureManager().bindTexture(LOCKED_SLOT_TEXTURE);
            GlStateManager.enableBlend();
            drawModalRectWithCustomSizedTexture(x + 154, y + 42, 0, 0, 18, 18, 18, 18);
            GlStateManager.disableBlend();
        }

        boolean shouldOverrideModel = !hasCustomSlot || !displayStack.isEmpty();

        // --- 准备工作 ---
        float originalSwingProgress = this.mc.player.swingProgress;
        float originalPrevSwingProgress = this.mc.player.prevSwingProgress;
        boolean originalSneaking = this.mc.player.isSneaking();
        float originalBodyYaw = this.mc.player.renderYawOffset;
        float originalHeadYaw = this.mc.player.rotationYawHead;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderPlayer renderPlayer = renderManager.getSkinMap().get(this.mc.player.getSkinType());
        ModelBase originalModel = renderPlayer.getMainModel();

        LayerArmorBase armorLayer = null;
        ModelBiped originalArmorBig = null;
        ModelBiped originalArmorSmall = null;

        // 【关键修改】：使用原生 List 或者 List<LayerRenderer<?>> 来避免泛型转换错误
        List<LayerRenderer<?>> layerRenderers = null;
        LayerHeldItem removedHeldItemLayer = null;
        int removedLayerIndex = -1;

        try {
            float lookX;
            float lookY;

            if (shouldOverrideModel) {
                this.mc.player.swingProgress = 0.0F;
                this.mc.player.prevSwingProgress = 0.0F;
                this.mc.player.setSneaking(false);

                lookX = (float)(x + 51) - mouseX;
                lookY = (float)(y + 75 - 50) - mouseY;

                ModelPreviewOverride overrideModel = this.mc.player.getSkinType().equals("slim") ? customModelSlim : customModelNormal;
                overrideModel.heldStack = displayStack;
                overrideModel.setCustomArmPitch(-0.5F);
                overrideModel.isSneak = false;
                overrideModel.isChild = originalModel.isChild;
                overrideModel.swingProgress = 0.0F;

                if (originalModel instanceof ModelBiped) {
                    overrideModel.originalModelToSync = (ModelBiped) originalModel;
                }

                ObfuscationReflectionHelper.setPrivateValue(RenderLivingBase.class, renderPlayer, overrideModel, "field_77045_g");

                // 【关键修改】：反射获取列表并移除手持层
                try {
                    // 这里获取到的列表可能是 List<LayerRenderer<AbstractClientPlayer>>，我们用通配符接收
                    layerRenderers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class, renderPlayer, "field_177097_h");
                    if (layerRenderers != null) {
                        for (int i = 0; i < layerRenderers.size(); i++) {
                            // 直接判断对象是否是 LayerHeldItem 的实例
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

                // 替换护甲模型
                try {
                    if (layerRenderers != null) {
                        for (Object layer : layerRenderers) { // 使用 Object 遍历
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
            } else {
                lookX = (float)(x + 51) - mouseX;
                lookY = (float)(y + 75 - 50) - mouseY;
            }

            GuiInventory.drawEntityOnScreen(x + 51, y + 75, 30, lookX, lookY, this.mc.player);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // --- 还原现场 ---

            this.mc.player.swingProgress = originalSwingProgress;
            this.mc.player.prevSwingProgress = originalPrevSwingProgress;
            this.mc.player.setSneaking(originalSneaking);
            this.mc.player.renderYawOffset = originalBodyYaw;
            this.mc.player.rotationYawHead = originalHeadYaw;

            customModelNormal.heldStack = ItemStack.EMPTY;
            customModelSlim.heldStack = ItemStack.EMPTY;
            customModelNormal.originalModelToSync = null;
            customModelSlim.originalModelToSync = null;

            if (shouldOverrideModel) {
                ObfuscationReflectionHelper.setPrivateValue(RenderLivingBase.class, renderPlayer, originalModel, "field_77045_g");
            }

            // 【关键修改】：还原 LayerHeldItem
            // 只要这里使用的是同一个 list 引用，泛型擦除后添加回去是没问题的
            if (layerRenderers != null && removedHeldItemLayer != null) {
                // 为了安全起见，这里强转回 (LayerRenderer) 再添加
                LayerRenderer layerToAdd = (LayerRenderer) removedHeldItemLayer;
                if (removedLayerIndex >= 0 && removedLayerIndex <= layerRenderers.size()) {
                    layerRenderers.add(removedLayerIndex, layerToAdd);
                } else {
                    layerRenderers.add(layerToAdd);
                }
            }

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
}