package xanthian.copperandtuffbackport.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;

public class CopperChestBlockEntity extends ChestBlockEntity {
    public CopperChestBlockEntity(BlockPos pos, BlockState state) { super(ModCopperAgeBlocks.CHEST_ENTITY, pos, state); }
}
