package com.sss.InventoryGUIExpansion.Playermodel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ModelPreviewOverride extends ModelPlayer {

    public ItemStack heldStack = ItemStack.EMPTY;
    private float armPitch = -0.5f;

    // --- 新增：用于同步的目标模型 ---
    public ModelBiped originalModelToSync = null;

    public ModelPreviewOverride(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
    }

    public void setCustomArmPitch(float angle) {
        this.armPitch = angle;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // 1. 设置 Pose
        if (!this.heldStack.isEmpty()) {
            this.rightArmPose = ArmPose.ITEM;
        } else {
            this.rightArmPose = ArmPose.EMPTY;
        }

        // 2. 父类计算基础角度
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        // 3. 应用自定义手臂角度
        if (!this.heldStack.isEmpty()) {
            this.bipedRightArm.rotateAngleX = armPitch;
            this.bipedRightArm.rotateAngleY = -0.1f;
            this.bipedRightArm.rotateAngleZ = 0.0f;

            this.bipedLeftArm.rotateAngleX = armPitch;
            this.bipedLeftArm.rotateAngleY = 0.1f;
            this.bipedLeftArm.rotateAngleZ = 0.0f;
        }

        // 4. 同步皮肤外层
        copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
        copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
        copyModelAngles(this.bipedBody, this.bipedBodyWear);
        copyModelAngles(this.bipedHead, this.bipedHeadwear);

        // --- 5. 新增：核心修复，同步给原版模型 ---
        if (this.originalModelToSync != null) {
            // 把我们计算好的头、身体、手臂角度，全部复制给原版模型
            // 这样那些读取原版模型的模组帽子就能正确旋转了
            copyModelAngles(this.bipedHead, this.originalModelToSync.bipedHead);
            copyModelAngles(this.bipedHeadwear, this.originalModelToSync.bipedHeadwear);
            copyModelAngles(this.bipedBody, this.originalModelToSync.bipedBody);
            copyModelAngles(this.bipedRightArm, this.originalModelToSync.bipedRightArm);
            copyModelAngles(this.bipedLeftArm, this.originalModelToSync.bipedLeftArm);
            copyModelAngles(this.bipedRightLeg, this.originalModelToSync.bipedRightLeg);
            copyModelAngles(this.bipedLeftLeg, this.originalModelToSync.bipedLeftLeg);
        }
    }

    public static void copyModelAngles(ModelRenderer source, ModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
    }

    // ... render 方法保持不变 ...
    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        if (!this.heldStack.isEmpty()) {
            GlStateManager.pushMatrix();
            this.bipedBody.postRender(scale);
            GlStateManager.translate(0.0F, 0.5F, -0.55F);
            GlStateManager.scale(0.8F, -0.8F, 0.8F);
            GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(this.heldStack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.bindTexture(0);
            GlStateManager.enableColorMaterial();
            RenderHelper.enableStandardItemLighting();
        }
    }
}