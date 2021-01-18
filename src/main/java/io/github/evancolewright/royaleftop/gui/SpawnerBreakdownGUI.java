package io.github.evancolewright.royaleftop.gui;

import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.FactionCache;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import io.github.evancolewright.royaleftop.utils.MoneyUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SpawnerBreakdownGUI
{
    private final RoyaleFTop plugin;
    private final FileConfiguration config;

    private final Inventory inventory;
    private final FactionCache factionCache;

    public SpawnerBreakdownGUI(RoyaleFTop plugin, FactionCache cache)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.factionCache = cache;

        this.inventory = Bukkit.createInventory(null, this.getInventorySize(),
                ChatUtils.colorize(config.getString("spawner_gui.name")
                        .replace("{FACTION}", Factions.getInstance().getFactionById(cache.getFactionID()).getTag())));
        this.populate();
    }

    private void populate()
    {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();

        filterSpawnerList().forEach(spawner ->
        {
            itemMeta.setOwner(MobHead.getNameByEntity(spawner.getKey()));
            itemMeta.setDisplayName(ChatUtils.colorize(config.getString("spawner_gui.head_name").replace("{SPAWNER}", spawner.getKey().getName())));
            itemMeta.setLore(this.replaceLore(spawner.getKey(), spawner.getValue()));
            itemStack.setItemMeta(itemMeta);
            this.inventory.addItem(itemStack);
        });
    }

    /**
     * Calculates the amount of rows needed for the inventory.
     * Inventory size is based on the amount of spawners present.
     *
     * @return The Inventory size
     */
    private int getInventorySize()
    {
        int spawnerCount = this.filterSpawnerList().size();
        int inventorySize = (int) Math.floor((spawnerCount + 8) / 9) * 9;

        return inventorySize < 9 ? 9 : inventorySize;
    }

    /**
     * Filters the spawner list for values that are != to 0.
     *
     * @return the filtered map
     */

    private Set<Map.Entry<EntityType, Integer>> filterSpawnerList()
    {
        return this.factionCache.getSpawners().entrySet().stream().filter(entry -> entry.getValue() != 0).collect(Collectors.toSet());
    }

    /**
     * Replace common placeholders in the lore for each spawner.
     *
     * @param entityType the entity
     * @param amount     the amount of spawners
     * @return the formatted list
     */
    private List<String> replaceLore(EntityType entityType, int amount)
    {
        double value = plugin.getWorthManager().getWorth(entityType);
        return ChatUtils.colorize(config.getStringList("spawner_gui.head_lore").stream().map(string -> string
                .replace("{AMOUNT}", amount + "")
                .replace("{TOTAL}", MoneyUtils.format(value * amount))
                .replace("{VALUE}", MoneyUtils.format(value)
                )).collect(Collectors.toList()));
    }

    /**
     * Opens the inventory for a player.
     *
     * @param player the player to open it for
     */
    public void open(Player player)
    {
        player.openInventory(this.inventory);
    }
}
