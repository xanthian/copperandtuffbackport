package xanthian.copperandtuffbackport;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xanthian.copperandtuffbackport.blocks.custom.CopperBulbBlock;
import xanthian.copperandtuffbackport.blocks.custom.CopperGrateBlock;

public class BlockParityTests implements FabricGameTest {
    private static Block block(String name) {
        return Registries.BLOCK.get(new Identifier(Initialise.MOD_ID, name));
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void bulbPulsesAndLightLevels(TestContext context) {
        String[] prefixes = {"", "exposed_", "weathered_", "oxidized_"};
        int[] light = {15, 12, 8, 4};
        BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
        var world = context.getWorld();
        for (String wax : new String[]{"", "waxed_"}) {
            for (int i = 0; i < prefixes.length; i++) {
                CopperBulbBlock bulb = (CopperBulbBlock) block(wax + prefixes[i] + "copper_bulb");
                world.setBlockState(pos, bulb.getDefaultState());
                context.assertTrue(world.getBlockState(pos).getLuminance() == 0, "New bulb must be dark");
                world.setBlockState(pos.up(), Blocks.REDSTONE_BLOCK.getDefaultState());
                context.assertTrue(world.getBlockState(pos).get(CopperBulbBlock.LIT), "Rising edge must light bulb");
                context.assertTrue(world.getBlockState(pos).getLuminance() == light[i], "Oxidation light level");
                bulb.neighborUpdate(world.getBlockState(pos), world, pos, Blocks.REDSTONE_BLOCK, pos.up(), false);
                context.assertTrue(world.getBlockState(pos).get(CopperBulbBlock.LIT), "Sustained power must not toggle");
                world.removeBlock(pos.up(), false);
                context.assertTrue(world.getBlockState(pos).get(CopperBulbBlock.LIT), "Falling edge must preserve light");
                context.assertTrue(bulb.getComparatorOutput(world.getBlockState(pos), world, pos) == 15, "Lit comparator output");
                world.setBlockState(pos.up(), Blocks.REDSTONE_BLOCK.getDefaultState());
                context.assertTrue(!world.getBlockState(pos).get(CopperBulbBlock.LIT), "Second pulse must turn off");
                context.assertTrue(bulb.getComparatorOutput(world.getBlockState(pos), world, pos) == 0, "Dark comparator output");
                world.removeBlock(pos.up(), false);
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void copperTransformationsAndGrates(TestContext context) {
        var world = context.getWorld();
        BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
        String[] prefixes = {"", "exposed_", "weathered_", "oxidized_"};
        for (String family : new String[]{"chiseled_copper", "copper_door", "copper_trapdoor", "copper_grate", "copper_bulb"}) {
            for (int i = 0; i < prefixes.length; i++) {
                Block copper = block(prefixes[i] + family);
                Block waxed = block("waxed_" + prefixes[i] + family);
                context.assertTrue(HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().get(copper) == waxed, "Waxing pair " + family);
                context.assertTrue(Oxidizable.getUnaffectedOxidationState(copper.getDefaultState()).isOf(block(family)), "Lightning reset " + family);
                context.assertTrue(i == 3 ? Oxidizable.getIncreasedOxidationBlock(copper).isEmpty()
                        : Oxidizable.getIncreasedOxidationBlock(copper).orElseThrow() == block(prefixes[i + 1] + family), "Oxidation chain " + family);
                for (Block candidate : new Block[]{copper, waxed}) {
                    context.assertTrue(!Items.WOODEN_PICKAXE.getDefaultStack().isSuitableFor(candidate.getDefaultState()), "Copper requires stone tools");
                    context.assertTrue(Items.STONE_PICKAXE.getDefaultStack().isSuitableFor(candidate.getDefaultState()), "Stone pickaxe harvests copper");
                    if (candidate instanceof CopperGrateBlock grate) {
                        BlockState state = candidate.getDefaultState().with(Properties.WATERLOGGED, true);
                        context.assertTrue(state.getFluidState().isOf(Fluids.WATER), "Waterlogged grate");
                        context.assertTrue(grate.isTransparent(state, world, pos), "Grate transmits skylight");
                        context.assertTrue(grate.getAmbientOcclusionLightLevel(state, world, pos) == 1.0F, "Grate ambient light");
                        context.assertTrue(grate.getCameraCollisionShape(state, world, pos, ShapeContext.absent()).isEmpty(), "Grate camera collision");
                        context.assertTrue(!state.isSolidBlock(world, pos), "Grate cannot conduct redstone");
                        context.assertTrue(!state.allowsSpawning(world, pos, EntityType.ZOMBIE), "Mobs cannot spawn on grates");
                        context.assertTrue(!state.shouldSuffocate(world, pos), "Grates cannot suffocate mobs");
                    }
                }
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void tuffShapesAndDrops(TestContext context) {
        var world = context.getWorld();
        BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
        String[] names = {"tuff", "tuff_slab", "tuff_stairs", "tuff_wall", "polished_tuff", "polished_tuff_slab",
                "polished_tuff_stairs", "polished_tuff_wall", "chiseled_tuff", "tuff_bricks", "tuff_brick_slab",
                "tuff_brick_stairs", "tuff_brick_wall", "chiseled_tuff_bricks"};
        for (String name : names) {
            Block block = name.equals("tuff") ? Blocks.TUFF : block(name);
            BlockState state = block.getDefaultState();
            context.assertTrue(state.getHardness(world, pos) == 1.5F && block.getBlastResistance() == 6.0F, "Tuff strength " + name);
            context.assertTrue(Items.WOODEN_PICKAXE.getDefaultStack().isSuitableFor(state), "Wooden pickaxe harvests tuff");
            context.assertTrue(state.isToolRequired() && !Items.STICK.getDefaultStack().isSuitableFor(state), "Tuff requires a pickaxe " + name);
            if (state.contains(Properties.WATERLOGGED)) {
                context.assertTrue(state.with(Properties.WATERLOGGED, true).getFluidState().isOf(Fluids.WATER), "Tuff shape waterlogging " + name);
            }
            var drops = Block.getDroppedStacks(state, world, pos, null, null, Items.STONE_PICKAXE.getDefaultStack());
            context.assertTrue(drops.size() == 1 && drops.get(0).isOf(block.asItem()) && drops.get(0).getCount() == 1, "Tuff drop " + name);
            if (block instanceof SlabBlock) {
                var doubleDrops = Block.getDroppedStacks(state.with(SlabBlock.TYPE, SlabType.DOUBLE), world, pos, null, null, Items.STONE_PICKAXE.getDefaultStack());
                context.assertTrue(doubleDrops.stream().mapToInt(ItemStack::getCount).sum() == 2, "Double slabs drop twice " + name);
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void doorRecipesUseIngots(TestContext context) {
        var manager = context.getWorld().getRecipeManager();
        for (String name : new String[]{"copper_door", "copper_trapdoor"}) {
            var recipe = manager.get(new Identifier(Initialise.MOD_ID, name)).orElseThrow();
            for (var ingredient : recipe.getIngredients()) {
                context.assertTrue(ingredient.test(Items.COPPER_INGOT.getDefaultStack()), "Doors and trapdoors are crafted from copper ingots");
                context.assertTrue(!ingredient.test(Items.COPPER_BLOCK.getDefaultStack()), "Copper blocks cannot substitute for ingots");
            }
            int count = recipe.getOutput(context.getWorld().getRegistryManager()).getCount();
            int ingredients = recipe.getIngredients().size();
            context.assertTrue(ingredients == (name.equals("copper_door") ? 6 : 4), "Door/trapdoor ingredient count avoids copper bars recipe collision");
            context.assertTrue(count == (name.equals("copper_door") ? 3 : 1), "Vanilla recipe yield (1.21.9 trapdoor compatibility)");
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void copperRecipeYields(TestContext context) {
        var manager = context.getWorld().getRecipeManager();
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
                    var recipe = manager.get(new Identifier(Initialise.MOD_ID, recipes[i])).orElseThrow();
                    context.assertTrue(recipe.getOutput(context.getWorld().getRegistryManager()).getCount() == yields[i],
                            "1.21.1 recipe yield: " + recipes[i]);
                }
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void waterloggedGrateDoesNotLeak(TestContext context) {
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var world = context.getWorld();
        world.setBlockState(pos, block("copper_grate").getDefaultState().with(Properties.WATERLOGGED, true));
        world.scheduleFluidTick(pos, Fluids.WATER, 1);
        context.runAtTick(10, () -> {
            context.assertTrue(world.getFluidState(pos).isOf(Fluids.WATER), "Grate retains water");
            for (var direction : net.minecraft.util.math.Direction.values()) {
                context.assertTrue(world.getFluidState(pos.offset(direction)).isEmpty(), "Water must not flow out of grate: " + direction);
            }
            context.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void doorHalvesAndMobTag(TestContext context) {
        OpenDoorsTask.create(); // Forces validation of all three villager tag redirects.
        TagKey<Block> mobDoors = TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "mob_interactable_doors"));
        var world = context.getWorld();
        BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
        for (String wax : new String[]{"", "waxed_"}) {
            for (String prefix : new String[]{"", "exposed_", "weathered_", "oxidized_"}) {
                Block copper = block(wax + prefix + "copper_door");
                context.assertTrue(copper.getDefaultState().isIn(mobDoors), "Mobs must recognize copper doors");
                world.setBlockState(pos, copper.getDefaultState());
                world.setBlockState(pos.up(), copper.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER));
                Block waxed = HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().get(copper);
                if (waxed != null) {
                    world.setBlockState(pos.up(), waxed.getStateWithProperties(world.getBlockState(pos.up())));
                    context.assertTrue(world.getBlockState(pos).isOf(waxed), "Waxing upper half updates lower half");
                    world.setBlockState(pos, copper.getStateWithProperties(world.getBlockState(pos)));
                    context.assertTrue(world.getBlockState(pos.up()).isOf(copper), "Unwaxing lower half updates upper half");
                }
                world.removeBlock(pos, false);
                context.assertTrue(world.getBlockState(pos.up()).isAir(), "Removing lower half removes upper half");
            }
        }
        context.complete();
    }
}
