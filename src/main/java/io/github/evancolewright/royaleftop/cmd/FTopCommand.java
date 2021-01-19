package io.github.evancolewright.royaleftop.cmd;

import com.sun.javaws.progress.Progress;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.tasks.RecalculationTask;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import io.github.evancolewright.royaleftop.utils.PageHandler;
import io.github.evancolewright.royaleftop.utils.ProgressBar;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTopCommand implements CommandExecutor
{

    private RoyaleFTop plugin;

    public FTopCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player && (label.equalsIgnoreCase("factionstop") || label.equalsIgnoreCase("ftop")))
        {
            Player player = (Player) sender;
            PageHandler pageHandler = new PageHandler(plugin);
            RecalculationTask recalculationTask = plugin.getRecalculationTask();

            if (recalculationTask.isRunning())
            {
                if (plugin.getRecalculationTask().isCompilingChunks())
                {
                    sendProgressMessage(player, true);
                    return true;
                } else
                {
                    sendProgressMessage(player, false);
                    return false;
                }
            } else
            {
                if (args.length == 0)
                {
                    pageHandler.sendPage(player, 1);
                } else if (args.length == 1)
                {
                    if (isInt(args[0]))
                    {
                        pageHandler.sendPage(player, Integer.parseInt(args[0]));
                    } else
                    {
                        player.sendMessage(ChatColor.RED + "Usage: /ftop <page>");
                    }
                } else
                {
                    player.sendMessage(ChatColor.RED + "Usage: /ftop <page>");
                }
            }

        }
        return false;
    }

    private boolean isInt(String string)
    {
        try
        {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException exception)
        {
            return false;
        }
    }

    private void sendProgressMessage(Player player, boolean compilingChunks)
    {
        List<String> recalc = new ArrayList<>();

        if (compilingChunks)
        {
            for (String s : plugin.getConfig().getStringList("messages.compiling_ftop"))
            {
                recalc.add(s.replace("{PROGRESS}",
                        ProgressBar.getProgressBarCompiling(
                                plugin.getRecalculationTask().getCurrentCompilingIndex(),
                                plugin.getRecalculationTask().getMaxCurrentCompilingIndex()
                                ,
                                40,
                                '|',
                                ChatColor.RED, ChatColor.GREEN)));
            }

            ChatUtils.sendMessage(player, recalc);

        } else
        {
            for (String s : plugin.getConfig().getStringList("messages.calculating_ftop"))
            {
                recalc.add(s.replace("{PROGRESS}",
                        ProgressBar.getProgressBar(
                                plugin.getRecalculationTask().getChunkStackSize(),
                                plugin.getRecalculationTask().getInitialSize(),
                                40,
                                '|',
                                ChatColor.RED, ChatColor.GREEN)));
            }

            ChatUtils.sendMessage(player, recalc);
        }


    }


}