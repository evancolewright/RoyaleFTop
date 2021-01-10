package io.github.evancolewright.royaleftop.cmd;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.utils.PageHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTopCommand implements CommandExecutor, TabExecutor
{

    private RoyaleFTop plugin;

    public FTopCommand(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("factionstop"))
        {
            Player player = (Player) sender;
            PageHandler pageHandler = new PageHandler(plugin);
            if (args.length == 0)
            {
                pageHandler.sendPage(player, 1);
                return true;
            } else if (args.length == 1)
            {
                if (isInt(args[0]))
                {
                    pageHandler.sendPage(player, Integer.valueOf(args[0]));
                    return true;
                }

                // Admin commands
                String sub = args[0];
                if (player.isOp())
                {
                    if (sub.equalsIgnoreCase("recalculate"))
                    {
                        player.sendMessage("Recalculate todo");
                    }
                    else if (sub.equalsIgnoreCase("terminate"))
                    {
                        player.sendMessage("Terminate todo");
                    } else if (sub.equalsIgnoreCase("reload"))
                    {
                        player.sendMessage("Reload todo");
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1 && sender.isOp() && sender instanceof Player)
        {
            List<String> subCmds = new ArrayList<>();
            subCmds.add("recalculate");
            subCmds.add("terminate");
            subCmds.add("reload");

            return subCmds;
        }
        return null;
    }

    private boolean isInt(String string)
    {
        try
        {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException exception)
        {
            return false;
        }
    }
}
