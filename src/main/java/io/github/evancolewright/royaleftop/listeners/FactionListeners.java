package io.github.evancolewright.royaleftop.listeners;

import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import io.github.evancolewright.royaleftop.RoyaleFTop;
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

    }

    @EventHandler
    public void onDisbandFaction(FactionDisbandEvent event)
    {

    }
}
