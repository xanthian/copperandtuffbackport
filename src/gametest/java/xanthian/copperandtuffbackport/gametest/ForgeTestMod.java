package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
@Mod("copperandtuffbackport_test")
public final class ForgeTestMod {
    // Match the production entry point on legacy NeoForge 1.20.1.
    @SuppressWarnings("removal")
    public ForgeTestMod() { FMLJavaModLoadingContext.get().getModEventBus().addListener(this::register); }
    private void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) CopperDecorationTests.registerItems(event);
    }
}
