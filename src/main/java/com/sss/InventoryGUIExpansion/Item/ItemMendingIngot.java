package com.sss.InventoryGUIExpansion.Item;

import com.sss.InventoryGUIExpansion.InventoryGUIExpansion;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMendingIngot extends Item {

    // 1. 创建一个静态实例，方便其他地方引用（比如合成配方）
    public static final ItemMendingIngot INSTANCE = new ItemMendingIngot();

    public ItemMendingIngot() {
        // 设置注册名
        this.setRegistryName("mending_ingot");
        // 设置本地化名 (item.mending_ingot.name)
        this.setTranslationKey("mending_ingot");
        // 设置创造模式栏
        this.setCreativeTab(CreativeTabs.MISC);
    }

    /**
     * 2. 内部注册处理类
     * 这里的 @Mod.EventBusSubscriber 会自动让 Forge 扫描到这个类并监听事件
     */
    @Mod.EventBusSubscriber(modid = InventoryGUIExpansion.MODID)
    public static class RegistrationHandler {

        // 注册物品
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(INSTANCE);
        }

        // 注册模型（仅客户端）
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void registerModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(
                    INSTANCE,
                    0,
                    new ModelResourceLocation(INSTANCE.getRegistryName(), "inventory")
            );
        }
    }
}