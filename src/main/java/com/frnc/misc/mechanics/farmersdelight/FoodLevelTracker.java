package com.frnc.misc.mechanics.farmersdelight;

import com.frnc.misc.Misc;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 饥饿值基线跟踪器。
 * 供 com.frnc.misc.mixin.NourishmentEffectMixin 使用: 记录各玩家已观测到的最高饥饿值,
 * 饥饿值下降时恢复至基线 (只增不减)。玩家登出时清理基线。
 */
public class FoodLevelTracker
{
    /** 各玩家已观测到的最高饥饿值基线 */
    private static final Map<UUID, Integer> MAX_FOOD_LEVEL = new HashMap<>();

    /**
     * 跟踪并返回应当显示的饥饿值:
     * 首次记录当前值为基线; 后续若 current 低于基线则返回基线 (阻止下降);
     * current 高于基线则更新基线。
     */
    public static int track(UUID playerId, int current)
    {
        Integer baseline = MAX_FOOD_LEVEL.get(playerId);
        if (baseline == null || current > baseline)
        {
            MAX_FOOD_LEVEL.put(playerId, current);
            return current;
        }
        return Math.max(current, baseline);
    }

    /** 玩家登出时清理其基线, 避免 Map 无限增长 */
    @Mod.EventBusSubscriber(modid = Misc.MOD_ID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
        {
            if (event.getEntity() != null)
            {
                MAX_FOOD_LEVEL.remove(event.getEntity().getUUID());
            }
        }
    }
}
