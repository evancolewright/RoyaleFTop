package io.github.evancolewright.royaleftop.tasks;

import io.github.evancolewright.royaleftop.RoyaleFTop;
import io.github.evancolewright.royaleftop.entity.FactionCache;
import io.github.evancolewright.royaleftop.entity.LazyChunk;
import io.github.evancolewright.royaleftop.managers.CacheManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkScannerTask implements Runnable
{
    private final RoyaleFTop plugin;
    protected final BlockingQueue<LazyChunk> queue = new LinkedBlockingQueue<>();

    protected volatile int accumulator = 0;

    public ChunkScannerTask(RoyaleFTop plugin)
    {
        this.plugin = plugin;
    }

    protected synchronized int getQueueSize()
    {
        return queue.size();
    }

    @Override
    public void run()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                accumulator++;
                System.out.print(accumulator + " :");
                LazyChunk currentChunk;
                try
                {
                    currentChunk = queue.take();
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
                ChunkSnapshot snapshot = currentChunk.getSnapshot();
                for (int y = 0; y < 256; y++)
                {
                    for (int x = 0; x < 16; x++)
                    {
                        for (int z = 0; z < 16; z++)
                        {
                            Material material = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));

                            if (material == Material.MOB_SPAWNER)
                            {
                                // Anon classes require final variable copies ;/
                                int finalX = x;
                                int finalY = y;
                                int finalZ = z;

                                // Run on the main thread.
                                new BukkitRunnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        final Chunk chunk = Bukkit.getWorld(snapshot.getWorldName()).getChunkAt(snapshot.getX(), snapshot.getZ());
                                        final BlockState blockState = chunk.getBlock(finalX, finalY, finalZ).getState();
                                        if (!(blockState instanceof CreatureSpawner))
                                        {
                                            return;
                                        }
                                        final CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
                                        plugin.getWorthManager().updateSpawnerWorth(currentChunk.getFactionCache(), creatureSpawner.getSpawnedType());

                                    }
                                }.runTask(plugin);
                            } else
                            {
                                plugin.getWorthManager().updateBlockWorth(currentChunk.getFactionCache(), material);
                            }
                        }
                    }
                }
            }

            if (Thread.currentThread().isInterrupted() || !Thread.currentThread().isAlive())
            {
                System.out.println("========Interupted! or dead");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}


//    private final RoyaleFTop plugin;
//    private CacheManager cacheManager;
////    private volatile Queue<LazyChunk> chunkQueue = new ArrayDeque<>();
//
//    @Getter
//    Stack<LazyChunk> chunks = new Stack<>();
//    private int accumulator = 0;
//
//    public ChunkScannerTask(RoyaleFTop plugin, Stack<LazyChunk> chunks)
//    {
//        this.plugin = plugin;
//        this.cacheManager = plugin.getCacheManager();
//        this.chunks = chunks;
//    }
//
////    protected int getQueueSize()
////    {
////        return chunkQueue.size();
////    }
////
////    protected void queue(LazyChunk chunk)
////    {
////        this.chunkQueue.add(chunk);
////    }
//
//    @Override
//    public void run()
//    {
//        LazyChunk chunk = null;
//        try
//        {
//            chunk = chunks.pop();
//        } catch (Exception exception)
//        {
//            this.cancel();
//            return;
//        }
//        accumulator++;
//        System.out.print(accumulator + "");
//        ChunkSnapshot snapshot = chunk.getSnapshot();
//        FactionCache cache = chunk.getFactionCache();
//        for (int y = 0; y < 256; y++)
//        {
//            for (int x = 0; x < 16; x++)
//            {
//                for (int z = 0; z < 16; z++)
//                {
//                    Material material = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
//                    if (material == Material.MOB_SPAWNER)
//                    {
//                        int finalX = x;
//                        int finalY = y;
//                        int finalZ = z;
//
//                        // Run on the main thread.
//                        new BukkitRunnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                final Chunk chunk = Bukkit.getWorld(snapshot.getWorldName()).getChunkAt(snapshot.getX(), snapshot.getZ());
//                                final BlockState blockState = chunk.getBlock(finalX, finalY, finalZ).getState();
//                                if (!(blockState instanceof CreatureSpawner))
//                                {
//                                    return;
//                                }
//                                final CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
//                                plugin.getWorthManager().updateSpawnerWorth(cache, creatureSpawner.getSpawnedType());
//                            }
//                        }.runTask(plugin);
//                    } else
//                    {
//                        plugin.getWorthManager().updateBlockWorth(cache, material);
//                    }
//                }
//            }
//        }
//    }
