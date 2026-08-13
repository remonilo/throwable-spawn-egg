package com.remonilo.throwspawn.mixin;

import com.remonilo.throwspawn.entity.ThrownSpawnEggEntity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void throwspawn$onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        SpawnEggItem self = (SpawnEggItem) (Object) this;

        if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            cir.setReturnValue(TypedActionResult.pass(stack));
            return;
        }

        if (!world.isClient()) {
            EntityType<?> mobType = self.getEntityType(stack);

            ThrownSpawnEggEntity egg = new ThrownSpawnEggEntity(world, user);
            egg.setMobType(mobType);
            egg.setItem(stack);
            egg.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.0F, 1.0F);
            world.spawnEntity(egg);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 0.5F,
                    0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        }

        user.getItemCooldownManager().set(stack.getItem(), 15);
        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        cir.setReturnValue(TypedActionResult.success(stack));
    }
}