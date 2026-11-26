package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class WeatheringCopperDecorations {
    public static class CopperLanternBlock extends LanternBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {
        private final WeatherState level;
        public CopperLanternBlock(WeatherState level, Properties settings) { super(settings); this.level = level; }
        @Override public WeatherState getAge() { return level; }
        @Override public boolean isRandomlyTicking(BlockState state) { return getAge() != WeatherState.OXIDIZED; }
        @Override public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) { onRandomTick(state, world, pos, random); }
    }
    public static class CopperChainBlock extends ChainBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {
        private final WeatherState level;
        public CopperChainBlock(WeatherState level, Properties settings) { super(settings); this.level = level; }
        @Override public WeatherState getAge() { return level; }
        @Override public boolean isRandomlyTicking(BlockState state) { return getAge() != WeatherState.OXIDIZED; }
        @Override public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) { onRandomTick(state, world, pos, random); }
    }
    public static class OxidizableCopperLightningRodBlock extends CopperLightningRodBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {
        private final WeatherState level;
        public OxidizableCopperLightningRodBlock(WeatherState level, Properties settings) { super(settings); this.level = level; }
        @Override public WeatherState getAge() { return level; }
        @Override public boolean isRandomlyTicking(BlockState state) { return getAge() != WeatherState.OXIDIZED; }
        @Override public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) { onRandomTick(state, world, pos, random); }
    }
    private WeatheringCopperDecorations() { }
}
