package xanthian.copperandtuffbackport.mixin;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xanthian.copperandtuffbackport.blocks.custom.WeatheringCopperDecorations;

@Mixin(Blocks.class)
public abstract class WeatheringVanillaRodMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "net/minecraft/world/level/block/LightningRodBlock"))
    private static LightningRodBlock copperandtuffbackport$weatheringRod(BlockBehaviour.Properties settings) {
        // Keep minecraft:lightning_rod and Blocks.LIGHTNING_ROD as the original, single registry entry.
        return new WeatheringCopperDecorations.OxidizableCopperLightningRodBlock(WeatheringCopper.WeatherState.UNAFFECTED, settings);
    }
}
