package xanthian.copperandtuffbackport.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.level.block.Block;

@Mixin(InteractWithDoor.class)
public abstract class OpenDoorsTaskMixin {
    @Unique
    private static final TagKey<Block> copperandtuffbackport$mobDoors =
            TagKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "mob_interactable_doors"));

    // 1.21 uses this tag for villagers and other brain-driven door users.
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD",
            target = "Lnet/minecraft/tags/BlockTags;WOODEN_DOORS:Lnet/minecraft/tags/TagKey;"),
            require = 3)
    private static TagKey<Block> copperandtuffbackport$mobInteractableDoors(TagKey<Block> original) {
        return original == BlockTags.WOODEN_DOORS ? copperandtuffbackport$mobDoors : original;
    }
}
