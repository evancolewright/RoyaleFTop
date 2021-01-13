package io.github.evancolewright.royaleftop.models;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class FactionCache
{
    private final String factionID;
    private UUID leaderUUID;

    private volatile Map<Material, Integer> blocks = new HashMap<>();
    private volatile Map<EntityType, Integer> spawners = new HashMap<>();

    /**
     * Creates a volatile storage cache for a faction.
     * <p>
     * For now, Main thread will not be permitted to read/edit
     * values from this class while our recalculation thread is active.
     * <p>
     * Meaning, only 1 thread will be given access to this class at once.
     * Also meaning, thread safe.
     *
     * This is subject to change, depending on my recalculation implementation.
     *
     * @param faction the faction to store info about
     */
    public FactionCache(Faction faction)
    {
        final Factions factions = Factions.getInstance();
        this.factionID = faction.getId();
        this.leaderUUID = faction.getFPlayerAdmin().getOfflinePlayer().getUniqueId();
    }

    public void addBlock(Material material)
    {
        int currentCount = getBlockCount(material);

        this.blocks.put(material, ++currentCount);
    }

    public void addSpawner(EntityType entityType)
    {
        int currentCount = getSpawnerCount(entityType);

        this.spawners.put(entityType, ++currentCount);
    }

    public int getBlockCount(Material material)
    {
        Integer amount = blocks.get(material);
        return amount != null ? amount : 0;
    }

    public int getSpawnerCount(EntityType entityType)
    {
        Integer amount = spawners.get(entityType);
        return amount != null ? amount : 0;
    }


    /**
     * Reset the cache to default values
     * usually prior to a recalculation.
     *
     * Please see CacheManager for deleting caches.
     */
    public void reset()
    {
        this.blocks = new HashMap<>();
        this.spawners = new HashMap<>();
    }
}
