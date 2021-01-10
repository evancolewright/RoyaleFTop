package io.github.evancolewright.royaleftop.listeners;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListeners implements Listener
{

    private RoyaleFTop plugin;

    public ChatListeners(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
    }
}
