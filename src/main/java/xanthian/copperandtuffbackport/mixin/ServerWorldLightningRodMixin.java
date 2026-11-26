package xanthian.copperandtuffbackport.mixin;

import net.minecraft.world.level.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ServerLevel.class)
public abstract class ServerWorldLightningRodMixin {
    @Unique
    private static final TagKey<Block> copperandtuffbackport$lightningRods =
            TagKey.create(Registries.BLOCK, ResourceLocation.tryBuild("minecraft", "lightning_rods"));

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean copperandtuffbackport$allRods(BlockState state, Block block, Operation<Boolean> original) {
        boolean result = original.call(state, block);
        // 1.21.9 suppresses skeleton-horse traps at any block in the lightning-rods tag.
        return block == Blocks.LIGHTNING_ROD && result == state.is(block)
                ? state.is(copperandtuffbackport$lightningRods) : result;
    }
}
