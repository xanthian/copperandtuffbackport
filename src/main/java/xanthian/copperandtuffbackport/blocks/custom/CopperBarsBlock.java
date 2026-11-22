package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class CopperBarsBlock extends PaneBlock {
    public CopperBarsBlock(Settings settings) { super(settings); }

    public static class OxidizableCopperBarsBlock extends CopperBarsBlock implements Oxidizable {
        private final OxidationLevel level;

        public OxidizableCopperBarsBlock(OxidationLevel level, Settings settings) {
            super(settings);
            this.level = level;
        }

        @Override public OxidationLevel getDegradationLevel() { return level; }
        @Override public boolean hasRandomTicks(BlockState state) { return Oxidizable.getIncreasedOxidationBlock(this).isPresent(); }
        @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { tickDegradation(state, world, pos, random); }
    }
}
