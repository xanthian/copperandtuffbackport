package xanthian.copperandtuffbackport.mixin;

import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xanthian.copperandtuffbackport.blocks.custom.WeatheringCopperDecorations;

@Mixin(Blocks.class)
public abstract class WeatheringVanillaRodMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "net/minecraft/block/LightningRodBlock"))
    private static LightningRodBlock copperandtuffbackport$weatheringRod(AbstractBlock.Settings settings) {
        // Keep minecraft:lightning_rod and Blocks.LIGHTNING_ROD as the original, single registry entry.
        return new WeatheringCopperDecorations.OxidizableCopperLightningRodBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings);
    }
}
