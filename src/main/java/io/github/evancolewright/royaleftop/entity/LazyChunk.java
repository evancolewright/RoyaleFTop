package io.github.evancolewright.royaleftop.entity;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

@Getter
public final class LazyChunk
{
    private final int x;
    private final int z;

    private final String worldName;
    private final FactionCache factionCache;

    @Getter
    private final ChunkSnapshot snapshot;

    /**
     * Creates a thread-safe chunk to be calculated.
     *
     * @param x            the x coordinate
     * @param z            the z coordinate
     * @param worldName    the world the chunk is in
     * @param factionCache the faction that this chunk belongs to
     */
    public LazyChunk(int x, int z, String worldName, FactionCache factionCache)
    {
        this.x = x;
        this.z = z;
        this.worldName = worldName;
        this.factionCache = factionCache;

        this.snapshot = this.asChunkSnapshot();
    }

    /**
     * Creates the same instance of a Lazy chunk, but with a ChunkSnapshot as the main param
     *
     * @param snapshot     the snapshot of the chunk to calculate
     * @param factionCache the faction that this chunk belongs to
     */
    public LazyChunk(ChunkSnapshot snapshot, FactionCache factionCache)
    {
        this.x = snapshot.getX();
        this.z = snapshot.getZ();

        this.worldName = snapshot.getWorldName();
        this.factionCache = factionCache;
        this.snapshot = this.asChunkSnapshot();
    }

    public ChunkSnapshot asChunkSnapshot()
    {
        return Bukkit.getWorld(this.worldName).getChunkAt(this.x, this.z).getChunkSnapshot();
    }

    public Location asLocation()
    {
        return Bukkit.getWorld(this.worldName).getBlockAt(this.x << 4, 0, this.z << 4).getLocation();
    }

    public Chunk asChunk()
    {
        return Bukkit.getWorld(worldName).getChunkAt(asLocation());
    }

    /**
     * Overridden toString for testing purposes.
     *
     * @return the chunk as a string
     */
    @Override
    public String toString()
    {
        return "LazyChunk{" +
                "x=" + x +
                ", z=" + z +
                ", worldName='" + worldName + '\'' +
                ", factionCache=" + factionCache +
                '}';
    }
}
