package xanthian.copperandtuffbackport.util;

import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;

/** Forge replaces vanilla's private POI registration method with its own public state index. */
@Mod.EventBusSubscriber(modid = Initialise.MOD_ID)
public final class CopperPoiRegistration {
    public static void register() {
        var type = ForgeRegistries.POI_TYPES.getValue(PoiTypes.LIGHTNING_ROD.location());
        if (type == null) throw new IllegalStateException("Vanilla lightning-rod POI is missing");
        var index = GameData.getBlockStatePointOfInterestTypeMap();
        for (var rod : ModCopperDecorations.RODS) {
            for (var state : rod.getStateDefinition().getPossibleStates()) {
                var previous = index.putIfAbsent(state, type);
                if (previous != null && previous != type) throw new IllegalStateException("Copper rod belongs to another POI: " + state);
            }
        }
    }
    // Forge restores registry snapshots between server sessions. Rebuild our extra state entries afterwards.
    @SubscribeEvent public static void serverStarting(ServerAboutToStartEvent event) { register(); }
    private CopperPoiRegistration() { }
}
