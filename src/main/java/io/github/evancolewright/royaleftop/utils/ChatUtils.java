package io.github.evancolewright.royaleftop.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class ChatUtils
{
    public static String colorize(String string)
    {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colorize(List<String> strings)
    {
        return strings.stream().map(ChatUtils::colorize).collect(Collectors.toList());
    }

    public static void sendMessage(CommandSender to, String string)
    {
        to.sendMessage(colorize(string));
    }

    public static void sendMessage(CommandSender to, List<String> strings)
    {
        List<String> colored = colorize(strings);
        colored.forEach(to::sendMessage);
    }
}
