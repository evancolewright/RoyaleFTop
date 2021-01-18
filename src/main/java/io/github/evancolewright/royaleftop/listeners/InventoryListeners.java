package io.github.evancolewright.royaleftop.listeners;

import com.massivecraft.factions.Faction;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListeners implements Listener
{
    private final RoyaleFTop plugin;

    public InventoryListeners(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }


    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if (isSpawnerBreakDown(event.getInventory().getName()))
        {
            event.setCancelled(true);
        }
    }

    private boolean isSpawnerBreakDown(String name)
    {
        name = ChatUtils.colorize(name);
        for (Faction faction : plugin.getAllPlayerFactions())
        {
            if (ChatUtils.colorize(plugin.getConfig().getString("spawner_gui.name").replace("{FACTION}", faction.getTag())).equals(name))
            {
                return true;
            }
        }
        return false;
    }
}
