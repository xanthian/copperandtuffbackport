package xanthian.copperandtuffbackport.util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
public final class ModBlockSetTypes {
    public static final BlockSetType COPPER = new BlockSetType("copperandtuffbackport:copper", true, SoundType.COPPER,
            ModSounds.BLOCK_COPPER_DOOR_CLOSE, ModSounds.BLOCK_COPPER_TRAPDOOR_CLOSE,
            ModSounds.BLOCK_COPPER_DOOR_OPEN, ModSounds.BLOCK_COPPER_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
    private ModBlockSetTypes() { }
}
