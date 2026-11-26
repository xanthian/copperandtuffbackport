package xanthian.copperandtuffbackport.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Optional;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class CopperTransitions {
    private static final BiMap<Block, Block> WEATHERING = HashBiMap.create();
    private static final BiMap<Block, Block> WAXING = HashBiMap.create();
    public static void registerOxidizableBlockPair(Block from, Block to) { WEATHERING.put(from, to); }
    public static void registerWaxableBlockPair(Block from, Block to) { WAXING.put(from, to); }
    public static Optional<Block> next(Block block) {
        return Optional.ofNullable(WEATHERING.get(block)).or(() -> WeatheringCopper.getNext(block));
    }
    public static Optional<BlockState> nextState(BlockState state) {
        return next(state.getBlock()).map(block -> block.withPropertiesOf(state));
    }
    public static Optional<BlockState> previous(BlockState state) {
        return WeatheringCopper.getPrevious(state).or(() -> Optional.ofNullable(WEATHERING.inverse().get(state.getBlock())).map(block -> block.withPropertiesOf(state)));
    }
    public static BlockState first(BlockState state) {
        Block current = state.getBlock();
        while (WEATHERING.inverse().containsKey(current)) current = WEATHERING.inverse().get(current);
        return WeatheringCopper.getFirst(current.withPropertiesOf(state));
    }
    public static Optional<BlockState> waxed(BlockState state) {
        return Optional.ofNullable(WAXING.get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }
    public static Optional<BlockState> unwaxed(BlockState state) {
        return Optional.ofNullable(WAXING.inverse().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }
    public static Block waxedBlock(Block block) {
        return WAXING.containsKey(block) ? WAXING.get(block) : HoneycombItem.WAXABLES.get().get(block);
    }
    public static Block unwaxedBlock(Block block) {
        return WAXING.containsValue(block) ? WAXING.inverse().get(block) : HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block);
    }
    private CopperTransitions() { }
}
