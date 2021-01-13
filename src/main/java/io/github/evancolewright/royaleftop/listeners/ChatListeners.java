package io.github.evancolewright.royaleftop.listeners;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.FactionCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListeners implements Listener
{

    private RoyaleFTop plugin;

    public ChatListeners(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        final String format = event.getFormat();

        if (format.contains("{FTOP_PLACE}"))
        {
            FactionCache factionCache = plugin.getCacheManager().getCacheByFaction(fPlayer.getFaction());

            if (factionCache == null || !plugin.getWorthManager().getSortedLeaderBoard().containsKey(factionCache))
            {
                event.setFormat(format.replace("{FTOP_PLACE}", "-"));
            }
            else
            {
                event.setFormat(format.replace("{FTOP_PLACE}", String.valueOf(plugin.getWorthManager().getLeaderboardPlacement(factionCache))));
            }
        }
    }
}
