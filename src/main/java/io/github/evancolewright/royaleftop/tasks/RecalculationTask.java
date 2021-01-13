package io.github.evancolewright.royaleftop.tasks;

import com.google.common.collect.Lists;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.LazyChunk;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RecalculationTask implements Runnable
{
    private final RoyaleFTop plugin;
    private final FileConfiguration config;

    private ExecutorService executorService;
    private final Stack<LazyChunk> chunkStack = new Stack<>();
    private ChunkScannerTask chunkRunnerTask;

    private long startTime;
    private int taskID;

    // Progress bar
    @Getter
    private int initialSize = 0;
    @Getter
    private int currentSize;

    private boolean isCompilingChunks;

    @Getter
    private long previousCalculationTimestamp = 0;

    @Getter
    private static boolean hasFinishedInitialCalcultion = false;


    public RecalculationTask(RoyaleFTop plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        executorService = Executors.newSingleThreadExecutor();
    }

    public void initialize()
    {
        if (!isRunning())
        {
            if (executorService.isShutdown())
            {
                this.executorService = Executors.newSingleThreadExecutor();
            }
            isCompilingChunks = true;
            this.chunkRunnerTask = new ChunkScannerTask(plugin);
            this.initialSize = chunkStack.size();
            this.currentSize = 0;
            this.startTime = System.currentTimeMillis();
            plugin.getCacheManager().getAllFactionCaches().forEach(cache -> cache.reset());
            plugin.getWorthManager().clearLeaderboard();
            this.executorService.submit(this.chunkRunnerTask);
            this.taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 1);
            Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, config.getStringList("messages.recalculation_starting")));
        }
    }

    public void terminate()
    {
        if (isRunning())
        {
            chunkStack.clear();
            plugin.getServer().getScheduler().cancelTask(taskID);
            executorService.shutdown();
            Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, config.getStringList("messages.recalculation_completed")));
        }
    }

    public boolean isRunning()
    {
        return !chunkStack.isEmpty();
    }


    public int getChunkStackSize()
    {
        return this.chunkStack.size();
    }

    @Override
    public void run()
    {
        int deployAmount = config.getInt("settings.recalcuation_chunks_each_tick");
        while (isRunning())
        {
            if (deployAmount-- <= 0)
            {
                break;
            }

            this.currentSize++;
            LazyChunk lazyChunk = this.chunkStack.pop();
            Chunk chunk = lazyChunk.asChunk();
            Faction faction = Factions.getInstance().getFactionById(lazyChunk.getFactionCache().getFactionID());

            if (chunk != null && chunk.load() && faction != null)
            {
                this.chunkRunnerTask.queue.add(lazyChunk);
            }
        }

        if (!isRunning())
        {
            this.plugin.getServer().getScheduler().cancelTask(taskID);
            this.plugin.getWorthManager().updateLeaderboard();
            Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, getCompletionMessage(System.currentTimeMillis() - startTime)));
            this.previousCalculationTimestamp = System.currentTimeMillis();

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!hasFinishedInitialCalcultion)
                    {
                        hasFinishedInitialCalcultion = true;
                    }
                    executorService.shutdown();
                    plugin.getWorthManager().updateLeaderboard();
                    plugin.getDatabaseManager().saveFTopData();

                }
            }.runTaskLater(this.plugin, 60);
        }
    }

    private List<String> getCompletionMessage(long milliSeconds)
    {
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(milliSeconds) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(milliSeconds) % 60;
        String time = String.format("%2dm%2ds", min, sec);

        List<String> formattedCompletionMessage = new ArrayList<>();
        config.getStringList("messages.recalculation_completed").forEach(s -> formattedCompletionMessage.add(s.replace("{TIME}", time)));
        return formattedCompletionMessage;
    }

//    private int getAllChunks()
//    {
//        List<LazyChunk> chunks = Lists.newArrayList();
//        Bukkit.broadcastMessage("starting all claims");
//        int acc = 0;
//        for (Faction faction : plugin.getAllPlayerFactions())
//        {
//            for (FLocation location : faction.getAllClaims())
//            {
//            }
//        }
//    }
}