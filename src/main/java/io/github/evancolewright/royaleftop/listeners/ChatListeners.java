package io.github.evancolewright.royaleftop.listeners;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.FactionCache;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.stream.Collectors;

public class ChatListeners implements Listener
{

    private final RoyaleFTop plugin;
    private final FileConfiguration config;

    public ChatListeners(RoyaleFTop plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();
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
                event.setFormat(format.replace("{FTOP_PLACE}", config.getString("chat.no_faction")));
            } else
            {
                int placement = plugin.getWorthManager().getLeaderboardPlacement(factionCache);
                event.setFormat(format.replace("{FTOP_PLACE}", ChatUtils.colorize(this.getColorByPlacement(placement) + config.getString("chat.format_prefix") + String.valueOf(placement))));
            }
        }
    }

    /**
     * Obtain the correct color for the player based on their placement.
     *
     * @param placement the placement of the faction
     * @return the color string
     */
    private String getColorByPlacement(int placement)
    {
        ConfigurationSection colors = config.getConfigurationSection("chat.placements");
        Map<Integer, String> placementColors = colors.getKeys(false).stream().collect(Collectors.toMap(s -> Integer.parseInt(s), s -> colors.getString(s)));

        if (placementColors.containsKey(placement))
            return placementColors.get(placement);

        return config.getString("chat.default_color");
    }
}
