package io.github.evancolewright.royaleftop.models;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public class BlockWorth
{
    private final Material material;
    private final double worth;
    private final String placeholder;

    public BlockWorth(Material material, double worth, String placeholder)
    {
        this.material = material;
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
        return "BlockWorth{" +
                "material=" + material +
                ", worth=" + worth +
                ", placeholder='" + placeholder + '\'' +
                '}';
    }
}
