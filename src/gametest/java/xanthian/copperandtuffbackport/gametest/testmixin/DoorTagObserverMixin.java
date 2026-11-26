package xanthian.copperandtuffbackport.gametest.testmixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.gametest.CooperationProbe;

@Mixin(value = InteractWithDoor.class, priority = 900)
public abstract class DoorTagObserverMixin {
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD",
            target = "Lnet/minecraft/tags/BlockTags;WOODEN_DOORS:Lnet/minecraft/tags/TagKey;"), require = 3)
    private static TagKey<Block> observeTag(TagKey<Block> original) {
        CooperationProbe.doorTagReads++;
        return original;
    }
}
