package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;

public class CopperChestBlock extends ChestBlock {
    private final Oxidizable.OxidationLevel oxidationLevel;

    public CopperChestBlock(Oxidizable.OxidationLevel level, Settings settings) {
        super(settings, () -> ModCopperAgeBlocks.CHEST_ENTITY);
        this.oxidationLevel = level;
    }

    public Oxidizable.OxidationLevel getOxidationLevel() { return oxidationLevel; }
    public boolean isWaxed() { return true; }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new CopperChestBlockEntity(pos, state); }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        // 1.20.1 normally scatters inventory and removes the entity whenever the block ID changes.
        if (!(newState.getBlock() instanceof CopperChestBlock)) super.onStateReplaced(state, world, pos, newState, moved);
    }

    private Direction neighborFacing(ItemPlacementContext ctx, Direction direction) {
        BlockState neighbor = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(direction));
        return neighbor.getBlock() instanceof CopperChestBlock && neighbor.get(CHEST_TYPE) == ChestType.SINGLE ? neighbor.get(FACING) : null;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        ChestType type = ChestType.SINGLE;
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        Direction side = ctx.getSide();
        if (side.getAxis().isHorizontal() && ctx.shouldCancelInteraction()) {
            Direction neighbor = neighborFacing(ctx, side.getOpposite());
            if (neighbor != null && neighbor.getAxis() != side.getAxis()) {
                facing = neighbor;
                type = neighbor.rotateYCounterclockwise() == side.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }
        if (type == ChestType.SINGLE && !ctx.shouldCancelInteraction()) {
            if (facing == neighborFacing(ctx, facing.rotateYClockwise())) type = ChestType.LEFT;
            else if (facing == neighborFacing(ctx, facing.rotateYCounterclockwise())) type = ChestType.RIGHT;
        }
        BlockState state = getDefaultState().with(FACING, facing).with(CHEST_TYPE, type)
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
        if (type != ChestType.SINGLE) {
            BlockState neighbor = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(getFacing(state)));
            if (neighbor.getBlock() instanceof CopperChestBlock other) {
                boolean waxed = isWaxed() && other.isWaxed();
                int level = Math.min(oxidationLevel.ordinal(), other.oxidationLevel.ordinal());
                state = ModCopperAgeBlocks.CHESTS[(waxed ? 4 : 0) + level].getStateWithProperties(state);
            }
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbor,
                                               WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        if (neighbor.getBlock() instanceof CopperChestBlock && direction.getAxis().isHorizontal()) {
            ChestType otherType = neighbor.get(CHEST_TYPE);
            if (state.get(CHEST_TYPE) == ChestType.SINGLE && otherType != ChestType.SINGLE
                    && state.get(FACING) == neighbor.get(FACING) && getFacing(neighbor) == direction.getOpposite()) {
                state = state.with(CHEST_TYPE, otherType.getOpposite());
            }
            if (state.get(CHEST_TYPE) != ChestType.SINGLE && getFacing(state) == direction) {
                return neighbor.getBlock().getStateWithProperties(state);
            }
        } else if (getFacing(state) == direction) {
            return state.with(CHEST_TYPE, ChestType.SINGLE);
        }
        return state;
    }
}
