package xanthian.copperandtuffbackport.util;
import java.util.Optional;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
/** Vanilla's age enum keeps old and new copper in the same neighbour-based weathering system. */
public interface CopperWeathering extends WeatheringCopper {
    @Override default Optional<BlockState> getNext(BlockState state) { return CopperTransitions.nextState(state); }
}
