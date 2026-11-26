package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CopperLightningRodBlock extends LightningRodBlock {
    public CopperLightningRodBlock(Properties settings) { super(settings); }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        // 1.21.9 preserves powered rods across oxidation/wax changes and schedules their reset.
        // 1.20.1 would clear POWERED when the old variant's queued tick belongs to another block.
        if (!state.is(oldState.getBlock()) && state.getValue(POWERED)
                && !world.getBlockTicks().hasScheduledTick(pos, this)) {
            world.scheduleTick(pos, this, 8);
        }
    }
}
