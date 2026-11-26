package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class OxidizableCopperChestBlock extends CopperChestBlock implements xanthian.copperandtuffbackport.util.CopperWeathering {
    public OxidizableCopperChestBlock(WeatherState level, Properties settings) { super(level, settings); }
    @Override public boolean isWaxed() { return false; }
    @Override public WeatherState getAge() { return getOxidationLevel(); }
    @Override public boolean isRandomlyTicking(BlockState state) { return getAge() != WeatherState.OXIDIZED; }
    @Override public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(TYPE) != ChestType.RIGHT && world.getBlockEntity(pos) instanceof ChestBlockEntity
                && ChestBlockEntity.getOpenCount(world, pos) == 0) {
            onRandomTick(state, world, pos, random);
        }
    }
}
