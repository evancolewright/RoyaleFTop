package io.github.evancolewright.royaleftop.cmd;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FTopLockCommand implements CommandExecutor
{
    private RoyaleFTop plugin;

    public FTopLockCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (label.equalsIgnoreCase("factionstoplock") && sender.isOp())
        {
            Player player = (Player) sender;
            boolean locked = plugin.isLocked();

            if (locked)
            {
                plugin.setLocked(false);
                player.sendMessage(ChatColor.GREEN + "FactionsTop has been " + ChatColor.UNDERLINE + "UNLOCKED!");
            }
            else
            {
                plugin.setLocked(true);
                player.sendMessage(ChatColor.GREEN + "FactionsTop has been " + ChatColor.UNDERLINE + "LOCKED!");
            }
        }
        return false;
    }
}
