package io.github.evancolewright.royaleftop.tasks;

import com.google.common.collect.Lists;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.models.LazyChunk;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
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

    private int compileTaskID;

    @Getter
    private long previousCalculationTimestamp = 0;

    @Getter
    private static boolean hasFinishedInitialCalcultion = true;

    private ChunkCompilerTask chunkCompiler;

    @Getter
    private boolean compilingChunks = true;


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
            this.chunkCompiler = new ChunkCompilerTask(this, getAllChunks());
            this.startTime = System.currentTimeMillis();
            this.compileTaskID = this.chunkCompiler.runTaskTimer(plugin, 0, 1).getTaskId();
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
            plugin.getServer().getScheduler().cancelTask(compileTaskID);
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

    public void queueChunk(LazyChunk chunk)
    {
        this.chunkStack.add(chunk);
    }

    @Override
    public void run()
    {
        if (chunkCompiler.isRunning())
        {
            return;
        }
        else if (compilingChunks)
        {
            this.compilingChunks = false;
            this.chunkRunnerTask = new ChunkScannerTask(plugin);
            this.initialSize = chunkStack.size();
            this.plugin.getServer().getScheduler().cancelTask(compileTaskID);
            this.currentSize = 0;
            plugin.getCacheManager().getAllFactionCaches().forEach(cache -> cache.reset());
            plugin.getWorthManager().clearLeaderboard();
            this.executorService.submit(this.chunkRunnerTask);
            return;
        }


        int deployAmount = config.getInt("settings.recalcuation_chunks_each_tick");
        while (isRunning() && !this.compilingChunks)
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

        if (!isRunning() && hasFinishedInitialCalcultion)
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
                    executorService.shutdown();
                    plugin.getWorthManager().updateLeaderboard();
                    plugin.getDatabaseManager().saveFTopData();
                    compilingChunks = true;

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

    /**
     * This method was lagging really bad prior.  I resorted to all sorts of different methods.
     * I am relatively satisfied with the ChunkCompiler implementation.  There was really no other way to
     * go about looping through 10k+ objects and calling Spigot methods all at the same time.
     *
     * @return  A list of lists of fLocation
     */
    private List<List<FLocation>> getAllChunks()
    {
        long start = System.currentTimeMillis();
        List<List<FLocation>> allClaims = new ArrayList<>();
        int acc = 0;
        for (Faction faction : plugin.getAllPlayerFactions())
        {
            allClaims.add(new ArrayList<>(faction.getAllClaims()));
        }
        return allClaims;
    }

    protected RoyaleFTop getPlugin()
    {
        return plugin;
    }

    public int getCurrentCompilingIndex()
    {
        return this.chunkCompiler.getCurrentIndex();
    }

    public int getMaxCurrentCompilingIndex()
    {
        return this.chunkCompiler.getMaxIndex();
    }
}