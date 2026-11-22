package xanthian.copperandtuffbackport.testmixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.CooperationProbe;

@Mixin(value = OpenDoorsTask.class, priority = 900)
public abstract class DoorTagObserverMixin {
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD",
            target = "Lnet/minecraft/registry/tag/BlockTags;WOODEN_DOORS:Lnet/minecraft/registry/tag/TagKey;"), require = 3)
    private static TagKey<Block> observeTag(TagKey<Block> original) {
        CooperationProbe.doorTagReads++;
        return original;
    }
}
