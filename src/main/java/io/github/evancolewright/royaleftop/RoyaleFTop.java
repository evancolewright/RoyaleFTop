/*
 * GNU General Public License v3.0
 * Please refer to the LICENSE file for the complete license.
 *
 * Copyright (c) 2021 Evan Wright
 */

package io.github.evancolewright.royaleftop;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.cmd.FTopCommand;
import io.github.evancolewright.royaleftop.database.MySQLDatabase;
import io.github.evancolewright.royaleftop.listeners.ChatListeners;
import io.github.evancolewright.royaleftop.listeners.CommandListener;
import io.github.evancolewright.royaleftop.listeners.FactionListeners;
import io.github.evancolewright.royaleftop.managers.CacheManager;
import io.github.evancolewright.royaleftop.managers.DatabaseManager;
import io.github.evancolewright.royaleftop.managers.WorthManager;
import io.github.evancolewright.royaleftop.tasks.RecalculationTask;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class RoyaleFTop extends JavaPlugin
{
    @Getter
    private Economy economy = null;
    private MySQLDatabase mySQL;

    @Getter
    private final Logger log = getLogger();
    private final PluginManager pluginManager = getServer().getPluginManager();

    // Managers
    @Getter
    private WorthManager worthManager;
    @Getter
    private CacheManager cacheManager;
    @Getter
    private DatabaseManager databaseManager;

    // Tasks
    private RecalculationTask recalculationTask;


    private PluginCommand ftopCommand;
    private FTopCommand fTopCommandInstance;

    @Override
    public void onEnable()
    {
        if (this.checkFactions())
        {
            if (setupEconomy())
            {
                log.info("Successfully hooked into Vault and an API provider!");
            } else
            {
                log.warning("Failed to hook into Vault.  You will no longer be able to calculate with player balances!");
            }
            this.initFiles();
            this.worthManager = new WorthManager(this);
            this.cacheManager = new CacheManager(this);
            this.databaseManager = new DatabaseManager(this);

            // Setup MySQL if applicable
            this.databaseManager.setupDatabase();

            this.registerListeners();

            // Create caches
            cacheManager.createCaches();

            ftopCommand = this.getCommand("factionstop");
            fTopCommandInstance = new FTopCommand(this);
            this.getCommand("factionstop").setExecutor(new FTopCommand(this));
            this.getCommand("factionstop").setTabCompleter(new FTopCommand(this));

            // Schedule initial tasks
            recalculationTask = new RecalculationTask(this);
            new BukkitRunnable()
            {

                @Override
                public void run()
                {
                    recalculationTask.initialize();
                }
            }.runTaskLater(this, 150);

        } else
        {
            log.severe("Disabling plugin.....");
            pluginManager.disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        try
        {
            this.databaseManager.getMySQL().closeConnection();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    private void initFiles()
    {
        // Save worths file
//        this.saveResource("worths.yml", false);

        // Save configuration
        this.saveDefaultConfig();
    }

    private void registerListeners()
    {
        this.pluginManager.registerEvents(new FactionListeners(this), this);
        this.pluginManager.registerEvents(new ChatListeners(this), this);
        this.pluginManager.registerEvents(new CommandListener(this), this);
    }

    public Set<Faction> getAllPlayerFactions()
    {
        final Set<Faction> playerFactions = new HashSet<>(Factions.getInstance().getAllFactions());

        // Remove System factions
        playerFactions.remove(Factions.getInstance().getWilderness());
        playerFactions.remove(Factions.getInstance().getSafeZone());
        playerFactions.remove(Factions.getInstance().getWarZone());

        return playerFactions;
    }

    /**
     * Check for the existence of VaultAPI and an Economy provider plugin.
     * If false, you will no longer be able to include player balances in calculation
     *
     * @return  The existence of Vault and an Economy
     */
    private boolean setupEconomy()
    {
        if (this.pluginManager.getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Semi-extensive check for the latest version of FactionsUUID.
     * <p>
     * The old version can be detected by a change in the 'Role' package structure of the API.
     * This plugin is only guaranteed to work with the latest!
     *
     * @return whether or not FUUID latest (after api change) is installed.
     */
    private boolean checkFactions()
    {
        final Plugin factionsPlugin = pluginManager.getPlugin("Factions");

        if (factionsPlugin == null)
        {
            log.severe("No Factions plugin was found! This plugin requires FactionsUUID to run!");
            return false;
        }
        // Check for MassiveFactions first
        if (this.pluginManager.getPlugin("MassiveCore") != null && factionsPlugin.getDescription().getDepend().contains("MassiveCore"))
        {
            log.severe("It appears that you are using MassiveFactions.  That's great, but unfortunately this plugin only supports FactionsUUID.");
            return false;
        } else
        {
            try
            {
                Class.forName("com.massivecraft.factions.perms.Role");  // New package structure (using this to signify the new version)
                log.info("Successfully hooked into FactionsUUID (or a fork of it at least)!");
                return true;
            } catch (ClassNotFoundException e)
            {
                log.severe("It appears you are running an older version of FactionsUUID. FactionsTop is not guaranteed to work with this older versions, so please proceed with caution!");
                return true;
            }
        }
    }

}
