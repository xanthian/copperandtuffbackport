package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluids;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;

public class CopperChestBlock extends ChestBlock {
    private final WeatheringCopper.WeatherState oxidationLevel;

    public CopperChestBlock(WeatheringCopper.WeatherState level, Properties settings) {
        super(settings, () -> ModCopperAgeBlocks.CHEST_ENTITY);
        this.oxidationLevel = level;
    }

    public WeatheringCopper.WeatherState getOxidationLevel() { return oxidationLevel; }
    public boolean isWaxed() { return true; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CopperChestBlockEntity(pos, state); }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        // 1.20.1 normally scatters inventory and removes the entity whenever the block ID changes.
        if (!(newState.getBlock() instanceof CopperChestBlock)) super.onRemove(state, world, pos, newState, moved);
    }

    private Direction neighborFacing(BlockPlaceContext ctx, Direction direction) {
        BlockState neighbor = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(direction));
        return neighbor.getBlock() instanceof CopperChestBlock && neighbor.getValue(TYPE) == ChestType.SINGLE ? neighbor.getValue(FACING) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        ChestType type = ChestType.SINGLE;
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        Direction side = ctx.getClickedFace();
        if (side.getAxis().isHorizontal() && ctx.isSecondaryUseActive()) {
            Direction neighbor = neighborFacing(ctx, side.getOpposite());
            if (neighbor != null && neighbor.getAxis() != side.getAxis()) {
                facing = neighbor;
                type = neighbor.getCounterClockWise() == side.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }
        if (type == ChestType.SINGLE && !ctx.isSecondaryUseActive()) {
            if (facing == neighborFacing(ctx, facing.getClockWise())) type = ChestType.LEFT;
            else if (facing == neighborFacing(ctx, facing.getCounterClockWise())) type = ChestType.RIGHT;
        }
        BlockState state = defaultBlockState().setValue(FACING, facing).setValue(TYPE, type)
                .setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).is(Fluids.WATER));
        if (type != ChestType.SINGLE) {
            BlockState neighbor = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(getConnectedDirection(state)));
            if (neighbor.getBlock() instanceof CopperChestBlock other) {
                boolean waxed = isWaxed() && other.isWaxed();
                int level = Math.min(oxidationLevel.ordinal(), other.oxidationLevel.ordinal());
                state = ModCopperAgeBlocks.CHESTS[(waxed ? 4 : 0) + level].withPropertiesOf(state);
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                               LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        if (neighbor.getBlock() instanceof CopperChestBlock && direction.getAxis().isHorizontal()) {
            ChestType otherType = neighbor.getValue(TYPE);
            if (state.getValue(TYPE) == ChestType.SINGLE && otherType != ChestType.SINGLE
                    && state.getValue(FACING) == neighbor.getValue(FACING) && getConnectedDirection(neighbor) == direction.getOpposite()) {
                state = state.setValue(TYPE, otherType.getOpposite());
            }
            if (state.getValue(TYPE) != ChestType.SINGLE && getConnectedDirection(state) == direction) {
                return neighbor.getBlock().withPropertiesOf(state);
            }
        } else if (getConnectedDirection(state) == direction) {
            return state.setValue(TYPE, ChestType.SINGLE);
        }
        return state;
    }
}
