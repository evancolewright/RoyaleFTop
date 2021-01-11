package io.github.evancolewright.royaleftop.tasks;

import com.google.common.collect.Lists;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.entity.LazyChunk;
import io.github.evancolewright.royaleftop.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
            this.chunkRunnerTask = new ChunkScannerTask(plugin);
            chunkStack.addAll(getAllChunks());
            this.initialSize = chunkStack.size();
            this.currentSize = 0;
            this.startTime = System.currentTimeMillis();
            plugin.getCacheManager().getAllFactionCaches().forEach(cache -> cache.reset());
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
            plugin.getServer().getScheduler().cancelTask(taskID);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!hasFinishedInitialCalcultion)
                    {
                        hasFinishedInitialCalcultion = true;
                    }
                    executorService.shutdownNow();
                    previousCalculationTimestamp = System.currentTimeMillis();
                    Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, getCompletionMessage(System.currentTimeMillis() - startTime)));
                    chunkRunnerTask = new ChunkScannerTask(plugin);
                    plugin.getWorthManager().updateLeaderboard();

                }
            }.runTaskLater(this.plugin, 60);
        }
    }

    private List<String> getCompletionMessage(long miliSeconds)
    {
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(miliSeconds) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds) % 60;
        String time = String.format("%2dm%2ds", min, sec);

        List<String> formattedCompletionMessage = new ArrayList<>();
        config.getStringList("messages.recalculation_completed").forEach(s -> formattedCompletionMessage.add(s.replace("{TIME}", time)));
        return formattedCompletionMessage;
    }

    private List<LazyChunk> getAllChunks()
    {
        List<LazyChunk> chunks = Lists.newArrayList();
        for (Faction faction : plugin.getAllPlayerFactions())
        {
            for (FLocation location : faction.getAllClaims())
            {
                LazyChunk lazyChunk = new LazyChunk((int) location.getX(), (int) location.getZ(), location.getWorld().getName(), plugin.getCacheManager().getCacheByFaction(faction));
                chunks.add(lazyChunk);
            }
        }
        return chunks;
    }

}









//    private final RoyaleFTop plugin;
//    private final FileConfiguration config;
//
//    private ExecutorService executorService;
//    @Getter
//    private final Stack<LazyChunk> chunkStack = new Stack<>();
//    private ChunkScannerTask chunkRunnerTask;
//
//    private volatile boolean isDone = false;
//
//    private boolean scheduled;
//    private long startTime;
//    private int taskID;
//
//    public RecalculationTask(RoyaleFTop plugin)
//    {
//        this.plugin = plugin;
//        this.config = plugin.getConfig();
//    }
//
//    public void initialize()
//    {
//        if (!isRunning())
//        {
//            chunkStack.addAll(getAllChunks());
//            this.startTime = System.currentTimeMillis();
//
//            // reset all current caches
//            plugin.getCacheManager().getAllFactionCaches().forEach(factionCache -> factionCache.reset());
//
//            this.chunkRunnerTask = new ChunkScannerTask(plugin, this.chunkStack);
////            this.executorService.submit(chunkRunnerTask);
//            this.taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, chunkRunnerTask, 0,1);
//
//            this.chunkRunnerTask.runTaskTimer(plugin, 0, 1);
//            Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, config.getStringList("messages.recalculation_starting")));
//        }
//    }
//
//    public boolean isRunning()
//    {
//        return !chunkStack.isEmpty() && !chunkRunnerTask.chunks.isEmpty();
//    }
//
////    /**
////     * Termination onCommand only!
////     */
////    public void terminate()
////    {
////        if (isRunning())
////        {
////            chunkStack.clear();
////            plugin.getServer().getScheduler().cancelTask(taskID);
////
////            // Interrupt our thread
////            executorService.shutdownNow();
////            Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, config.getStringList("messages.recalculation_completed")));
////        }
////    }
////
////    @Override
////    public void run()
////    {
////        int deployAmount = config.getInt("settings.recalcuation_chunks_each_tick");
////        while (isRunning())
////        {
////            if (deployAmount-- <= 0)
////            {
////                break;
////            }
////            LazyChunk lazyChunk = this.chunkStack.pop();
////            Chunk chunk = lazyChunk.asChunk();
////
////            if ( chunk != null && chunk.load() && Factions.getInstance().getFactionById(lazyChunk.getFactionCache().getFactionID()) != null)
////            {
////                this.chunkRunnerTask.queue(lazyChunk);
////            }
////        }
////
////        if (!isRunning())
////        {
////            if (!scheduled)
////            {
////                new BukkitRunnable()
////                {
////                    @Override
////                    public void run()
////                    {
////                        executorService.shutdownNow();
////                        plugin.getServer().getScheduler().cancelTask(taskID);
////                        Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, getCompletionMessage(System.currentTimeMillis() - startTime)));
////                    }
////                }.runTaskLater(plugin, 60);
////            }
////            scheduled = true;
////        }
////
////    }
//
//    private List<String> getCompletionMessage(long miliSeconds)
//    {
//        int min = (int) TimeUnit.MILLISECONDS.toMinutes(miliSeconds) % 60;
//        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds) % 60;
//        String time = String.format("%2dm%2ds", min, sec);
//
//        List<String> formattedCompletionMessage = new ArrayList<>();
//        for (String s : config.getStringList("messages.recalculation_completed"))
//        {
//            formattedCompletionMessage.add(s.replace("{TIME}", time));
//        }
//        return formattedCompletionMessage;
//    }
//
//    private List<LazyChunk> getAllChunks()
//    {
//        List<LazyChunk> chunks = Lists.newArrayList();
//        for (Faction faction : plugin.getAllPlayerFactions())
//        {
//            for (FLocation location : faction.getAllClaims())
//            {
//                LazyChunk lazyChunk = new LazyChunk((int) location.getX(), (int) location.getZ(), location.getWorld().getName(), plugin.getCacheManager().getCacheByFaction(faction));
//                chunks.add(lazyChunk);
//            }
//        }
//        return chunks;
//    }
