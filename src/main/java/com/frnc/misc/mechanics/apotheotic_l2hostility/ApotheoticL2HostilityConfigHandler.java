package com.frnc.misc.mechanics.apotheotic_l2hostility;

import com.frnc.misc.Config;
import com.frnc.misc.Misc;
import dev.xkmc.l2hostility.init.data.LHConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * 修改 L2Hostility 的 allowNoAI 配置: 默认 false (无 AI 生物不获得等级) -> true。
 *
 * 原因: 神化 (Apotheosis) 刷怪笼的紫颂果修饰器会把刷出的生物设为 NoAI,
 * 而 L2Hostility 的 MobTraitCap 在 mob.isNoAi() 且 allowNoAI=false 时跳过等级/词条初始化。
 * 本处理让 allowNoAI 跟随总开关 apotheosisL2HostilityFixEnabled (默认开启)。
 *
 * applyOverrides() 在 Misc.commonSetup 中调用 (所有模组配置加载完成后执行);
 * ModConfigEvent 处理器用于配置重载时再次应用。
 */
public class ApotheoticL2HostilityConfigHandler
{
    /** 覆盖 L2Hostility 配置 (须在所有配置加载完成后调用) */
    public static void applyOverrides()
    {
        LHConfig.COMMON.allowNoAI.set(Config.apotheosisL2HostilityFixEnabled);
    }

    @Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandler
    {
        /** 配置加载/重载时再次应用覆盖 */
        @SubscribeEvent
        public static void onConfigLoad(final ModConfigEvent event)
        {
            if ("l2hostility".equals(event.getConfig().getModId()))
            {
                applyOverrides();
            }
        }
    }
}
