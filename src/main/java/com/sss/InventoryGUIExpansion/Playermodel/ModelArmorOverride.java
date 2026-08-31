package com.sss.InventoryGUIExpansion.Playermodel;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ModelArmorOverride extends ModelBiped {
    public ItemStack heldStack = ItemStack.EMPTY;
    private float armPitch = -0.5f;

    public ModelArmorOverride(float modelSize) {
        // 关键点：这里指定 textureHeight 为 32，适配原版护甲
        super(modelSize, 0.0F, 64, 32);
    }

    public void setCustomArmPitch(float angle) {
        this.armPitch = angle;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // 设置 ArmPose 确保基础姿态正确
        if (!this.heldStack.isEmpty()) {
            this.rightArmPose = ArmPose.ITEM;
        } else {
            this.rightArmPose = ArmPose.EMPTY;
        }

        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        // 强制覆盖手臂角度，与身体模型保持一致
        if (!this.heldStack.isEmpty()) {
            this.bipedRightArm.rotateAngleX = armPitch;
            this.bipedRightArm.rotateAngleY = -0.1f;
            this.bipedRightArm.rotateAngleZ = 0.0f;

            // 左手也同步，防止不对称
            this.bipedLeftArm.rotateAngleX = armPitch;
            this.bipedLeftArm.rotateAngleY = 0.1f;
            this.bipedLeftArm.rotateAngleZ = 0.0f;
        }
    }
}