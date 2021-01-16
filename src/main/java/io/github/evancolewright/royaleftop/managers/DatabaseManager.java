package io.github.evancolewright.royaleftop.managers;

import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.database.MySQLDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DatabaseManager
{
    private final RoyaleFTop plugin;
    private final FileConfiguration config;
    private final Logger log;

    @Getter
    private MySQLDatabase mySQL;

    public DatabaseManager(io.github.evancolewright.royaleftop.RoyaleFTop plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.log = plugin.getLog();
    }

    /**
     * Setup the Database table for dumping
     *
     */
    public void setupDatabase()
    {
        final ConfigurationSection database = config.getConfigurationSection("database");
        final String host = database.getString("host");
        final String name = database.getString("name");
        final String username = database.getString("username");
        final String password = database.getString("password");

        if (database.getBoolean("enabled"))
        {
            new BukkitRunnable()
            {
                @SneakyThrows
                @Override
                public void run()
                {
                    mySQL = new MySQLDatabase(host, name, username, password);
                    mySQL.openConnection();
                    log.info("Successfully connected to a MySQL Database!");
                    mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS FactionsTop(" +
                            "placement INT NOT NULL PRIMARY KEY," + "faction_name VARCHAR(16) NOT NULL," +
                            "faction_leader_uuid VARCHAR(36) NOT NULL," +
                            "spawner_worth FLOAT NOT NULL," +
                            "block_worth FLOAT NOT NULL)", true);
                }
            }.runTaskAsynchronously(plugin);

        } else
        {
            log.warning("You opted out of MySQL data dumping!  If you wish to use MySQL, please enable it in the config.yml with appropriate login credentials!");
        }
    }

    /**
     * Dumps all FTop data to external DB.
     */
    public void saveFTopData()
    {
        final Factions factions = Factions.getInstance();
        final WorthManager worthManager = plugin.getWorthManager();

        List<CacheData> cacheDataList = worthManager.getSortedLeaderBoard().entrySet().stream().map(entry -> new CacheData(worthManager.getLeaderboardPlacement(entry.getKey()),
                factions.getFactionById(entry.getKey().getFactionID()).getTag(),
                factions.getFactionById(entry.getKey().getFactionID()).getFPlayerAdmin().getOfflinePlayer().getUniqueId().toString(),
                worthManager.getSpawnerWorth(entry.getKey()),
                worthManager.getBlockWorth(entry.getKey())
        )).collect(Collectors.toList());

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                final long startTime = System.currentTimeMillis();
                PreparedStatement preparedStatement = null;
                try
                {
                    mySQL.openConnection();
                    mySQL.executeUpdate("TRUNCATE FactionsTop", false);
                    preparedStatement = mySQL.getConnection().prepareStatement("INSERT INTO FactionsTop(placement, faction_name, faction_leader_uuid, spawner_worth, block_worth) VALUES (?,?,?,?,?)");
                    for (CacheData cache : cacheDataList)
                    {
                        preparedStatement.setString(1, String.valueOf(cache.placement));
                        preparedStatement.setString(2, cache.factionName);
                        preparedStatement.setString(3, cache.factionLeader);
                        preparedStatement.setString(4, String.valueOf(cache.spawnerWorth));
                        preparedStatement.setString(5, String.valueOf(cache.blockWorth));
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                    log.info("Saved all FactionTop data to external MySQL database!  Took: " + (System.currentTimeMillis() - startTime) + "ms.");

                } catch (SQLException e)
                {
                    log.severe("Failed to save Faction Data for FactionsTop!");
                    e.printStackTrace();
                } finally
                {
                    try
                    {
                        if (preparedStatement != null)
                            preparedStatement.close();
                    } catch (SQLException exception)
                    {
                        exception.printStackTrace();
                    } finally
                    {
                        mySQL.closeConnection();
                    }
                }

            }
        }.runTaskLaterAsynchronously(this.plugin, 100);
    }


    private class CacheData
    {

        int placement;
        String factionName;
        String factionLeader;
        double spawnerWorth;
        double blockWorth;

        public CacheData(int placement, String factionName, String factionLeader, double spawnerWorth, double blockWorth)
        {
            this.placement = placement;
            this.factionName = factionName;
            this.factionLeader = factionLeader;
            this.spawnerWorth = spawnerWorth;
            this.blockWorth = blockWorth;
        }
    }

}
