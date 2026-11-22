package xanthian.copperandtuffbackport.testmixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.CooperationProbe;

@Mixin(value = LightningEntity.class, priority = 900)
public abstract class LightningOperationObserverMixin {
    @WrapOperation(method = "powerLightningRod", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean observePower(BlockState state, Block block, Operation<Boolean> original) {
        CooperationProbe.powerCalls++;
        return original.call(state, block);
    }

    @WrapOperation(method = "cleanOxidation", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private static boolean observeCleaning(BlockState state, Block block, Operation<Boolean> original) {
        CooperationProbe.cleaningCalls++;
        return original.call(state, block);
    }
}
