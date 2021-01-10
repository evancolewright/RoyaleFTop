package io.github.evancolewright.royaleftop.entity;

import lombok.Getter;
import org.bukkit.entity.EntityType;

@Getter
public class SpawnerWorth
{
    private final EntityType entityType;
    private final double worth;
    private final String placeholder;

    public SpawnerWorth(EntityType entityType, double worth, String placeholder)
    {
        this.entityType = entityType;
        this.worth = worth;
        this.placeholder = placeholder;
    }

    /**
     * Replace the placeholder when it is evident in an input.
     *
     * @param input  the initial string
     * @param amount the amount to replace it with
     * @return the new string
     */
    private String replacePlaceholder(String input, int amount)
    {
        return input.replace(placeholder, String.valueOf(amount));
    }

    /**
     * Overridden toString() method for testing purposes.
     *
     * @return the object in easy-to-read string form
     */
    @Override
    public String toString()
    {
        return "SpawnerWorth{" +
                "entityType=" + entityType +
                ", worth=" + worth +
                ", placeholder='" + placeholder + '\'' +
                '}';
    }
}
