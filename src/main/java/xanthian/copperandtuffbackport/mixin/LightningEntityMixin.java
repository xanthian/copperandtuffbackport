package xanthian.copperandtuffbackport.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(LightningEntity.class)
public abstract class LightningEntityMixin {
    @WrapOperation(method = "powerLightningRod", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean copperandtuffbackport$allRods(BlockState state, Block block, Operation<Boolean> original) {
        boolean result = original.call(state, block);
        // Preserve explicit changes made by another wrapper; extend vanilla's otherwise unchanged check.
        if (block == Blocks.LIGHTNING_ROD && result == state.isOf(block)) {
            return state.getBlock() instanceof LightningRodBlock;
        }
        return result;
    }

    @WrapOperation(method = "cleanOxidation", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private static boolean copperandtuffbackport$cleanStruckBlock(BlockState state, Block block, Operation<Boolean> original) {
        boolean result = original.call(state, block);
        // 1.21.9 cleans the struck rod itself; 1.20.1 first skips to its supporting block.
        return block == Blocks.LIGHTNING_ROD && result == state.isOf(block) ? false : result;
    }
}
