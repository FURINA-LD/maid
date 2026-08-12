package com.frnc.misc.mechanics.kaleidoscope_cookery;

import com.frnc.misc.Misc;
import com.github.ysbbbbbb.kaleidoscopecookery.config.GeneralConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

/**
 * 修改森罗物语：厨房「饱腹代偿 (Satiated Shield)」:
 * 关闭原版 SATIATED_SHIELD_ABSORB_ENABLED, 使其复杂伤害逻辑 (减免百分比/疲劳/封顶等) 不再生效,
 * 由 SatiatedShieldHandler 以 Forge 事件实现简化的"饱和度+饥饿值抵伤"机制。
 *
 * 通过 ForgeConfigSpec.ConfigValue#set() 覆盖配置值。
 * applyOverrides() 在 Misc.commonSetup 中调用 (所有模组配置加载完成后执行, 保证可靠生效);
 * ModConfigEvent 处理器用于配置重载时再次应用。
 */
public class KaleidoscopeConfigHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 覆盖饱腹代偿默认配置 (须在所有配置加载完成后调用) */
    public static void applyOverrides()
    {
        GeneralConfig.SATIATED_SHIELD_ABSORB_ENABLED.set(false);
        LOGGER.info("[misc] SatiatedShield override -> absorbEnabled={}", GeneralConfig.SATIATED_SHIELD_ABSORB_ENABLED.get());
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
