package io.github.evancolewright.royaleftop.utils;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.entity.BlockWorth;
import io.github.evancolewright.royaleftop.entity.FactionCache;
import io.github.evancolewright.royaleftop.entity.SpawnerWorth;
import io.github.evancolewright.royaleftop.entity.WorthType;
import io.github.evancolewright.royaleftop.managers.WorthManager;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageHandler
{
    private final RoyaleFTop plugin;
    private final FileConfiguration config;
    private final WorthManager worthManager;

    private List<FactionCache> caches;

    public PageHandler(RoyaleFTop plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.worthManager = plugin.getWorthManager();
        this.caches = plugin.getCacheManager().getAllFactionCaches();
    }

    public void sendPage(Player player, int page)
    {
        player.sendMessage(ChatUtils.colorize(config.getString("top_list.top_border")
                .replace("{PAGE}", String.valueOf(page))));

        int perPage = config.getInt("top_list.per_page");
        int start = (page - 1) * perPage;
        int end = start + perPage;
        if (end > this.caches.size())
        {
            end = this.caches.size();
        }

        List<FancyMessage> send = getAllFTopValues().subList(start, end);
        send.forEach(message -> message.send(player));
    }

    private List<FancyMessage> getAllFTopValues()
    {
        List<FancyMessage> messages = new ArrayList<>();
        int acc = 1;
        for (Map.Entry<FactionCache, Double> entry : plugin.getWorthManager().getSortedLeaderBoard().entrySet())
        {
            messages.add(getSingleFTopValue(entry.getKey(), acc));
            acc++;
        }
        return messages;
    }

    private FancyMessage getSingleFTopValue(FactionCache cache, int place)
    {
        double overallWorth = plugin.getWorthManager().getOverallWorth(cache);
        double spawnerWorth = plugin.getWorthManager().getSpawnerWorth(cache);
        double blockWorth = plugin.getWorthManager().getBlockWorth(cache);
        Faction faction = Factions.getInstance().getFactionById(cache.getFactionID());
        String leaderName = faction.getFPlayerAdmin().getName();

        FancyMessage fancyMessage = new FancyMessage(
                replaceCommonPlaceholders(
                        ChatUtils.colorize(
                                config.getString("top_list.format"))
                        , faction, overallWorth, spawnerWorth, blockWorth, place)
        );

        List<String> tooltip = new ArrayList<>(this.getHoverMessage(cache));
        fancyMessage.tooltip(ChatUtils.colorize(tooltip));

        fancyMessage.command("/ftop");

        return fancyMessage;
    }

    private String replaceCommonPlaceholders(String string, Faction faction, double overallWorth, double spawnerWorth, double blockWorth, int place)
    {
        String leaderName = faction.getFPlayerAdmin().getName();
        return ChatUtils.colorize(
                string.replace("{LEADER}", leaderName)
                        .replace("{FACTION}", faction.getTag())
                        .replace("{PLACE}", String.valueOf(place))
                        .replace("{OVERALL_WORTH}", MoneyUtils.format(overallWorth))
                        .replace("{SPAWNER_WORTH}", MoneyUtils.format(spawnerWorth))
                        .replace("{BLOCK_WORTH}", MoneyUtils.format(blockWorth)));
    }

    public List<String> getHoverMessage(FactionCache factionCache)
    {
        List<String> defaultMessage = plugin.getConfig().getStringList("top_list.tooltip");
        Faction faction = Factions.getInstance().getFactionById(factionCache.getFactionID());
        List<String> returnMe = new ArrayList<>();
        for (String s : defaultMessage)
        {
            // Replace default placeholders
            s = s.replace("{FACTION}", faction.getTag())
                    .replace("{LEADER}", faction.getFPlayerAdmin().getName())
                    .replace("{OVERALL_WORTH}", MoneyUtils.format(plugin.getWorthManager().getOverallWorth(factionCache)))
                    .replace("{SPAWNER_WORTH}", MoneyUtils.format(plugin.getWorthManager().getSpawnerWorth(factionCache)))
                    .replace("{BLOCK_WORTH}", MoneyUtils.format(plugin.getWorthManager().getBlockWorth(factionCache)));

            // line
            for (BlockWorth blockWorth : plugin.getWorthManager().getBlockWorths())
            {
                s = s.replace(blockWorth.getPlaceholder(), factionCache.getBlockCount(blockWorth.getMaterial()) + "");
            }
            for (SpawnerWorth spawnerWorth : plugin.getWorthManager().getSpawnerWorths())
            {
                s = s.replace(spawnerWorth.getPlaceholder(), factionCache.getSpawnerCount(spawnerWorth.getEntityType()) + "");
            }
            returnMe.add(ChatUtils.colorize(s));
        }
        return returnMe;
    }
}
