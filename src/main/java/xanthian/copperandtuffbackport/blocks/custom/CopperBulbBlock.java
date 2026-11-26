package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import xanthian.copperandtuffbackport.util.ModSounds;

public class CopperBulbBlock extends Block {
    public static final BooleanProperty POWERED;
    public static final BooleanProperty LIT;

    static {
        POWERED = BlockStateProperties.POWERED;
        LIT = BlockStateProperties.LIT;
    }

    public CopperBulbBlock(Properties settings) {
        super(settings.isRedstoneConductor((state, world, pos) -> false));
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(POWERED, false));
    }

    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.getBlock() != state.getBlock() && world instanceof ServerLevel serverWorld) {
            this.update(state, serverWorld, pos);
        }

    }

    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world instanceof ServerLevel serverWorld) {
            this.update(state, serverWorld, pos);
        }

    }

    public void update(BlockState state, ServerLevel world, BlockPos pos) {
        boolean bl = world.hasNeighborSignal(pos);
        if (bl == state.getValue(POWERED)) {
            return;
        }
        BlockState blockState = state;
        if (!state.getValue(POWERED).booleanValue()) {
            world.playSound(null, pos, (blockState = blockState.cycle(LIT)).getValue(LIT) ? ModSounds.BLOCK_COPPER_BULB_TURN_ON : ModSounds.BLOCK_COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
        }
        world.setBlock(pos, blockState.setValue(POWERED, bl), 3);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED);
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return world.getBlockState(pos).getValue(LIT) ? 15 : 0;
    }
}
