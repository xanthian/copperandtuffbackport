package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperLightningRodBlock extends LightningRodBlock {
    public CopperLightningRodBlock(Settings settings) { super(settings); }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        // 1.21.9 preserves powered rods across oxidation/wax changes and schedules their reset.
        // 1.20.1 would clear POWERED when the old variant's queued tick belongs to another block.
        if (!state.isOf(oldState.getBlock()) && state.get(POWERED)
                && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.scheduleBlockTick(pos, this, 8);
        }
    }
}
