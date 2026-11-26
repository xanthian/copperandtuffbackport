package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import xanthian.copperandtuffbackport.blocks.custom.CopperBulbBlock;
import xanthian.copperandtuffbackport.blocks.custom.CopperGrateBlock;

@net.minecraftforge.gametest.GameTestHolder(Initialise.MOD_ID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class BlockParityTests {
    private static Block block(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(Initialise.MOD_ID, name));
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void bulbPulsesAndLightLevels(GameTestHelper context) {
        String[] prefixes = {"", "exposed_", "weathered_", "oxidized_"};
        int[] light = {15, 12, 8, 4};
        BlockPos pos = context.absolutePos(new BlockPos(2, 2, 2));
        var world = context.getLevel();
        for (String wax : new String[]{"", "waxed_"}) {
            for (int i = 0; i < prefixes.length; i++) {
                CopperBulbBlock bulb = (CopperBulbBlock) block(wax + prefixes[i] + "copper_bulb");
                world.setBlockAndUpdate(pos, bulb.defaultBlockState());
                context.assertTrue(!world.getBlockState(pos).isRedstoneConductor(world, pos), "Every bulb variant remains nonconductive");
                context.assertTrue(world.getBlockState(pos).getLightEmission() == 0, "New bulb must be dark");
                world.setBlockAndUpdate(pos.above(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                context.assertTrue(world.getBlockState(pos).getValue(CopperBulbBlock.LIT), "Rising edge must light bulb");
                context.assertTrue(world.getBlockState(pos).getLightEmission() == light[i], "Oxidation light level");
                bulb.neighborChanged(world.getBlockState(pos), world, pos, Blocks.REDSTONE_BLOCK, pos.above(), false);
                context.assertTrue(world.getBlockState(pos).getValue(CopperBulbBlock.LIT), "Sustained power must not toggle");
                world.removeBlock(pos.above(), false);
                context.assertTrue(world.getBlockState(pos).getValue(CopperBulbBlock.LIT), "Falling edge must preserve light");
                context.assertTrue(bulb.getAnalogOutputSignal(world.getBlockState(pos), world, pos) == 15, "Lit comparator output");
                world.setBlockAndUpdate(pos.above(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                context.assertTrue(!world.getBlockState(pos).getValue(CopperBulbBlock.LIT), "Second pulse must turn off");
                context.assertTrue(bulb.getAnalogOutputSignal(world.getBlockState(pos), world, pos) == 0, "Dark comparator output");
                world.removeBlock(pos.above(), false);
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void copperTransformationsAndGrates(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos pos = context.absolutePos(new BlockPos(2, 2, 2));
        String[] prefixes = {"", "exposed_", "weathered_", "oxidized_"};
        for (String family : new String[]{"chiseled_copper", "copper_door", "copper_trapdoor", "copper_grate", "copper_bulb"}) {
            for (int i = 0; i < prefixes.length; i++) {
                Block copper = block(prefixes[i] + family);
                Block waxed = block("waxed_" + prefixes[i] + family);
                context.assertTrue(xanthian.copperandtuffbackport.util.CopperTransitions.waxedBlock(copper) == waxed, "Waxing pair " + family);
                context.assertTrue(xanthian.copperandtuffbackport.util.CopperTransitions.first(copper.defaultBlockState()).is(block(family)), "Lightning reset " + family);
                context.assertTrue(i == 3 ? xanthian.copperandtuffbackport.util.CopperTransitions.next(copper).isEmpty()
                        : xanthian.copperandtuffbackport.util.CopperTransitions.next(copper).orElseThrow() == block(prefixes[i + 1] + family), "Oxidation chain " + family);
                for (Block candidate : new Block[]{copper, waxed}) {
                    context.assertTrue(!Items.WOODEN_PICKAXE.getDefaultInstance().isCorrectToolForDrops(candidate.defaultBlockState()), "Copper requires stone tools");
                    context.assertTrue(Items.STONE_PICKAXE.getDefaultInstance().isCorrectToolForDrops(candidate.defaultBlockState()), "Stone pickaxe harvests copper");
                    if (candidate instanceof CopperGrateBlock grate) {
                        BlockState state = candidate.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
                        context.assertTrue(state.getFluidState().is(Fluids.WATER), "Waterlogged grate");
                        context.assertTrue(grate.propagatesSkylightDown(state, world, pos), "Grate transmits skylight");
                        context.assertTrue(grate.getShadeBrightness(state, world, pos) == 1.0F, "Grate ambient light");
                        context.assertTrue(grate.getVisualShape(state, world, pos, CollisionContext.empty()).isEmpty(), "Grate camera collision");
                        context.assertTrue(!state.isRedstoneConductor(world, pos), "Grate cannot conduct redstone");
                        context.assertTrue(!state.isValidSpawn(world, pos, EntityType.ZOMBIE), "Mobs cannot spawn on grates");
                        context.assertTrue(!state.isSuffocating(world, pos), "Grates cannot suffocate mobs");
                    }
                }
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void tuffShapesAndDrops(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos pos = context.absolutePos(new BlockPos(2, 2, 2));
        String[] names = {"tuff", "tuff_slab", "tuff_stairs", "tuff_wall", "polished_tuff", "polished_tuff_slab",
                "polished_tuff_stairs", "polished_tuff_wall", "chiseled_tuff", "tuff_bricks", "tuff_brick_slab",
                "tuff_brick_stairs", "tuff_brick_wall", "chiseled_tuff_bricks"};
        for (String name : names) {
            Block block = name.equals("tuff") ? Blocks.TUFF : block(name);
            BlockState state = block.defaultBlockState();
            context.assertTrue(state.getDestroySpeed(world, pos) == 1.5F && block.getExplosionResistance() == 6.0F, "Tuff strength " + name);
            context.assertTrue(Items.WOODEN_PICKAXE.getDefaultInstance().isCorrectToolForDrops(state), "Wooden pickaxe harvests tuff");
            context.assertTrue(state.requiresCorrectToolForDrops() && !Items.STICK.getDefaultInstance().isCorrectToolForDrops(state), "Tuff requires a pickaxe " + name);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                context.assertTrue(state.setValue(BlockStateProperties.WATERLOGGED, true).getFluidState().is(Fluids.WATER), "Tuff shape waterlogging " + name);
            }
            var drops = Block.getDrops(state, world, pos, null, null, Items.STONE_PICKAXE.getDefaultInstance());
            context.assertTrue(drops.size() == 1 && drops.get(0).is(block.asItem()) && drops.get(0).getCount() == 1, "Tuff drop " + name);
            if (block instanceof SlabBlock) {
                var doubleDrops = Block.getDrops(state.setValue(SlabBlock.TYPE, SlabType.DOUBLE), world, pos, null, null, Items.STONE_PICKAXE.getDefaultInstance());
                context.assertTrue(doubleDrops.stream().mapToInt(ItemStack::getCount).sum() == 2, "Double slabs drop twice " + name);
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void doorRecipesUseIngots(GameTestHelper context) {
        var manager = context.getLevel().getRecipeManager();
        for (String name : new String[]{"copper_door", "copper_trapdoor"}) {
            var recipe = manager.byKey(new ResourceLocation(Initialise.MOD_ID, name)).orElseThrow();
            for (var ingredient : recipe.getIngredients()) {
                context.assertTrue(ingredient.test(Items.COPPER_INGOT.getDefaultInstance()), "Doors and trapdoors are crafted from copper ingots");
                context.assertTrue(!ingredient.test(Items.COPPER_BLOCK.getDefaultInstance()), "Copper blocks cannot substitute for ingots");
            }
            int count = recipe.getResultItem(context.getLevel().registryAccess()).getCount();
            int ingredients = recipe.getIngredients().size();
            context.assertTrue(ingredients == (name.equals("copper_door") ? 6 : 4), "Door/trapdoor ingredient count avoids copper bars recipe collision");
            context.assertTrue(count == (name.equals("copper_door") ? 3 : 1), "Vanilla recipe yield (1.21.9 trapdoor compatibility)");
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void copperRecipeYields(GameTestHelper context) {
        var manager = context.getLevel().getRecipeManager();
        for (String wax : new String[]{"", "waxed_"}) {
            for (String prefix : new String[]{"", "exposed_", "weathered_", "oxidized_"}) {
                String material = wax + prefix + (prefix.isEmpty() ? "copper_block" : "copper");
                String cut = wax + prefix + "cut_copper";
                String chiseled = wax + prefix + "chiseled_copper";
                String grate = wax + prefix + "copper_grate";
                String[] recipes = {chiseled, chiseled + "_from_" + material + "_stonecutting",
                        chiseled + "_from_" + cut + "_stonecutting", grate,
                        grate + "_from_" + material + "_stonecutting", wax + prefix + "copper_bulb"};
                int[] yields = {1, 4, 1, 4, 4, 4};
                for (int i = 0; i < recipes.length; i++) {
                    var recipe = manager.byKey(new ResourceLocation(Initialise.MOD_ID, recipes[i])).orElseThrow();
                    context.assertTrue(recipe.getResultItem(context.getLevel().registryAccess()).getCount() == yields[i],
                            "1.21.1 recipe yield: " + recipes[i]);
                }
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void waterloggedGrateDoesNotLeak(GameTestHelper context) {
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var world = context.getLevel();
        world.setBlockAndUpdate(pos, block("copper_grate").defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        world.scheduleTick(pos, Fluids.WATER, 1);
        context.runAtTickTime(10, () -> {
            context.assertTrue(world.getFluidState(pos).is(Fluids.WATER), "Grate retains water");
            for (var direction : net.minecraft.core.Direction.values()) {
                context.assertTrue(world.getFluidState(pos.relative(direction)).isEmpty(), "Water must not flow out of grate: " + direction);
            }
            context.succeed();
        });
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void doorHalvesAndMobTag(GameTestHelper context) {
        InteractWithDoor.create(); // Forces validation of all three villager tag redirects.
        TagKey<Block> mobDoors = TagKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "mob_interactable_doors"));
        var world = context.getLevel();
        BlockPos pos = context.absolutePos(new BlockPos(2, 2, 2));
        world.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        for (String wax : new String[]{"", "waxed_"}) {
            for (String prefix : new String[]{"", "exposed_", "weathered_", "oxidized_"}) {
                Block copper = block(wax + prefix + "copper_door");
                context.assertTrue(copper.defaultBlockState().is(mobDoors), "Mobs must recognize copper doors");
                world.setBlockAndUpdate(pos, copper.defaultBlockState());
                world.setBlockAndUpdate(pos.above(), copper.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
                Block waxed = xanthian.copperandtuffbackport.util.CopperTransitions.waxedBlock(copper);
                if (waxed != null) {
                    world.setBlockAndUpdate(pos.above(), waxed.withPropertiesOf(world.getBlockState(pos.above())));
                    context.assertTrue(world.getBlockState(pos).is(waxed), "Waxing upper half updates lower half");
                    world.setBlockAndUpdate(pos, copper.withPropertiesOf(world.getBlockState(pos)));
                    context.assertTrue(world.getBlockState(pos.above()).is(copper), "Unwaxing lower half updates upper half");
                }
                world.removeBlock(pos, false);
                context.assertTrue(world.getBlockState(pos.above()).isAir(), "Removing lower half removes upper half");
            }
        }
        context.succeed();
    }
}
