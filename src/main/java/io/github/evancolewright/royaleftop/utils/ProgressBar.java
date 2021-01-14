package io.github.evancolewright.royaleftop.utils;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;

public final class ProgressBar
{
    public static String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor completedColor,
                                        ChatColor notCompletedColor)
    {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars) + Strings.repeat("" + completedColor + symbol, progressBars)
                ;
    }

    public static String getProgressBarCompiling(int current, int max, int totalBars, char symbol, ChatColor completedColor,
                                        ChatColor notCompletedColor)
    {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat("" + notCompletedColor + symbol, progressBars) + Strings.repeat("" + completedColor + symbol, totalBars - progressBars)
                ;
    }
}
