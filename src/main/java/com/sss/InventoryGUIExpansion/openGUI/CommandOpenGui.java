package com.sss.InventoryGUIExpansion.openGUI;

import com.sss.InventoryGUIExpansion.InventoryGUIExpansion;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandOpenGui extends CommandBase {

    @Override
    public String getName() {
        return "inventoryguiexpansion";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        // 更新用法提示，包含两个子命令
        return "/inventoryguiexpansion <player> [crafting|smelting]";
    }

    /**
     * 设为 true，确保普通玩家在没有 OP 权限时也能使用此命令
     */
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // 校验参数长度
        if (args.length < 2) {
            throw new CommandException(getUsage(sender));
        }

        // 1. 获取目标玩家
        EntityPlayer targetPlayer = getPlayer(server, sender, args[0]);
        String subCommand = args[1].toLowerCase();

        // 2. 根据子命令打开不同的 GUI
        // 注意：你需要在 InventoryGUIExpansion 主类中定义两个不同的 ID，例如 GUI_ID 和 SMELTING_GUI_ID
        if (!targetPlayer.world.isRemote) {
            if ("crafting".equals(subCommand)) {
                targetPlayer.openGui(
                        InventoryGUIExpansion.instance,
                        InventoryGUIExpansion.GUI_ID_CRAFTING_GUI,
                        targetPlayer.world,
                        (int)targetPlayer.posX,
                        (int)targetPlayer.posY,
                        (int)targetPlayer.posZ
                );
            }
            else if ("smelting".equals(subCommand)) {
                targetPlayer.openGui(
                        InventoryGUIExpansion.instance,
                        InventoryGUIExpansion.GUI_ID_SMELTING, // 对应烧炼/修复 GUI
                        targetPlayer.world,
                        (int)targetPlayer.posX,
                        (int)targetPlayer.posY,
                        (int)targetPlayer.posZ
                );
            } else {
                // 如果输入的不是这两个子命令，抛出用法错误
                throw new CommandException(getUsage(sender));
            }
        }
    }

    /**
     * 命令补全提示：增加 smelting 选项
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        } else if (args.length == 2) {
            // 提供 crafting 和 smelting 两个候选词
            return getListOfStringsMatchingLastWord(args, "crafting", "smelting");
        }
        return Collections.emptyList();
    }
}