package com.frnc.misc.mechanics.apotheotic_l2hostility;

import com.frnc.misc.Config;
import com.frnc.misc.Misc;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * 让神化 (Apotheosis) 刷怪笼刷出的生物也获得 L2Hostility 的等级与词条。
 *
 * 原因: Apotheosis 刷怪笼在 LyingLevel (仅实现 WorldGenLevel 的伪造 ServerLevel 包装) 中
 * 触发 MobSpawnEvent.FinalizeSpawn, 导致 L2Hostility 的 CapabilityEvents.initMob 里
 * ChunkDifficulty.at(fakeLevel, pos) 取不到区块难度, 从而跳过 MobTraitCap.init (等级/词条初始化)。
 *
 * 本处理在生物加入真实世界 (EntityJoinLevelEvent, 此时 level 为真实 ServerLevel) 时,
 * 对"是 L2Hostility 目标但尚未初始化"的生物重新执行等级初始化, 使神化刷怪笼生物也拥有等级与词条。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class ApotheoticL2HostilityHandler
{
    @SubscribeEvent
    public static void onEntityJoin(final EntityJoinLevelEvent event)
    {
        if (!Config.apotheosisL2HostilityFixEnabled) return; // 总开关
        Level level = event.getLevel();
        if (level.isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        // 非 L2Hostility 目标 (配置排除的实体/类型) 直接跳过
        if (!MobTraitCap.HOLDER.isProper(living)) return;

        MobTraitCap cap = (MobTraitCap) MobTraitCap.HOLDER.get(living);
        if (cap.isInitialized()) return; // 已在刷怪时正常初始化 (含自然生成等)

        // 在真实世界中查找区块难度并初始化, 修复神化刷怪笼因 LyingLevel 导致的跳过升级
        Optional<ChunkDifficulty> difficulty = ChunkDifficulty.at(level, living.blockPosition());
        if (difficulty.isPresent())
        {
            cap.init(level, living, difficulty.get());
        }
    }
}
