package io.github.evancolewright.royaleftop.cmd;

import com.sun.javaws.progress.Progress;
import io.github.evancolewright.royaleftop.RoyaleFTop;
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

public class FTopCommand implements CommandExecutor, TabExecutor
{

    private RoyaleFTop plugin;

    public FTopCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("factionstop"))
        {
            Player player = (Player) sender;
            PageHandler pageHandler = new PageHandler(plugin);
            if (args.length == 0)
            {
                if (plugin.getRecalculationTask().isRunning())
                {
                    sendProgressMessage(player);
                    return false;
                }
                pageHandler.sendPage(player, 1);
                return true;
            } else if (args.length == 1)
            {
                if (plugin.getRecalculationTask().isRunning())
                {
                    sendProgressMessage(player);
                    return false;
                }
                if (isInt(args[0]))
                {
                    pageHandler.sendPage(player, Integer.valueOf(args[0]));
                    return true;
                }

                // Admin commands
                String sub = args[0];
                if (player.isOp())
                {
                    if (sub.equalsIgnoreCase("recalculate"))
                    {
                        if (plugin.getRecalculationTask().isRunning())
                        {
                            player.sendMessage(ChatColor.RED + "Recalculation task is already running!");

                        } else {
                            plugin.getRecalculationTask().initialize();
                        }
                    } else if (sub.equalsIgnoreCase("terminate"))
                    {
                        if (plugin.getRecalculationTask().isRunning())
                        {
                            plugin.getRecalculationTask().terminate();
                            player.sendMessage(ChatColor.GREEN + "Recalculation task has been cancelled.");

                        } else {
                            player.sendMessage(ChatColor.RED + "A recalculation task is not running!");
                        }
                    } else if (sub.equalsIgnoreCase("reload"))
                    {
                        plugin.reloadConfig();
                        player.sendMessage(ChatColor.GREEN + "Config reloaded.");
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1 && sender.isOp() && sender instanceof Player)
        {
            List<String> subCmds = new ArrayList<>();
            subCmds.add("recalculate");
            subCmds.add("terminate");
            subCmds.add("reload");

            return subCmds;
        }
        return null;
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

    private void sendProgressMessage(Player player)
    {
        List<String> recalc = new ArrayList<>();

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
