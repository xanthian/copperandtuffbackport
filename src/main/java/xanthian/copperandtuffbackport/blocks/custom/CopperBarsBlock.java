package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class CopperBarsBlock extends IronBarsBlock {
    public CopperBarsBlock(Properties settings) { super(settings); }

    public static class OxidizableCopperBarsBlock extends CopperBarsBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {
        private final WeatherState level;

        public OxidizableCopperBarsBlock(WeatherState level, Properties settings) {
            super(settings);
            this.level = level;
        }

        @Override public WeatherState getAge() { return level; }
        @Override public boolean isRandomlyTicking(BlockState state) { return getAge() != WeatherState.OXIDIZED; }
        @Override public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) { onRandomTick(state, world, pos, random); }
    }
}
