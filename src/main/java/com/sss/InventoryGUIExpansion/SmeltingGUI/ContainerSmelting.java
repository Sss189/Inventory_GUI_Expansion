package com.sss.InventoryGUIExpansion.SmeltingGUI;

import com.sss.InventoryGUIExpansion.Item.ItemMendingIngot;
import com.sss.InventoryGUIExpansion.PotionDisableCrafting.PotionDisableCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ContainerSmelting extends Container {
    public InventoryBasic inputInventory = new InventoryBasic("SmeltingSlots", false, 7);
    private final EntityPlayer player;

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = new EntityEquipmentSlot[]{
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET
    };

    public ContainerSmelting(EntityPlayer player) {
        this.player = player;

        // Slot 0: 待修复物品
        this.addSlotToContainer(new Slot(inputInventory, 0, 99, 14) {
            @Override public void onSlotChanged() { super.onSlotChanged(); ContainerSmelting.this.onCraftMatrixChanged(inputInventory); }
        });

        // Slot 1: 修复材料 (同种/原材料/MendingIngot)
        this.addSlotToContainer(new Slot(inputInventory, 1, 119, 5) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                ItemStack target = inputInventory.getStackInSlot(0);
                if (target.isEmpty()) return true;
                return stack.getItem() == target.getItem() ||
                        target.getItem().getIsRepairable(target, stack) ||
                        stack.getItem() == ItemMendingIngot.INSTANCE;
            }
            @Override public void onSlotChanged() { super.onSlotChanged(); ContainerSmelting.this.onCraftMatrixChanged(inputInventory); }
        });

        // Slot 2: 铁锭 (强制触媒)
        this.addSlotToContainer(new Slot(inputInventory, 2, 119, 23) {
            @Override public boolean isItemValid(ItemStack stack) { return stack.getItem() == Items.IRON_INGOT; }
            @Override public void onSlotChanged() { super.onSlotChanged(); ContainerSmelting.this.onCraftMatrixChanged(inputInventory); }
        });

        // Slot 3: 修复产物
        this.addSlotToContainer(new Slot(inputInventory, 3, 155, 14) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override
            public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
                inputInventory.decrStackSize(0, 1);
                inputInventory.decrStackSize(1, 1);
                inputInventory.decrStackSize(2, 1);
                ContainerSmelting.this.onCraftMatrixChanged(inputInventory);
                return super.onTake(playerIn, stack);
            }
        });

        // Slot 4: 烧炼输入
        this.addSlotToContainer(new Slot(inputInventory, 4, 99, 43) {
            @Override public void onSlotChanged() { super.onSlotChanged(); ContainerSmelting.this.onCraftMatrixChanged(inputInventory); }
        });

        // Slot 5: 燃料 (煤炭)
        this.addSlotToContainer(new Slot(inputInventory, 5, 119, 43) {
            @Override public boolean isItemValid(ItemStack stack) { return stack.getItem() == Items.COAL; }
            @Override public void onSlotChanged() { super.onSlotChanged(); ContainerSmelting.this.onCraftMatrixChanged(inputInventory); }
        });

        // Slot 6: 烧炼产物
        this.addSlotToContainer(new Slot(inputInventory, 6, 155, 43) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override
            public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
                inputInventory.decrStackSize(4, 1);
                inputInventory.decrStackSize(5, 1);
                ContainerSmelting.this.onCraftMatrixChanged(inputInventory);
                return super.onTake(playerIn, stack);
            }
        });

        setupPlayerInventory(player.inventory);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        if (player.isPotionActive(PotionDisableCrafting.INSTANCE)) {
            inputInventory.setInventorySlotContents(3, ItemStack.EMPTY);
            inputInventory.setInventorySlotContents(6, ItemStack.EMPTY);
            return;
        }
        updateRepairResult();
        updateSmeltResult();
    }

    private void updateRepairResult() {
        ItemStack target = inputInventory.getStackInSlot(0);
        ItemStack mat = inputInventory.getStackInSlot(1);
        ItemStack iron = inputInventory.getStackInSlot(2);

        if (target.isEmpty() || !target.isItemDamaged() || mat.isEmpty() || iron.isEmpty() || iron.getItem() != Items.IRON_INGOT) {
            inputInventory.setInventorySlotContents(3, ItemStack.EMPTY);
            return;
        }

        int maxDmg = target.getMaxDamage();
        int curDmg = target.getItemDamage();
        int repair = 0;
        boolean valid = false;

        if (mat.getItem() == ItemMendingIngot.INSTANCE) {
            repair = maxDmg / 2;
            valid = true;
        } else if (mat.getItem() == target.getItem()) {
            repair = (mat.getMaxDamage() - mat.getItemDamage()) + (maxDmg * 5 / 100);
            valid = true;
        } else if (target.getItem().getIsRepairable(target, mat)) {
            repair = maxDmg / 4;
            valid = true;
        }

        if (valid) {
            ItemStack result = target.copy();
            result.setItemDamage(Math.max(0, curDmg - repair));
            inputInventory.setInventorySlotContents(3, result);
        } else {
            inputInventory.setInventorySlotContents(3, ItemStack.EMPTY);
        }
    }

    private void updateSmeltResult() {
        ItemStack in = inputInventory.getStackInSlot(4);
        ItemStack fuel = inputInventory.getStackInSlot(5);
        if (!in.isEmpty() && !fuel.isEmpty() && fuel.getItem() == Items.COAL) {
            ItemStack res = FurnaceRecipes.instance().getSmeltingResult(in);
            if (!res.isEmpty()) {
                inputInventory.setInventorySlotContents(6, res.copy());
                return;
            }
        }
        inputInventory.setInventorySlotContents(6, ItemStack.EMPTY);
    }

    private void setupPlayerInventory(InventoryPlayer playerInv) {
        for (int k = 0; k < 4; ++k) {
            final EntityEquipmentSlot slot = ARMOR_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInv, 36 + (3 - k), 8, 8 + k * 18) {
                @Override public int getSlotStackLimit() { return 1; }
                @Override public boolean isItemValid(ItemStack s) { return s.getItem().isValidArmor(s, slot, player); }
                @Nullable @SideOnly(Side.CLIENT) @Override public String getSlotTexture() { return ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()]; }
            });
        }
        this.addSlotToContainer(new Slot(playerInv, 40, 77, 62));
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        for (int i = 0; i < 9; ++i)
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack1 = slot.getStack();
            itemstack = stack1.copy();

            if (index <= 6) {
                if (!this.mergeItemStack(stack1, 7, 43, true)) return ItemStack.EMPTY;
                slot.onSlotChange(stack1, itemstack);
            } else {
                if (stack1.getItem() instanceof ItemArmor) {
                    int armorIdx = 7 + (3 - ((ItemArmor) stack1.getItem()).armorType.getIndex());
                    if (!this.mergeItemStack(stack1, armorIdx, armorIdx + 1, false)) return ItemStack.EMPTY;
                } else if (stack1.getItem() == Items.IRON_INGOT) {
                    if (!this.mergeItemStack(stack1, 2, 3, false)) return ItemStack.EMPTY;
                } else if (stack1.getItem() == Items.COAL) {
                    if (!this.mergeItemStack(stack1, 5, 6, false)) return ItemStack.EMPTY;
                } else if (stack1.getItem() == ItemMendingIngot.INSTANCE) {
                    if (!this.mergeItemStack(stack1, 1, 2, false)) return ItemStack.EMPTY;
                } else {
                    ItemStack target = inputInventory.getStackInSlot(0);
                    if (!target.isEmpty() && (stack1.getItem() == target.getItem() || target.getItem().getIsRepairable(target, stack1))) {
                        if (!this.mergeItemStack(stack1, 1, 2, false)) return ItemStack.EMPTY;
                    } else if (!this.mergeItemStack(stack1, 0, 1, false) && !this.mergeItemStack(stack1, 4, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
            if (stack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(playerIn, stack1);
        }
        return itemstack;
    }

    @Override public boolean canInteractWith(EntityPlayer playerIn) { return true; }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote) {
            for (int i : new int[]{0, 1, 2, 4, 5}) {
                ItemStack s = inputInventory.getStackInSlot(i);
                if (!s.isEmpty()) playerIn.dropItem(s, false);
            }
        }
    }
}