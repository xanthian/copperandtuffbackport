package xanthian.copperandtuffbackport.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(OpenDoorsTask.class)
public abstract class OpenDoorsTaskMixin {
    @Unique
    private static final TagKey<Block> copperandtuffbackport$mobDoors =
            TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "mob_interactable_doors"));

    // 1.21 uses this tag for villagers and other brain-driven door users.
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD",
            target = "Lnet/minecraft/registry/tag/BlockTags;WOODEN_DOORS:Lnet/minecraft/registry/tag/TagKey;"),
            require = 3)
    private static TagKey<Block> copperandtuffbackport$mobInteractableDoors(TagKey<Block> original) {
        return original == BlockTags.WOODEN_DOORS ? copperandtuffbackport$mobDoors : original;
    }
}
