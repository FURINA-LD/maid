package com.frnc.misc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue DOUBLE_JUMP_ENABLED = BUILDER
            .comment("Whether double jump is enabled by default (can be toggled in-game with the J key)")
            .define("doubleJumpEnabled", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean doubleJumpEnabled;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        doubleJumpEnabled = DOUBLE_JUMP_ENABLED.get();
    }
}
