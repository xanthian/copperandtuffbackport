package xanthian.copperandtuffbackport.gametest.testmixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.gametest.CooperationProbe;

@Mixin(value = LightningBolt.class, priority = 900)
public abstract class LightningOperationObserverMixin {
    @WrapOperation(method = "powerLightningRod", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean observePower(BlockState state, Block block, Operation<Boolean> original) {
        CooperationProbe.powerCalls++;
        return original.call(state, block);
    }

    @WrapOperation(method = "clearCopperOnLightningStrike", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean observeCleaning(BlockState state, Block block, Operation<Boolean> original) {
        CooperationProbe.cleaningCalls++;
        return original.call(state, block);
    }
}
