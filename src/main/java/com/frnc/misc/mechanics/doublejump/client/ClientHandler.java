package com.frnc.misc.mechanics.doublejump.client;

import com.frnc.misc.Misc;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** 客户端按键注册: J 键切换二段跳开关 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler
{
    public static final String KEY_CATEGORY = "key.categories.misc";
    public static final String KEY_TOGGLE_DOUBLE_JUMP = "key.misc.double_jump_toggle";

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            KEY_TOGGLE_DOUBLE_JUMP,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KEY_CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event)
    {
        event.register(TOGGLE_KEY);
    }
}
