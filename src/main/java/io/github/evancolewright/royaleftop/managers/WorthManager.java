package io.github.evancolewright.royaleftop.managers;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.BlockWorth;
import io.github.evancolewright.royaleftop.models.FactionCache;
import io.github.evancolewright.royaleftop.models.SpawnerWorth;
import io.github.evancolewright.royaleftop.utils.WorthSorter;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorthManager
{
    private final RoyaleFTop plugin;
    private final FileConfiguration config;

    @Getter
    private final List<BlockWorth> blockWorths = new ArrayList<>();
    @Getter
    private final List<SpawnerWorth> spawnerWorths = new ArrayList<>();
    @Getter
    private final Map<FactionCache, Double> leaderboard = new HashMap<>();

    public WorthManager(RoyaleFTop plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.loadWorths();
    }

    public double getSpawnerWorth(FactionCache factionCache)
    {
        double spawnerWorth = 0.0;
        for (Map.Entry<EntityType, Integer> spawner : factionCache.getSpawners().entrySet())
        {
            EntityType type = spawner.getKey();
            int amount = spawner.getValue();
            spawnerWorth += (long) (getSpawnerWorth(type) * amount);
        }

        return spawnerWorth;
    }

    public double getBlockWorth(FactionCache factionCache)
    {
        double blockWorth = 0.0;
        for (Map.Entry<Material, Integer> block : factionCache.getBlocks().entrySet())
        {
            Material type = block.getKey();
            int amount = block.getValue();
            blockWorth += (long) (getBlockWorth(type) * amount);
        }

        return blockWorth;
    }

    public double getOverallWorth(FactionCache cache)
    {
        return this.getBlockWorth(cache) + this.getSpawnerWorth(cache);
    }

    public void updateBlockWorth(FactionCache cache, Material material)
    {
        AtomicBoolean found = new AtomicBoolean(false);
        blockWorths.forEach(blockWorth ->
        {
            if (blockWorth.getMaterial() == material)
                found.set(true);
        });

        if (found.get())
            cache.addBlock(material);
    }

    public void updateSpawnerWorth(FactionCache cache, EntityType entityType)
    {
        AtomicBoolean found = new AtomicBoolean(false);
        spawnerWorths.forEach(spawnerWorth ->
        {
            if (spawnerWorth.getEntityType() == entityType)
                found.set(true);
        });
        if (found.get())
        {
            cache.addSpawner(entityType);
        }

    }

    public double getWorth(EntityType entityType)
    {
        return this.spawnerWorths.stream().filter(spawnerWorth -> spawnerWorth.getEntityType() == entityType).findFirst().get().getWorth();
    }

    public Map<FactionCache, Double> getSortedLeaderBoard()
    {
        WorthSorter<FactionCache, Double> sorter = new WorthSorter<>(this.leaderboard);

        return sorter.getSortedMap();
    }

    public void updateLeaderboard()
    {
        this.leaderboard.clear();
        for (FactionCache cache : plugin.getCacheManager().getAllFactionCaches())
        {
            this.leaderboard.put(cache, this.getOverallWorth(cache));
        }
    }

    public int getLeaderboardPlacement(FactionCache cache)
    {
        int accumulator = 1;
        for (Map.Entry<FactionCache, Double> entry : this.getSortedLeaderBoard().entrySet())
        {
            if (entry.getKey().equals(cache))
            {
                return accumulator;
            }
            accumulator++;
        }
        return -99;
    }

    public void clearLeaderboard()
    {
        this.leaderboard.clear();
    }

    private double getBlockWorth(Material material)
    {
        Optional<BlockWorth> block = blockWorths.stream().filter(blockWorth -> blockWorth.getMaterial() == material).findAny();
        return block.map(BlockWorth::getWorth).orElse(0.0);
    }

    private double getSpawnerWorth(EntityType entityType)
    {
        Optional<SpawnerWorth> spawner = spawnerWorths.stream().filter(spawnerWorth -> spawnerWorth.getEntityType() == entityType).findAny();
        return spawner.map(SpawnerWorth::getWorth).orElse(0.0);
    }

    public void addLeaderboard(FactionCache cache)
    {
        this.leaderboard.put(cache, 0.0);
    }

    public void removeLeaderboard(FactionCache cache)
    {
        if (leaderboard.containsKey(cache))
        {
            this.leaderboard.remove(cache);
        }
    }

    /**
     * Load worth values from the config
     */
    private void loadWorths()
    {
        final ConfigurationSection blocks = config.getConfigurationSection("worths.blocks");
        final ConfigurationSection spawners = config.getConfigurationSection("worths.spawners");
        blocks.getKeys(false).forEach(string ->
        {
            Material material = Material.matchMaterial(blocks.getString(string + ".type"));
            double worth = blocks.getDouble(string + ".worth");
            String placeholder = blocks.getString(string + ".placeholder");
            blockWorths.add(new BlockWorth(material, worth, placeholder));
        });
        spawners.getKeys(false).forEach(string ->
        {
            EntityType entity = EntityType.valueOf(spawners.getString(string + ".type"));
            double worth = spawners.getDouble(string + ".worth");
            String placeholder = spawners.getString(string + ".placeholder");
            spawnerWorths.add(new SpawnerWorth(entity, worth, placeholder));
        });
    }


}
