package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableCopperChestBlock extends CopperChestBlock implements Oxidizable {
    public OxidizableCopperChestBlock(OxidationLevel level, Settings settings) { super(level, settings); }
    @Override public boolean isWaxed() { return false; }
    @Override public OxidationLevel getDegradationLevel() { return getOxidationLevel(); }
    @Override public boolean hasRandomTicks(BlockState state) { return Oxidizable.getIncreasedOxidationBlock(this).isPresent(); }
    @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(CHEST_TYPE) != ChestType.RIGHT && world.getBlockEntity(pos) instanceof ChestBlockEntity
                && ChestBlockEntity.getPlayersLookingInChestCount(world, pos) == 0) {
            tickDegradation(state, world, pos, random);
        }
    }
}
