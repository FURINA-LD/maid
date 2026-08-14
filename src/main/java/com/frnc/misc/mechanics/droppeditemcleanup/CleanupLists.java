package com.frnc.misc.mechanics.droppeditemcleanup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * 掉落物清理的维度/物品黑白名单 (参考 CleanMaid 的清单文件):
 *   游戏目录下 misc-blacklist.json / misc-whitelist.json, 格式:
 *     { "items": ["minecraft:diamond"], "dimensions": ["minecraft:overworld"] }
 *   语义: 黑名单 = 必须清理 (命中即清); 白名单 = 受保护 (命中即免清理)。
 *   文件不存在时自动创建为空清单。每次清理开始前会重新加载 (编辑后下次清理生效)。
 */
public class CleanupLists
{
    private static Set<ResourceLocation> itemBlacklist = Set.of();
    private static Set<ResourceLocation> itemWhitelist = Set.of();
    private static Set<ResourceLocation> dimensionBlacklist = Set.of();
    private static Set<ResourceLocation> dimensionWhitelist = Set.of();

    /** 从游戏目录加载黑白名单清单 */
    public static void load()
    {
        Path blacklistFile = FMLPaths.GAMEDIR.get().resolve("misc-blacklist.json");
        Path whitelistFile = FMLPaths.GAMEDIR.get().resolve("misc-whitelist.json");
        itemBlacklist = loadList(blacklistFile, "items");
        dimensionBlacklist = loadList(blacklistFile, "dimensions");
        itemWhitelist = loadList(whitelistFile, "items");
        dimensionWhitelist = loadList(whitelistFile, "dimensions");
    }

    /** 读取文件中的指定 key 的 ResourceLocation 列表; 文件不存在则创建空清单 */
    private static Set<ResourceLocation> loadList(Path file, String key)
    {
        Set<ResourceLocation> result = new HashSet<>();
        try
        {
            if (!Files.exists(file))
            {
                Files.writeString(file, "{\n  \"items\": [],\n  \"dimensions\": []\n}");
                return result;
            }
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            JsonElement elem = root.get(key);
            if (elem != null && elem.isJsonArray())
            {
                for (JsonElement e : (JsonArray) elem)
                {
                    ResourceLocation id = ResourceLocation.tryParse(e.getAsString());
                    if (id != null)
                    {
                        result.add(id);
                    }
                }
            }
        }
        catch (IOException | RuntimeException ignored)
        {
            // 读取失败时保持空清单, 不阻断游戏
        }
        return result;
    }

    public static boolean isItemBlacklisted(ResourceLocation id)
    {
        return itemBlacklist.contains(id);
    }

    public static boolean isItemWhitelisted(ResourceLocation id)
    {
        return itemWhitelist.contains(id);
    }

    public static boolean isDimensionBlacklisted(ResourceLocation id)
    {
        return dimensionBlacklist.contains(id);
    }

    public static boolean isDimensionWhitelisted(ResourceLocation id)
    {
        return dimensionWhitelist.contains(id);
    }
}
