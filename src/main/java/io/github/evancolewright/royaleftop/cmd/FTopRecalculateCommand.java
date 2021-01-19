package io.github.evancolewright.royaleftop.cmd;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FTopRecalculateCommand implements CommandExecutor
{

    private RoyaleFTop plugin;

    public FTopRecalculateCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (label.equalsIgnoreCase("factionstoprecalculate") && sender.isOp())
        {
            if (plugin.getRecalculationTask().isRunning())
            {
                sender.sendMessage(ChatColor.RED + "Recalculation task is already running!");

            } else
            {
                plugin.getRecalculationTask().initialize();
            }
        }
        return false;
    }
}
