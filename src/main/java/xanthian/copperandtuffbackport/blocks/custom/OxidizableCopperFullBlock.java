package xanthian.copperandtuffbackport.blocks.custom;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.state.BlockState;
import xanthian.copperandtuffbackport.util.CopperWeathering;
public final class OxidizableCopperFullBlock extends WeatheringCopperFullBlock implements CopperWeathering {
    public OxidizableCopperFullBlock(WeatherState age, Properties properties) { super(age, properties); }
    @Override public boolean isRandomlyTicking(BlockState state) { return getAge() != WeatherState.OXIDIZED; }
}
