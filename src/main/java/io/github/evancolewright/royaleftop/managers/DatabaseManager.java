package io.github.evancolewright.royaleftop.managers;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.database.MySQLDatabase;
import io.github.evancolewright.royaleftop.models.FactionCache;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

    public void setupDatabase()
    {
        final ConfigurationSection database = config.getConfigurationSection("database");
        final String host = database.getString("host");
        final String name = database.getString("name");
        final String username = database.getString("username");
        final String password = database.getString("password");

        if (database.getBoolean("enabled"))
        {
            // Dispatch to another thread because this process will easily freeze the main thread.
            new BukkitRunnable()
            {
                @SneakyThrows
                @Override
                public void run()
                {
                    mySQL = new MySQLDatabase(host, name, username, password);
                    try
                    {
                        mySQL.openConnection();
                        log.info("Successfully connected to a MySQL Database!");
                        mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS FactionsTop(" +
                                "placement INT NOT NULL PRIMARY KEY," + "faction_name VARCHAR(16) NOT NULL," +
                                "faction_leader_uuid VARCHAR(36) NOT NULL," +
                                "spawner_worth FLOAT NOT NULL," +
                                "block_worth FLOAT NOT NULL)");
                    } catch (ClassNotFoundException | SQLException e)
                    {
                       e.printStackTrace();
                    }
                    finally
                    {
                        mySQL.closeConnection();
                    }
                }
            }.runTaskAsynchronously(plugin);

        } else
        {
            log.warning("You opted out of MySQL data dumping!  If you wish to use MySQL, please enable it in the config.yml with appropriate login credentials!");
        }
    }

    public void saveFTopData()
    {
        List<CacheData> cacheDataList = new ArrayList<>();
        final WorthManager worthManager = plugin.getWorthManager();
        for (Map.Entry<FactionCache, Double> entry : plugin.getWorthManager().getSortedLeaderBoard().entrySet())
        {
            Faction faction = Factions.getInstance().getFactionById(entry.getKey().getFactionID());
            cacheDataList.add(new CacheData(worthManager.getLeaderboardPlacement(entry.getKey()), faction.getTag(), faction.getFPlayerAdmin().getOfflinePlayer().getUniqueId().toString(), worthManager.getSpawnerWorth(entry.getKey()), worthManager.getBlockWorth(entry.getKey())));
        }
        new BukkitRunnable()
        {
            @SneakyThrows
            @Override
            public void run()
            {
                try
                {
                    mySQL.openConnection();
                    mySQL.executeUpdate("TRUNCATE FactionsTop");
                    PreparedStatement statement = mySQL.getConnection().prepareStatement("INSERT INTO FactionsTop(placement, faction_name, faction_leader_uuid, spawner_worth, block_worth) VALUES (?,?,?,?,?)");
                    cacheDataList.forEach(cache -> {
                        try
                        {
                            statement.setString(1, String.valueOf(cache.placement));
                            statement.setString(2, cache.factionName);
                            statement.setString(3, cache.factionLeader);
                            statement.setString(4, String.valueOf(cache.spawnerWorth));
                            statement.setString(5, String.valueOf(cache.blockWorth));
                            statement.addBatch();
                        } catch (SQLException throwables)
                        {
                            throwables.printStackTrace();
                        }
                    });
                    statement.executeBatch();

                } catch (ClassNotFoundException | SQLException e)
                {
                    e.printStackTrace();
                }
                finally {
                   mySQL.closeConnection();
                }

            }
        }.runTaskLater(this.plugin, 100);
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
