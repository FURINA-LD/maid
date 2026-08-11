package com.frnc.misc.mechanics.kaleidoscope_cookery;

import com.frnc.misc.Misc;
import com.github.ysbbbbbb.kaleidoscopecookery.config.GeneralConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

/**
 * 修改森罗物语：厨房「饱腹代偿 (Satiated Shield)」的默认配置:
 *   - SATIATED_SHIELD_MIN_FOOD_LEVEL:             最小饥饿值 4 -> 1 (仅需 1 点饥饿即可生效)
 *   - IS_SATIATED_SHIELD_DISABLE_WHEN_HUNGRY_EFFECT: true -> false (带"饥饿"效果时不禁用)
 *   - SATIATED_SHIELD_MAX_DAMAGE_REDUCTION:        64 -> 模组支持的上限 2.147483647E9 (减免封顶取消)
 *
 * 通过 ForgeConfigSpec.ConfigValue#set() 覆盖配置值。
 * applyOverrides() 在 Misc.commonSetup 中调用 (所有模组配置加载完成后执行, 保证可靠生效);
 * ModConfigEvent 处理器用于配置重载时再次应用。
 */
public class KaleidoscopeConfigHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 森罗物语 SATIATED_SHIELD_MAX_DAMAGE_REDUCTION 的 defineInRange 上限 (即 Integer.MAX_VALUE) */
    private static final double MAX_DAMAGE_REDUCTION_CAP = 2.147483647E9;

    /** 覆盖饱腹代偿默认配置 (须在所有配置加载完成后调用) */
    public static void applyOverrides()
    {
        GeneralConfig.SATIATED_SHIELD_MIN_FOOD_LEVEL.set(1);
        GeneralConfig.IS_SATIATED_SHIELD_DISABLE_WHEN_HUNGRY_EFFECT.set(false);
        GeneralConfig.SATIATED_SHIELD_MAX_DAMAGE_REDUCTION.set(MAX_DAMAGE_REDUCTION_CAP);

        LOGGER.info("[misc] SatiatedShield override -> minFood={}, disableWhenHungry={}, maxReduction={}",
                GeneralConfig.SATIATED_SHIELD_MIN_FOOD_LEVEL.get(),
                GeneralConfig.IS_SATIATED_SHIELD_DISABLE_WHEN_HUNGRY_EFFECT.get(),
                GeneralConfig.SATIATED_SHIELD_MAX_DAMAGE_REDUCTION.get());
    }

    @Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandler
    {
        /** 配置加载/重载时再次应用覆盖 (ModConfigEvent 同时覆盖 Loading / Reloading) */
        @SubscribeEvent
        public static void onConfigLoad(final ModConfigEvent event)
        {
            if ("kaleidoscope_cookery".equals(event.getConfig().getModId()))
            {
                applyOverrides();
            }
        }
    }
}
