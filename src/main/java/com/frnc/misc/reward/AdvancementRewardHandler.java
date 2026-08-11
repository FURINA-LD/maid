package com.frnc.misc.reward;

import com.frnc.misc.Misc;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Optional;

// 完成进度后玩家永久获得 farmersdelight:nourishment (Nourishment II) buff。
// 关键语义:
//   - 时长硬编码为 -1(Minecraft 的无限时长标记, 与任何数据文件无关)
//   - amplifier = 1 => Nourishment II
//   - HUD 显示图标(showIcon = true)
@Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AdvancementRewardHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 触发进度: 女仆模组「好感度提升至最大」 */
    private static final ResourceLocation TRIGGER_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath("touhou_little_maid", "favorability/favorability_increased_max");
    /** 奖励效果: 农夫乐事 Nourishment */
    private static final ResourceLocation REWARD_EFFECT = ResourceLocation.fromNamespaceAndPath("farmersdelight", "nourishment");
    /** 效果等级: 1 => Nourishment II */
    private static final int AMPLIFIER = 1;
    /** 永久时长标记 (Minecraft 中 duration == -1 即无限时长) */
    private static final int INFINITE_DURATION = -1;

    /** 进度完成时立即给予效果 */
    @SubscribeEvent
    public static void onAdvancementEarn(final AdvancementEvent.AdvancementEarnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && TRIGGER_ADVANCEMENT.equals(event.getAdvancement().getId()))
        {
            applyPermanentNourishment(serverPlayer);
        }
    }

    /** 已完成的玩家登录时重新施加, 保证跨重登仍然生效 (对应 c6c 的 checkAndApplyAllCompletedRewards 于登录时调用) */
    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && hasCompleted(serverPlayer))
        {
            applyPermanentNourishment(serverPlayer);
        }
    }

    /** 复活后重新施加 (死亡会清除所有效果, 保证"永久"在死亡后仍成立) */
    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && hasCompleted(serverPlayer))
        {
            applyPermanentNourishment(serverPlayer);
        }
    }

    /**
     * 阻止牛奶等效果清除机制移除永久 Nourishment buff。
     * 牛奶通过 LivingEntity.removeAllEffects() 清除所有效果, 其中会对每个效果触发可取消的
     * MobEffectEvent.Remove; 对已完成进度的玩家取消该移除即可令 buff 免疫牛奶。
     */
    @SubscribeEvent
    public static void onEffectRemove(final MobEffectEvent.Remove event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && hasCompleted(serverPlayer)
                && REWARD_EFFECT.equals(BuiltInRegistries.MOB_EFFECT.getKey(event.getEffect())))
        {
            event.setCanceled(true);
        }
    }

    /** 判断玩家是否已完成触发进度 */
    private static boolean hasCompleted(final ServerPlayer player)
    {
        if (player.getServer() == null)
        {
            return false;
        }
        final ServerAdvancementManager advancementManager = player.getServer().getAdvancements();
        final Advancement advancement = advancementManager.getAdvancement(TRIGGER_ADVANCEMENT);
        if (advancement == null)
        {
            return false;
        }
        final PlayerAdvancements playerAdvancements = player.getAdvancements();
        final AdvancementProgress progress = playerAdvancements.getOrStartProgress(advancement);
        return progress.isDone();
    }

    /** 应用永久 Nourishment 效果 */
    private static void applyPermanentNourishment(final ServerPlayer player)
    {
        final Optional<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getOptional(REWARD_EFFECT);
        if (effect.isEmpty())
        {
            LOGGER.warn("[misc] 效果 {} 未注册, 无法给予永久 Nourishment", REWARD_EFFECT);
            return;
        }

        //   new MobEffectInstance(effect, duration=-1, amplifier, ambient=false, visible=false, showIcon=true)
        final MobEffectInstance instance = new MobEffectInstance(
                effect.get(),
                INFINITE_DURATION,
                AMPLIFIER,
                false, // ambient
                false, // visible (粒子)
                true   // showIcon (HUD 显示图标)
        );
        player.addEffect(instance);
        LOGGER.info("[misc] 已给予玩家 {} 永久 {} (Nourishment {})", player.getName().getString(), REWARD_EFFECT, AMPLIFIER + 1);
    }
}
