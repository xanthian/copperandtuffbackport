package xanthian.copperandtuffbackport.mixin;

import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PointOfInterestTypes.class)
public interface PointOfInterestTypesInvoker {
    @Invoker("registerStates")
    static void copperandtuffbackport$registerStates(RegistryEntry<PointOfInterestType> type, Set<BlockState> states) {
        throw new AssertionError();
    }
}
