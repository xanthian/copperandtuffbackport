package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class OxidizableCopperBulbBlock extends CopperBulbBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {

    private final WeatherState oxidationLevel;

    public OxidizableCopperBulbBlock(WeatherState oxidationLevel, Properties settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.onRandomTick(state, world, pos, random);
    }

    public boolean isRandomlyTicking(BlockState state) {
        return getAge() != WeatherState.OXIDIZED;
    }

    public WeatherState getAge() {
        return this.oxidationLevel;
    }
}
