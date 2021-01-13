package io.github.evancolewright.royaleftop.listeners;

import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.FactionCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListeners implements Listener
{

    private RoyaleFTop plugin;

    public FactionListeners(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreateFaction(FactionCreateEvent event)
    {
        plugin.getCacheManager().createCache(event.getFaction());
        plugin.getWorthManager().addLeaderboard(plugin.getCacheManager().getCacheByFaction(event.getFaction()));
    }

    @EventHandler
    public void onDisbandFaction(FactionDisbandEvent event)
    {
        FactionCache cache = plugin.getCacheManager().getCacheByFaction(event.getFaction());
        plugin.getWorthManager().removeLeaderboard(cache);
        plugin.getCacheManager().deleteCache(event.getFaction());
    }
    @EventHandler
    public void onLeaveEvent(FPlayerLeaveEvent event)
    {
        if (event.getFaction().getFPlayerAdmin().equals(event.getfPlayer()))
        {
            FactionCache cache = plugin.getCacheManager().getCacheByFaction(event.getFaction());
            plugin.getWorthManager().removeLeaderboard(cache);
            plugin.getCacheManager().deleteCache(event.getFaction());
        }
    }
}
