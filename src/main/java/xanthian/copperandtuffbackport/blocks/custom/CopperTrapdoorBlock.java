package xanthian.copperandtuffbackport.blocks.custom;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
public class CopperTrapdoorBlock extends TrapDoorBlock {
    public CopperTrapdoorBlock(Properties properties, BlockSetType type) {
        super(properties.isValidSpawn((state, world, pos, entity) -> false), type);
    }
}
