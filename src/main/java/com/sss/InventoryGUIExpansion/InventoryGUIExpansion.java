package com.sss.InventoryGUIExpansion;

import com.sss.InventoryGUIExpansion.CraftingGUI.ContainerCrafting;
import com.sss.InventoryGUIExpansion.CraftingGUI.GuiCrafting;
import com.sss.InventoryGUIExpansion.CraftingGUI.InventoryData;
import com.sss.InventoryGUIExpansion.SmeltingGUI.ContainerSmelting;
import com.sss.InventoryGUIExpansion.SmeltingGUI.GuiSmelting;
import com.sss.InventoryGUIExpansion.openGUI.CommandOpenGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = InventoryGUIExpansion.MODID, name = InventoryGUIExpansion.NAME, version = InventoryGUIExpansion.VERSION)
public class InventoryGUIExpansion {
    public static final String MODID = "inventoryguiexpansion";
    public static final String NAME = "Inventory GUI Expansion";
    public static final String VERSION = "1.0";

    @Instance(MODID)
    public static InventoryGUIExpansion instance;

    @SidedProxy(
            clientSide = "com.sss.InventoryGUIExpansion.InventoryGUIExpansion$ClientProxy",
            serverSide = "com.sss.InventoryGUIExpansion.InventoryGUIExpansion$CommonProxy"
    )
    public static CommonProxy proxy;

    public static final int GUI_ID_CRAFTING_GUI = 0;    // 方块打开 (有槽)
    public static final int GUI_ID_SMELTING = 1;          // 熔炉
    public static final int GUI_ID_CRAFTING_NOSLOT = 2; // 命令/物品打开 (无槽)

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        com.sss.InventoryGUIExpansion.Network.NetworkHandler.init();
        MinecraftForge.EVENT_BUS.register(new InventoryData.EventHandler());
        CapabilityManager.INSTANCE.register(InventoryData.class, new Capability.IStorage<InventoryData>() {
            @Override
            public NBTBase writeNBT(Capability<InventoryData> capability, InventoryData instance, EnumFacing side) {
                return instance.serializeNBT();
            }
            @Override
            public void readNBT(Capability<InventoryData> capability, InventoryData instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }, InventoryData::new);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandOpenGui());
    }

    /**
     * 服务端代理
     */
    public static class CommonProxy implements IGuiHandler {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            switch (ID) {
                case GUI_ID_CRAFTING_GUI:
                    // ID 0: 方块模式 -> hasCustomSlot = true
                    return new ContainerCrafting(player, true);

                case GUI_ID_CRAFTING_NOSLOT:
                    // ID 2: 便携/命令模式 -> hasCustomSlot = false
                    return new ContainerCrafting(player, false);

                case GUI_ID_SMELTING:
                    return new ContainerSmelting(player);
            }
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return null;
        }
    }

    /**
     * 客户端代理
     */
    public static class ClientProxy extends CommonProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            switch (ID) {
                case GUI_ID_CRAFTING_GUI:
                    // ID 0: 方块模式 -> hasCustomSlot = true
                    return new GuiCrafting(player, true);

                case GUI_ID_CRAFTING_NOSLOT:
                    // ID 2: 便携/命令模式 -> hasCustomSlot = false
                    return new GuiCrafting(player, false);

                case GUI_ID_SMELTING:
                    return new GuiSmelting(player);
            }
            return null;
        }
    }
}