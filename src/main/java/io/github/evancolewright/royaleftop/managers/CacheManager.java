package io.github.evancolewright.royaleftop.managers;

import com.massivecraft.factions.Faction;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.entity.FactionCache;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CacheManager
{

    private RoyaleFTop plugin;

    @Getter
    private final List<FactionCache> allFactionCaches = new ArrayList<>();

    public CacheManager(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Creates all Faction Caches for every faction server wide.
     *
     * This should only happen on server start.
     */
    public void createCaches()
    {
        this.plugin.getAllPlayerFactions().forEach(playerFaction -> this.allFactionCaches.add(new FactionCache(playerFaction)));
    }

    /**
     * Retrieve a FactionCache from a Faction ID
     *
     * @param factionID  the id of the faction to fetch from
     * @return  the faction cache (or null)
     */
    public FactionCache getCacheByFactionID(String factionID)
    {
        return this.allFactionCaches.stream().filter(factionCache ->
                factionCache.getFactionID().equals(factionID))
                .findAny()
                .orElse(null);
    }

    /**
     * Retrieve a FactionCache from a Faction object
     *
     * @param faction  the faction to fetch a Cache for
     * @return  the faction cache (or null)
     */
    public FactionCache getCacheByFaction(Faction faction)
    {
        String id = faction.getId();
        return this.allFactionCaches.stream().filter(factionCache ->
                factionCache.getFactionID().equals(id))
                .findAny()
                .orElse(null);
    }

    /**
     * Retrieve a FactionCache from a Faction ID
     *
     * @param faction  the id of the faction to fetch from
     * @return  the faction cache (or null)
     */
    public void createCache(Faction faction)
    {
        this.allFactionCaches.add(new FactionCache(faction));
    }

    /**
     * Delete a FactionCache for a faction
     * @param faction
     */
    public void deleteCache(Faction faction)
    {
        FactionCache cache = this.getCacheByFaction(faction);
        this.allFactionCaches.remove(cache);
    }
}
