package xanthian.copperandtuffbackport.mixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Optional;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xanthian.copperandtuffbackport.util.CopperTransitions;
@Mixin(HoneycombItem.class)
public abstract class CopperWaxingMapsMixin {
    @ModifyReturnValue(method = "getWaxed", at = @At("RETURN"))
    private static Optional<BlockState> copperandtuffbackport$waxing(Optional<BlockState> original, BlockState state) {
        return original.or(() -> CopperTransitions.waxed(state));
    }
}
