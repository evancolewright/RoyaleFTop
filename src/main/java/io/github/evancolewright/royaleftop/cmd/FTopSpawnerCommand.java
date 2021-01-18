package io.github.evancolewright.royaleftop.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.gui.SpawnerBreakdownGUI;
import io.github.evancolewright.royaleftop.models.FactionCache;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class FTopSpawnerCommand implements CommandExecutor
{

    private final RoyaleFTop plugin;

    public FTopSpawnerCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("ftopspawners"))
        {
            Player player = (Player) sender;

            if (args.length == 0)
            {
                player.sendMessage(ChatColor.RED + "/ftopspawners <faction>");
            } else if (args.length == 1)
            {
                Faction faction = Factions.getInstance().getByTag(args[0]);
                if (plugin.getCacheManager().getCacheByFaction(faction) != null)
                {
                    new SpawnerBreakdownGUI(plugin, plugin.getCacheManager().getCacheByFaction(faction)).open(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Faction not found!");
                }
            }
        }
        return false;
    }
}
