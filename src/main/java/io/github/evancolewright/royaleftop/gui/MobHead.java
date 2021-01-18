package io.github.evancolewright.royaleftop.gui;

import org.bukkit.entity.EntityType;

import java.util.Arrays;

public enum MobHead
{
    /**
     * Maps the Player head name to the entity type
     * from the official minecraft head docs.
     *
     * https://minecraft.gamepedia.com/Head
     */

    PIG("MHF_Pig", EntityType.PIG),
    COW("MHF_Cow", EntityType.COW),
    ZOMBIE("MHF_Zombie", EntityType.ZOMBIE),
    SKELETON("MHF_Skeleton", EntityType.SKELETON),
    SPIDER("MHF_Spider", EntityType.SPIDER),
    SLIME("MHF_Slime", EntityType.SLIME),
    SHEEP("MHF_Sheep", EntityType.SHEEP),
    IRON_GOLEM("MHF_Golem", EntityType.IRON_GOLEM),
    BLAZE("MHF_Blaze", EntityType.BLAZE),
    ENDERMAN("MHF_Enderman", EntityType.ENDERMAN),
    ZOMBIE_PIGMAN("MHF_PigZombie", EntityType.PIG_ZOMBIE),
    CREEPER("MHF_Creeper", EntityType.CREEPER),
    CAVE_SPIDER("MHF_CaveSpider", EntityType.CAVE_SPIDER),
    CHICKEN("MHF_Chicken", EntityType.CHICKEN);

    private final String headName;
    private final EntityType entityType;

    MobHead(String headName, EntityType entityType)
    {
        this.headName = headName;
        this.entityType = entityType;
    }

    public static String getNameByEntity(EntityType entityType)
    {
        return Arrays.stream(MobHead.values()).filter(head -> head.entityType == entityType).findFirst().get().headName;
    }
}
