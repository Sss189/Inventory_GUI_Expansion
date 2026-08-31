package com.sss.InventoryGUIExpansion.CraftingGUI;

import com.sss.InventoryGUIExpansion.Item.BlockCompressedTable;
import com.sss.InventoryGUIExpansion.PotionDisableCrafting.PotionDisableCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerCrafting extends Container {
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public InventoryCraftResult craftResult = new InventoryCraftResult();

    private final EntityPlayer player;
    private final boolean hasCustomSlot; // 是否有压缩工作台槽

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = new EntityEquipmentSlot[]{
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET
    };

    public ContainerCrafting(EntityPlayer player, boolean hasCustomSlot) {
        this.player = player;
        this.hasCustomSlot = hasCustomSlot;

        // --- 0. 合成产物槽 ---
        this.addSlotToContainer(new SlotCrafting(player, craftMatrix, craftResult, 0, 155, 43));

        // --- 1-9. 3x3 合成矩阵 ---
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlotToContainer(new Slot(craftMatrix, j + i * 3, 99 + j * 18, 7 + i * 18));
            }
        }

        // --- 10. 压缩合成表专用槽 (仅当 hasCustomSlot 为真时添加) ---
        if (hasCustomSlot) {
            InventoryData cap = player.getCapability(InventoryData.CRAFTING_DATA_CAP, null);
            IItemHandler customHandler = (cap != null) ? cap.handler : null;

            if (customHandler != null) {
                this.addSlotToContainer(new SlotItemHandler(customHandler, 0, 155, 7) {
                    @Override
                    public boolean isItemValid(@Nonnull ItemStack stack) {
                        return stack.getItem() == Item.getItemFromBlock(BlockCompressedTable.INSTANCE);
                    }
                    @Override
                    public int getItemStackLimit(@Nonnull ItemStack stack) { return 1; }
                    @Override
                    public void onSlotChanged() {
                        super.onSlotChanged();
                        ContainerCrafting.this.onCraftMatrixChanged(craftMatrix);
                    }
                });
            }
        }

        // --- 盔甲槽 ---
        for (int k = 0; k < 4; ++k) {
            final EntityEquipmentSlot equipmentSlot = ARMOR_SLOTS[k];
            this.addSlotToContainer(new Slot(player.inventory, 36 + (3 - k), 8, 8 + k * 18) {
                @Override public int getSlotStackLimit() { return 1; }
                @Override public boolean isItemValid(ItemStack stack) { return stack.getItem().isValidArmor(stack, equipmentSlot, player); }
                @Nullable @SideOnly(Side.CLIENT) @Override public String getSlotTexture() { return ItemArmor.EMPTY_SLOT_NAMES[equipmentSlot.getIndex()]; }
            });
        }

        // --- 副手槽 ---
        this.addSlotToContainer(new Slot(player.inventory, 40, 77, 62) {
            @Nullable @SideOnly(Side.CLIENT) @Override public String getSlotTexture() { return "minecraft:items/empty_armor_slot_shield"; }
        });

        // --- 玩家背包 ---
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // --- 快捷栏 ---
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
        }

        this.onCraftMatrixChanged(craftMatrix);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        if (player.isPotionActive(PotionDisableCrafting.INSTANCE)) {
            craftResult.setInventorySlotContents(0, ItemStack.EMPTY);
            return;
        }

        if (hasCustomSlot) {
            // 有槽模式：需检查 Slot 10 是否有物品
            InventoryData cap = player.getCapability(InventoryData.CRAFTING_DATA_CAP, null);
            if (cap != null && !cap.handler.getStackInSlot(0).isEmpty()) {
                this.slotChangedCraftingGrid(player.world, player, craftMatrix, craftResult);
            } else {
                craftResult.setInventorySlotContents(0, ItemStack.EMPTY);
            }
        } else {
            // 无槽模式：直接允许合成
            this.slotChangedCraftingGrid(player.world, player, craftMatrix, craftResult);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) { return true; }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote) {
            this.clearContainer(playerIn, playerIn.world, craftMatrix);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack1 = slot.getStack();
            itemstack = stack1.copy();

            // 动态索引计算
            int customSlotIndex = hasCustomSlot ? 10 : -1;
            int armorStart = hasCustomSlot ? 11 : 10;
            int playerInvStart = armorStart + 4 + 1; // 盔甲(4) + 副手(1)
            int end = playerInvStart + 36;

            if (index == 0) { // 产物
                stack1.getItem().onCreated(stack1, playerIn.world, playerIn);
                if (!this.mergeItemStack(stack1, playerInvStart, end, true)) return ItemStack.EMPTY;
                slot.onSlotChange(stack1, itemstack);
            }
            else if (index >= 1 && index <= 9) { // 合成格 -> 背包
                if (!this.mergeItemStack(stack1, playerInvStart, end, true)) return ItemStack.EMPTY;
            }
            else if (hasCustomSlot && index == customSlotIndex) { // 专用槽 -> 背包
                if (!this.mergeItemStack(stack1, playerInvStart, end, true)) return ItemStack.EMPTY;
            }
            else if (index >= playerInvStart) { // 背包 -> 其他
                // 1. 尝试放入专用槽 (仅在有槽模式下)
                if (hasCustomSlot && stack1.getItem() == Item.getItemFromBlock(BlockCompressedTable.INSTANCE)) {
                    if (!this.mergeItemStack(stack1, customSlotIndex, customSlotIndex + 1, false)) return ItemStack.EMPTY;
                }
                // 2. 尝试放入盔甲槽
                else if (stack1.getItem() instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor) stack1.getItem();
                    int armorIdx = armorStart + (3 - armor.armorType.getIndex());
                    if (!this.mergeItemStack(stack1, armorIdx, armorIdx + 1, false)) {
                        if (!this.mergeItemStack(stack1, 1, 10, false)) return ItemStack.EMPTY;
                    }
                }
                // 3. 放入合成格
                else {
                    if (!this.mergeItemStack(stack1, 1, 10, false)) return ItemStack.EMPTY;
                }
            }
            else if (index >= armorStart && index < playerInvStart) { // 盔甲/副手 -> 背包
                if (!this.mergeItemStack(stack1, playerInvStart, end, true)) return ItemStack.EMPTY;
            }

            if (stack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();

            if (stack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(playerIn, stack1);
        }

        return itemstack;
    }
}