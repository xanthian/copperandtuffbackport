package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class OxidizableCopperDoorBlock extends CopperDoorBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {
    private final WeatherState oxidationLevel;

    public OxidizableCopperDoorBlock(BlockSetType type, WeatheringCopper.WeatherState oxidationLevel, Properties settings) {
        super(settings, type);
        this.oxidationLevel = oxidationLevel;
    }

    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            this.onRandomTick(state, world, pos, random);
        }

    }

    public boolean isRandomlyTicking(BlockState state) {
        return getAge() != WeatherState.OXIDIZED;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.getBlock() instanceof DoorBlock && neighborState.getValue(HALF) != doubleBlockHalf ? neighborState.setValue(HALF, doubleBlockHalf) : Blocks.AIR.defaultBlockState();
        } else {
            return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    public WeatherState getAge() {
        return this.oxidationLevel;
    }
}
