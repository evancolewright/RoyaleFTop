package io.github.evancolewright.royaleftop.cmd;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FTopReloadCommand implements CommandExecutor
{
    private RoyaleFTop plugin;

    public FTopReloadCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player && label.equalsIgnoreCase("factionstopreload"))
        {
            Player player = (Player) sender;

            if (player.isOp())
            {
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "Config has been reloaded.");
            }
        }
        return false;
    }
}
