package com.sss.InventoryGUIExpansion.PotionDisableCrafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting; // 关键：引入 SlotCrafting
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * 这是一个代理槽位。
 * 修改：现在它继承自 SlotCrafting 以保持与其他模组（如 Quark）的兼容性。
 */
public class SlotCraftingProxy extends SlotCrafting { // 关键修改：extends SlotCrafting
    private final Slot originalSlot;
    private final EntityPlayer player;

    // 创建一个静态的虚拟合成矩阵，仅用于满足父类构造函数的要求
    // 因为我们会覆盖所有方法并代理给 originalSlot，所以这个 dummy 不会被实际使用
    private static final InventoryCrafting DUMMY_MATRIX = new InventoryCrafting(new Container() {
        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }
    }, 1, 1);

    public SlotCraftingProxy(Slot originalSlot, EntityPlayer player) {
        // 调用父类 SlotCrafting 的构造函数
        // 我们传入 DUMMY_MATRIX 只是为了不报错，实际逻辑全部走 originalSlot
        super(player, DUMMY_MATRIX, originalSlot.inventory, originalSlot.getSlotIndex(), originalSlot.xPos, originalSlot.yPos);

        this.originalSlot = originalSlot;
        this.player = player;
        this.slotNumber = originalSlot.slotNumber; // 确保全局索引一致
    }

    private boolean isBlocked() {
        return player.isPotionActive(PotionDisableCrafting.INSTANCE);
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        return isBlocked() ? ItemStack.EMPTY : originalSlot.getStack();
    }

    @Override
    public boolean getHasStack() {
        return !isBlocked() && originalSlot.getHasStack();
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return originalSlot.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return !isBlocked() && originalSlot.canTakeStack(playerIn);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (!isBlocked()) {
            originalSlot.putStack(stack);
        }
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        return isBlocked() ? ItemStack.EMPTY : originalSlot.decrStackSize(amount);
    }

    @Override
    @Nonnull
    public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
        if (isBlocked()) {
            return ItemStack.EMPTY;
        }
        // 关键：这里直接调用 originalSlot.onTake
        // 这意味着原始的合成逻辑（扣除材料、给予成就等）依然由原版槽位处理
        // 我们不需要去管 super.onTake 里那个 DUMMY_MATRIX
        return originalSlot.onTake(playerIn, stack);
    }

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        if (!isBlocked()) {
            originalSlot.onSlotChange(p_75220_1_, p_75220_2_);
        }
    }

    // 覆盖 SlotCrafting 特有的方法，防止意外副作用
    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        // 这里的逻辑通常被 onTake 内部调用，或者由 originalSlot 处理
        // 我们留空或者通过反射调用 originalSlot 的受保护方法（如果需要）
        // 但通常 onTake 代理足够处理绝大多数情况
        super.onCrafting(stack, amount);
    }
}