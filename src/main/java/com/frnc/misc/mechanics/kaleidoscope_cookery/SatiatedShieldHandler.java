package com.frnc.misc.mechanics.kaleidoscope_cookery;

import com.frnc.misc.Misc;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 饱腹代偿简化机制 (取代原版 SatiatedShieldEvent 的复杂逻辑):
 *   拥有该效果的玩家受击时全额吸收伤害 (不掉血):
 *   - 优先扣除饱和度, 饱和度不足则扣饥饿值 (每 1 点饱和度/饥饿值抵 1 点伤害)
 *   - 即便伤害超出饱和度+饥饿值, 过量部分也由盾吸收 (吸收过量伤害)
 *   - 食物/饱和度被扣至 0 时仍然全额吸收
 *
 * 原版逻辑已由 KaleidoscopeConfigHandler 关闭 (SATIATED_SHIELD_ABSORB_ENABLED=false)。
 * 虚空/强制击杀等无法豁免的伤害 (BYPASSES_INVULNERABILITY) 不被吸收。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class SatiatedShieldHandler
{
    @SubscribeEvent
    public static void onPlayerHurt(final LivingDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) return;

        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return; // 无法豁免的伤害不吸收

        if (!player.hasEffect(ModEffects.SATIATED_SHIELD.get())) return; // 需拥有饱腹代偿效果

        float damage = event.getAmount();
        FoodData foodData = player.getFoodData();
        float saturation = foodData.getSaturationLevel();
        int food = foodData.getFoodLevel();

        // 优先扣饱和度, 不足再扣饥饿值 (消耗上限 = 当前可用食物, 每 1 点抵 1 点伤害)
        float absorbed = Math.min(damage, saturation + food);
        float satCost = Math.min(saturation, absorbed);
        foodData.setSaturation(saturation - satCost);
        float foodCost = absorbed - satCost;
        foodData.setFoodLevel(Math.max(0, food - (int) Math.ceil(foodCost)));

        // 全额吸收: 即便伤害超出饱和度+饥饿值, 过量部分也由盾吸收, 不掉血
        event.setAmount(0.0F);
    }
}
