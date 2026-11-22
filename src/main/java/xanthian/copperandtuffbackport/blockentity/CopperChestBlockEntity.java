package xanthian.copperandtuffbackport.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;

public class CopperChestBlockEntity extends ChestBlockEntity {
    public CopperChestBlockEntity(BlockPos pos, BlockState state) { super(ModCopperAgeBlocks.CHEST_ENTITY, pos, state); }
}
