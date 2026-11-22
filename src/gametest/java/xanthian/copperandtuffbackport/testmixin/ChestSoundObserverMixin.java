package xanthian.copperandtuffbackport.testmixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.CooperationProbe;

@Mixin(ChestBlockEntity.class)
public abstract class ChestSoundObserverMixin {
    @WrapOperation(method = "playSound", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private static void observeSound(World world, PlayerEntity player, double x, double y, double z,
                                     SoundEvent sound, SoundCategory category, float volume, float pitch, Operation<Void> original) {
        CooperationProbe.soundCalls++;
        CooperationProbe.sound = sound;
        original.call(world, player, x, y, z, sound, category, volume, pitch);
    }
}
