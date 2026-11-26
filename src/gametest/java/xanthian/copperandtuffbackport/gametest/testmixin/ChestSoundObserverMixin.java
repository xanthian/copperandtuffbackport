package xanthian.copperandtuffbackport.gametest.testmixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.gametest.CooperationProbe;

@Mixin(ChestBlockEntity.class)
public abstract class ChestSoundObserverMixin {
    @WrapOperation(method = "playSound", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private static void observeSound(Level world, Player player, double x, double y, double z,
                                     SoundEvent sound, SoundSource category, float volume, float pitch, Operation<Void> original) {
        CooperationProbe.soundCalls++;
        CooperationProbe.sound = sound;
        original.call(world, player, x, y, z, sound, category, volume, pitch);
    }
}
