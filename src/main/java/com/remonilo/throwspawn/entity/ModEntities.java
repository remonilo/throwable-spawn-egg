package com.remonilo.throwspawn.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.remonilo.throwspawn.ThrowableSpawnEggs;

public class ModEntities {

    public static final EntityType<ThrownSpawnEggEntity> THROWN_SPAWN_EGG = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(ThrowableSpawnEggs.MOD_ID, "thrown_spawn_egg"),
            EntityType.Builder.<ThrownSpawnEggEntity>create(ThrownSpawnEggEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25F, 0.25F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build()
    );

    public static void register() {
        // just needs to be called once to trigger static init above
    }
}