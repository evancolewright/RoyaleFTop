package io.github.evancolewright.royaleftop.listeners;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener
{
    private RoyaleFTop plugin;

    public CommandListener(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Re-route the typical /f top command to our /ftop command executor.
     *
     * @param event the event
     */
    @EventHandler
    public void onDispatchCommand(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        Player player = event.getPlayer();

        if (message.startsWith("/f top") || message.startsWith("/factions top"))
        {
            event.setCancelled(true);
            if (message.equalsIgnoreCase("/f top") || message.equalsIgnoreCase("/factions top"))
            {
                // No page number was given, default to page 1.
                player.performCommand("ftop");
            } else
            {
                String[] args = message.split(" ");
                if (args.length == 3)
                {
                    if (isInt(args[2]))
                    {
                        player.performCommand("ftop " + Integer.valueOf(args[2]));
                        return;
                    }
                    player.performCommand("ftop 1");
                }
            }
        }
    }

    private boolean isInt(String input)
    {
        try
        {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException exception)
        {
            return false;
        }
    }

}
