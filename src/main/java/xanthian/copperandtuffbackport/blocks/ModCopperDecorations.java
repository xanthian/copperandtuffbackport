package xanthian.copperandtuffbackport.blocks;

import static xanthian.copperandtuffbackport.util.BlockRegistration.registerBlock;

import java.util.HashSet;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.poi.PointOfInterestTypes;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blocks.custom.CopperTorchBlock;
import xanthian.copperandtuffbackport.blocks.custom.CopperLightningRodBlock;
import xanthian.copperandtuffbackport.blocks.custom.WeatheringCopperDecorations;
import xanthian.copperandtuffbackport.mixin.PointOfInterestTypesInvoker;

public final class ModCopperDecorations {
    public static final Block[] LANTERNS = new Block[8];
    public static final Block[] CHAINS = new Block[8];
    public static final Block[] RODS = new Block[8];
    public static final Item NUGGET = new Item(new FabricItemSettings());
    public static final DefaultParticleType FLAME = FabricParticleTypes.simple();
    public static Block TORCH;
    public static Block WALL_TORCH;

    public static void register() {
        Registry.register(Registries.ITEM, id("copper_nugget"), NUGGET);
        Registry.register(Registries.PARTICLE_TYPE, id("copper_fire_flame"), FLAME);
        TORCH = new CopperTorchBlock(FabricBlockSettings.copyOf(Blocks.TORCH).luminance(14), FLAME);
        Registry.register(Registries.BLOCK, id("copper_torch"), TORCH);
        WALL_TORCH = new CopperTorchBlock.CopperWallTorchBlock(FabricBlockSettings.copyOf(Blocks.WALL_TORCH).luminance(14).dropsLike(TORCH), FLAME);
        Registry.register(Registries.BLOCK, id("copper_wall_torch"), WALL_TORCH);
        Registry.register(Registries.ITEM, id("copper_torch"), new VerticallyAttachableBlockItem(TORCH, WALL_TORCH, new FabricItemSettings(), Direction.DOWN));
        MapColor[] colors = {MapColor.ORANGE, MapColor.TERRACOTTA_LIGHT_GRAY, MapColor.DARK_AQUA, MapColor.TEAL};
        for (int wax = 0; wax < 2; wax++) {
            for (int level = 0; level < 4; level++) {
                int index = wax * 4 + level;
                var oxidation = Oxidizable.OxidationLevel.values()[level];
                String prefix = (wax == 1 ? "waxed_" : "") + ModCopperAgeBlocks.PREFIXES[level];
                var lantern = FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).solid().strength(3.5F)
                        .sounds(BlockSoundGroup.LANTERN).luminance(15).nonOpaque().pistonBehavior(PistonBehavior.DESTROY);
                LANTERNS[index] = wax == 0 ? new WeatheringCopperDecorations.CopperLanternBlock(oxidation, lantern) : new LanternBlock(lantern);
                var chain = FabricBlockSettings.create().solid().requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.CHAIN).nonOpaque();
                CHAINS[index] = wax == 0 ? new WeatheringCopperDecorations.CopperChainBlock(oxidation, chain) : new ChainBlock(chain);
                if (index == 0) RODS[index] = Blocks.LIGHTNING_ROD;
                else {
                    var rod = FabricBlockSettings.copyOf(Blocks.LIGHTNING_ROD).mapColor(colors[level]);
                    RODS[index] = wax == 0 ? new WeatheringCopperDecorations.OxidizableCopperLightningRodBlock(oxidation, rod) : new CopperLightningRodBlock(rod);
                    registerBlock(prefix + "lightning_rod", RODS[index]);
                }
                registerBlock(prefix + "copper_lantern", LANTERNS[index]);
                registerBlock(prefix + "copper_chain", CHAINS[index]);
            }
        }
        for (Block[] family : new Block[][]{LANTERNS, CHAINS, RODS}) {
            for (int i = 0; i < 4; i++) {
                OxidizableBlocksRegistry.registerWaxableBlockPair(family[i], family[i + 4]);
                if (i < 3) OxidizableBlocksRegistry.registerOxidizableBlockPair(family[i], family[i + 1]);
            }
        }
        var poiStates = new HashSet<BlockState>();
        for (int i = 1; i < 8; i++) poiStates.addAll(RODS[i].getStateManager().getStates());
        PointOfInterestTypesInvoker.copperandtuffbackport$registerStates(
                Registries.POINT_OF_INTEREST_TYPE.entryOf(PointOfInterestTypes.LIGHTNING_ROD), poiStates);
    }
    private static Identifier id(String name) { return new Identifier(Initialise.MOD_ID, name); }
    private ModCopperDecorations() { }
}
