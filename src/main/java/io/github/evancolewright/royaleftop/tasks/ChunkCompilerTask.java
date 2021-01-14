package io.github.evancolewright.royaleftop.tasks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import io.github.evancolewright.royaleftop.models.FactionCache;
import io.github.evancolewright.royaleftop.models.LazyChunk;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ChunkCompilerTask extends BukkitRunnable
{
    private RecalculationTask recalculationTask;
    private List<FLocation> allChunks;

    @Getter
    private int currentIndex = 0;

    @Getter
    private int maxIndex;

    @Getter
    private boolean isRunning = true;

    public ChunkCompilerTask(RecalculationTask recalculationTask, List<List<FLocation>> allChunks)
    {
        this.recalculationTask = recalculationTask;
        this.allChunks = compileToSingleList(allChunks);

        this.maxIndex = this.allChunks.size() - 1;
    }


    @Override
    public void run()
    {
        for (int i = 0; i < 10; i++)
        {
            if (this.currentIndex > this.maxIndex)
            {
                this.cancel();
                this.isRunning = false;
                return;
            }
            FLocation location = this.allChunks.get(currentIndex);
            Faction faction = Board.getInstance().getFactionAt(location);
            FactionCache cache = recalculationTask.getPlugin().getCacheManager().getCacheByFaction(faction);
            recalculationTask.queueChunk(new LazyChunk(location.getChunk().getX(), location.getChunk().getZ(), location.getWorldName(), cache));
            currentIndex++;
        }
    }

    private List<FLocation> compileToSingleList(List<List<FLocation>> allChunks)
    {
        List<FLocation> toReturn = new ArrayList<>();
        for (List<FLocation> faction : allChunks)
        {
            for (FLocation location : faction)
            {
                toReturn.add(location);
            }
        }

        return toReturn;
    }
}
