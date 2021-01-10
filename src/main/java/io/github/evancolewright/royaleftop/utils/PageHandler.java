package io.github.evancolewright.royaleftop.utils;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.entity.FactionCache;
import io.github.evancolewright.royaleftop.entity.WorthType;
import io.github.evancolewright.royaleftop.managers.WorthManager;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
        for (int i = 0; i < caches.size(); i++)
        {
            messages.add(getSingleFTopValue(caches.get(i), i + 1));
        }
        return messages;
    }

    private FancyMessage getSingleFTopValue(FactionCache cache, int place)
    {
        double overallWorth = plugin.getWorthManager().getSpawnerWorth(cache);
        Bukkit.broadcastMessage(overallWorth + "eeee");
        double spawnerWorth = 5000.00;
        double blockWorth = 5000.00;
        Faction faction = Factions.getInstance().getFactionById(cache.getFactionID());
        String leaderName = faction.getFPlayerAdmin().getName();

        FancyMessage fancyMessage = new FancyMessage(
                replaceCommonPlaceholders(
                        ChatUtils.colorize(
                                config.getString("top_list.format"))
                        , faction, overallWorth, spawnerWorth, blockWorth, place)
        );

        List<String> tooltip = new ArrayList<>(config.getStringList("top_list.tooltip"));
        fancyMessage.tooltip(ChatUtils.colorize(tooltip));

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
}
