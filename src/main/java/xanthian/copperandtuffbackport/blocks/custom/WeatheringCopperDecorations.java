package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class WeatheringCopperDecorations {
    public static class CopperLanternBlock extends LanternBlock implements Oxidizable {
        private final OxidationLevel level;
        public CopperLanternBlock(OxidationLevel level, Settings settings) { super(settings); this.level = level; }
        @Override public OxidationLevel getDegradationLevel() { return level; }
        @Override public boolean hasRandomTicks(BlockState state) { return Oxidizable.getIncreasedOxidationBlock(this).isPresent(); }
        @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { tickDegradation(state, world, pos, random); }
    }
    public static class CopperChainBlock extends ChainBlock implements Oxidizable {
        private final OxidationLevel level;
        public CopperChainBlock(OxidationLevel level, Settings settings) { super(settings); this.level = level; }
        @Override public OxidationLevel getDegradationLevel() { return level; }
        @Override public boolean hasRandomTicks(BlockState state) { return Oxidizable.getIncreasedOxidationBlock(this).isPresent(); }
        @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { tickDegradation(state, world, pos, random); }
    }
    public static class OxidizableCopperLightningRodBlock extends CopperLightningRodBlock implements Oxidizable {
        private final OxidationLevel level;
        public OxidizableCopperLightningRodBlock(OxidationLevel level, Settings settings) { super(settings); this.level = level; }
        @Override public OxidationLevel getDegradationLevel() { return level; }
        @Override public boolean hasRandomTicks(BlockState state) { return Oxidizable.getIncreasedOxidationBlock(this).isPresent(); }
        @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { tickDegradation(state, world, pos, random); }
    }
    private WeatheringCopperDecorations() { }
}
