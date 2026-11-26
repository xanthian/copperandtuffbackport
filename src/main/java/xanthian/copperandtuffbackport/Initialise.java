package xanthian.copperandtuffbackport;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import xanthian.copperandtuffbackport.blocks.*;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.util.*;

@Mod(Initialise.MOD_ID)
public final class Initialise {
    public static final String MOD_ID = "copperandtuffbackport";
    // Legacy NeoForge 1.20.1 uses the static context accessor; retain the shared loader entry point.
    @SuppressWarnings("removal")
    public Initialise() {
        ModSounds.registerSounds();
        ModCopperDecorations.prepare();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::register);
        bus.addListener(this::setup);
        bus.addListener(ModItemGroup::build);
    }
    private void setup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(CopperPoiRegistration::register);
    }
    private void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            BlockRegistration.withBlockRegistry(event, () -> {
                ModCopperBlocks.registerBlocks();
                ModTuffBlocks.registerBlocks();
                ModCopperAgeBlocks.register();
                ModCopperDecorations.register();
                ModRegistries.registerOxidizableBlocks();
            });
        } else if (event.getRegistryKey().equals(Registries.ITEM)) {
            ModCopperDecorations.NUGGET = new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties());
            BlockRegistration.registerItems(event);
        } else if (event.getRegistryKey().equals(Registries.SOUND_EVENT)) {
            BlockRegistration.registerSounds(event);
        } else if (event.getRegistryKey().equals(Registries.PARTICLE_TYPE)) {
            event.register(Registries.PARTICLE_TYPE, id("copper_fire_flame"), () -> ModCopperDecorations.FLAME);
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            ModCopperAgeBlocks.CHEST_ENTITY = BlockEntityType.Builder.of(CopperChestBlockEntity::new, ModCopperAgeBlocks.CHESTS).build(null);
            event.register(Registries.BLOCK_ENTITY_TYPE, id("copper_chest"), () -> ModCopperAgeBlocks.CHEST_ENTITY);
        }
    }
    public static ResourceLocation id(String path) { return ResourceLocation.tryBuild(MOD_ID, path); }
}
