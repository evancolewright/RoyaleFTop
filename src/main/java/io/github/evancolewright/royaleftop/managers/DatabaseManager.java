package io.github.evancolewright.royaleftop.managers;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.database.MySQLDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
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
                                "faction_name VARCHAR(16) NOT NULL," +
                                "faction_leader_uuid VARCHAR(36) NOT NULL," +
                                "spawner_worth FLOAT NOT NULL," +
                                "block_worth FLOAT NOT NULL," +
                                "money_worth FLOAT)");
                    } catch (ClassNotFoundException | SQLException e)
                    {
                        log.severe("Could not connect to MySQL database!  Please check your credentials and restart your server!");
                    }
                }
            }.runTaskAsynchronously(plugin);

        } else
        {
            log.warning("You opted out of MySQL data dumping!  If you wish to use MySQL, please enable it in the config.yml with appropriate login credentials!");
        }
    }

    public void dumpFactionsData()
    {

    }

}
