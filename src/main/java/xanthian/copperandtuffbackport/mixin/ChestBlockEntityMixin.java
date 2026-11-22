package xanthian.copperandtuffbackport.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xanthian.copperandtuffbackport.blocks.custom.CopperChestBlock;
import xanthian.copperandtuffbackport.util.ModSounds;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {
    @ModifyVariable(method = "playSound", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static SoundEvent copperandtuffbackport$copperSounds(SoundEvent original, World world,
                                                                 BlockPos pos, BlockState state, SoundEvent sound) {
        if (!(state.getBlock() instanceof CopperChestBlock chest)) return original;
        int group = Math.max(0, chest.getOxidationLevel().ordinal() - 1);
        // Leave the original body and other mods' custom sounds intact.
        if (original == SoundEvents.BLOCK_CHEST_OPEN) return ModSounds.COPPER_CHEST_OPEN[group];
        if (original == SoundEvents.BLOCK_CHEST_CLOSE) return ModSounds.COPPER_CHEST_CLOSE[group];
        return original;
    }
}
