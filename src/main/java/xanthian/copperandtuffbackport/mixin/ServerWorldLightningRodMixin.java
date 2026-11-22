package xanthian.copperandtuffbackport.mixin;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ServerWorld.class)
public abstract class ServerWorldLightningRodMixin {
    @Unique
    private static final TagKey<Block> copperandtuffbackport$lightningRods =
            TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "lightning_rods"));

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean copperandtuffbackport$allRods(BlockState state, Block block, Operation<Boolean> original) {
        boolean result = original.call(state, block);
        // 1.21.9 suppresses skeleton-horse traps at any block in the lightning-rods tag.
        return block == Blocks.LIGHTNING_ROD && result == state.isOf(block)
                ? state.isIn(copperandtuffbackport$lightningRods) : result;
    }
}
