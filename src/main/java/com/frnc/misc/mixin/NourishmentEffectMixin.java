package com.frnc.misc.mixin;

import com.frnc.misc.mechanics.farmersdelight.FoodLevelTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.effect.NourishmentEffect;

/**
 * 修改农夫乐事 Nourishment (滋养) buff: 在原效果 (每 tick 重置消耗度) 的基础上,
 * 新增"阻止饥饿值下降"。
 *
 * 在 applyEffectTick 返回后注入 (原效果已重置消耗度):
 * FoodData.tick 在效果 tick 之前执行, 因此在 RETURN 注入能捕获本 tick 的饥饿变化,
 * 通过 FoodLevelTracker 将饥饿值锁定在已观测到的最高水平 (只增不减)。
 */
@Mixin(NourishmentEffect.class)
public abstract class NourishmentEffectMixin
{
    @Inject(method = "applyEffectTick", at = @At("RETURN"))
    private void misc$preventHungerLoss(LivingEntity entity, int amplifier, CallbackInfo ci)
    {
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        FoodData foodData = player.getFoodData();
        foodData.setFoodLevel(FoodLevelTracker.track(player.getUUID(), foodData.getFoodLevel()));
    }
}
