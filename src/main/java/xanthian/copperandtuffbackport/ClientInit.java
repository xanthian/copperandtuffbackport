package xanthian.copperandtuffbackport;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;
import xanthian.copperandtuffbackport.blocks.custom.CopperGrateBlock;
import xanthian.copperandtuffbackport.client.CopperChestRenderer;

@Mod.EventBusSubscriber(modid = Initialise.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientInit {
    // Both 1.20.1 loaders support this layer registration, including models supplied by resource packs.
    @SuppressWarnings("removal")
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ForgeRegistries.BLOCKS.getEntries().forEach(entry -> {
            Block block = entry.getValue();
            if (entry.getKey().location().getNamespace().equals(Initialise.MOD_ID)
                    && (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof IronBarsBlock
                    || block instanceof ChainBlock || block instanceof LanternBlock || block instanceof LightningRodBlock
                    || block instanceof TorchBlock || block instanceof CopperGrateBlock)) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
            }
        }));
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModCopperAgeBlocks.CHEST_ENTITY, CopperChestRenderer::new);
    }
    @SubscribeEvent public static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModCopperDecorations.FLAME, FlameParticle.Provider::new);
    }
}
