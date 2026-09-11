package com.remonilo.throwspawn.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ThrownSpawnEggEntity extends ThrownItemEntity {

    private static final String ENTITY_TYPE_KEY = "throwspawn_entity_type";
    private EntityType<?> mobType;

    public ThrownSpawnEggEntity(EntityType<? extends ThrownItemEntity> type, World world) {
        super(type, world);
    }

    public ThrownSpawnEggEntity(World world, PlayerEntity owner) {
        super(ModEntities.THROWN_SPAWN_EGG, owner, world);
    }

    public void setMobType(EntityType<?> mobType) {
        this.mobType = mobType;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EGG;
    }

    // NOTE: particle still shows untinted egg texture. SpawnEggItem's color tint isn't applied
    // by the generic ITEM particle type. Would need a custom particle factory to fix; not worth it for now.
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient() && this.mobType != null) {
            Vec3d spawnPos;

            if (hitResult instanceof BlockHitResult blockHitResult) {
                Direction side = blockHitResult.getSide();
                if (side == Direction.UP) {
                    spawnPos = hitResult.getPos();
                } else {
                    Vec3d normal = Vec3d.of(side.getVector());
                    spawnPos = hitResult.getPos().add(normal.multiply(0.5));
                }
            } else {
                spawnPos = hitResult.getPos();
            }

            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.POOF,
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        8, 0.2, 0.2, 0.2, 0.02
                );
            }

            Entity entity = this.mobType.create(this.getWorld());
            if (entity != null) {
                entity.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, this.getYaw(), 0.0F);
                this.getWorld().spawnEntity(entity);
            }
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity hitEntity = entityHitResult.getEntity();
        hitEntity.damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(ENTITY_TYPE_KEY)) {
            EntityType.fromNbt(nbt.getCompound(ENTITY_TYPE_KEY)).ifPresent(t -> this.mobType = t);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.mobType != null) {
            NbtCompound typeNbt = new NbtCompound();
            typeNbt.putString("id", EntityType.getId(this.mobType).toString());
            nbt.put(ENTITY_TYPE_KEY, typeNbt);
        }
    }
}