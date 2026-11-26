package xanthian.copperandtuffbackport.mixin;

import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(LightningBolt.class)
public abstract class LightningEntityMixin {
    @WrapOperation(method = "clearCopperOnLightningStrike", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/WeatheringCopper;getFirst(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState copperandtuffbackport$cleanModCopper(BlockState state, Operation<BlockState> original) {
        return xanthian.copperandtuffbackport.util.CopperTransitions.first(original.call(state));
    }

    @WrapOperation(method = "randomStepCleaningCopper", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/WeatheringCopper;getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private static java.util.Optional<BlockState> copperandtuffbackport$cleanNearbyModCopper(BlockState state, Operation<java.util.Optional<BlockState>> original) {
        return original.call(state).or(() -> xanthian.copperandtuffbackport.util.CopperTransitions.previous(state));
    }
    @WrapOperation(method = "powerLightningRod", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean copperandtuffbackport$allRods(BlockState state, Block block, Operation<Boolean> original) {
        boolean result = original.call(state, block);
        // Preserve explicit changes made by another wrapper; extend vanilla's otherwise unchanged check.
        if (block == Blocks.LIGHTNING_ROD && result == state.is(block)) {
            return state.getBlock() instanceof LightningRodBlock;
        }
        return result;
    }

    @WrapOperation(method = "clearCopperOnLightningStrike", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean copperandtuffbackport$cleanStruckBlock(BlockState state, Block block, Operation<Boolean> original) {
        boolean result = original.call(state, block);
        // 1.21.9 cleans the struck rod itself; 1.20.1 first skips to its supporting block.
        return block == Blocks.LIGHTNING_ROD && result == state.is(block) ? false : result;
    }
}
