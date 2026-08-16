package com.frnc.misc.mixin;

import com.frnc.misc.mechanics.apotheotic_creation.ApotheosisItemMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复神化词缀装备无法被机械动力仓库管理员 (Stock Keeper) 取出的问题。
 *
 * Create 的物流/库存匹配 (InventorySummary 聚合/计数/擦除) 使用
 * ItemHandlerHelper.canItemStacksStack 做严格 NBT 比较。神化词缀物品 NBT 含随机词缀数值/耐久,
 * 导致按词缀装备取出失败。本 Mixin 使两个神化词缀装备若词缀身份相同 (同物品+稀有度+词缀ID集合)
 * 则判定为"同一种可堆叠", 忽略 NBT 数值差异。词缀装备均不可堆叠 (maxStackSize=1), 无合并丢数据风险。
 */
@Mixin(ItemHandlerHelper.class)
public abstract class ItemHandlerHelperMixin
{
    @Inject(method = "canItemStacksStack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void misc$affixAwareCanStack(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir)
    {
        CompoundTag tagA = a.getTag();
        CompoundTag tagB = b.getTag();
        if (tagA == null || tagB == null) return; // 无 NBT, 交给原逻辑
        if (ApotheosisItemMatcher.isAffixItem(a) && ApotheosisItemMatcher.isAffixItem(b)
                && ApotheosisItemMatcher.sameAffixIdentity(a, b))
        {
            cir.setReturnValue(true);
        }
    }
}
