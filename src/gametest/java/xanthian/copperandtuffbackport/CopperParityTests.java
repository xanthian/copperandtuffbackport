package xanthian.copperandtuffbackport;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.CheckedRandom;

public class CopperParityTests implements FabricGameTest {
    private static final String[] LEVELS = {"", "exposed_", "weathered_", "oxidized_"};
    private static final String[] FAMILIES = {"copper_block", "cut_copper", "cut_copper_slab", "cut_copper_stairs",
            "chiseled_copper", "copper_door", "copper_trapdoor", "copper_grate", "copper_bulb"};

    private static Block copper(String family, int level, boolean waxed) {
        String name = LEVELS[level] + (family.equals("copper_block") && level > 0 ? "copper" : family);
        boolean vanilla = family.equals("copper_block") || family.startsWith("cut_copper");
        return Registries.BLOCK.get(new Identifier(vanilla ? "minecraft" : Initialise.MOD_ID, (waxed ? "waxed_" : "") + name));
    }

    private static BlockState configured(Block block) {
        BlockState state = block.getDefaultState();
        if (state.contains(Properties.WATERLOGGED)) state = state.with(Properties.WATERLOGGED, true);
        if (state.contains(Properties.OPEN)) state = state.with(Properties.OPEN, true);
        if (state.contains(Properties.POWERED)) state = state.with(Properties.POWERED, true);
        if (state.contains(Properties.LIT)) state = state.with(Properties.LIT, true);
        if (state.contains(Properties.HORIZONTAL_FACING)) state = state.with(Properties.HORIZONTAL_FACING, Direction.EAST);
        return state;
    }

    private static void place(TestContext context, BlockPos pos, BlockState state) {
        var world = context.getWorld();
        // Remove both halves before changing to another family.
        world.removeBlock(pos.up(), false);
        world.removeBlock(pos, false);
        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
        world.setBlockState(pos, state);
        if (state.getBlock() instanceof DoorBlock) {
            world.setBlockState(pos.up(), state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void recipeAdvancementsUnlockTheCorrectRecipes(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockCreativeServerPlayerInWorld();
        var tracker = player.getAdvancementTracker();
        int checked = 0;
        for (var advancement : world.getServer().getAdvancementLoader().getAdvancements()) {
            var id = advancement.getId();
            if (!id.getNamespace().equals(Initialise.MOD_ID) || !id.getPath().startsWith("recipes/")) continue;
            String name = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
            var recipe = world.getRecipeManager().get(new Identifier(Initialise.MOD_ID, name)).orElseThrow();
            player.getRecipeBook().remove(recipe);
            tracker.revokeCriterion(advancement, "has_the_recipe");
            tracker.grantCriterion(advancement, "has_the_recipe");
            context.assertTrue(player.getRecipeBook().contains(recipe), "Advancement must unlock mod recipe: " + name);
            checked++;
        }
        context.assertTrue(checked == 136, "All 136 mod recipe advancements must be tested");
        player.discard();
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void waxingAwardsVanillaAdvancements(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockCreativeServerPlayerInWorld();
        var tracker = player.getAdvancementTracker();
        var waxOn = world.getServer().getAdvancementLoader().get(new Identifier("minecraft", "husbandry/wax_on"));
        var waxOff = world.getServer().getAdvancementLoader().get(new Identifier("minecraft", "husbandry/wax_off"));
        context.assertTrue(waxOn != null && waxOff != null, "Vanilla waxing advancements must load");
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        java.util.List<Block> blocks = new java.util.ArrayList<>();
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) blocks.add(copper(family, level, false));
        }
        for (int level = 0; level < 4; level++) {
            blocks.add(xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks.BARS[level]);
            blocks.add(xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks.CHESTS[level]);
            blocks.add(xanthian.copperandtuffbackport.blocks.ModCopperDecorations.LANTERNS[level]);
            blocks.add(xanthian.copperandtuffbackport.blocks.ModCopperDecorations.CHAINS[level]);
            blocks.add(xanthian.copperandtuffbackport.blocks.ModCopperDecorations.RODS[level]);
        }
        for (Block block : blocks) {
            tracker.revokeCriterion(waxOn, "wax_on");
            tracker.revokeCriterion(waxOff, "wax_off");
            place(context, pos, block.getDefaultState());
            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
            Items.HONEYCOMB.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
            context.assertTrue(tracker.getProgress(waxOn).isDone(), "Wax On must recognize " + Registries.BLOCK.getId(block));
            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            Items.IRON_AXE.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
            context.assertTrue(tracker.getProgress(waxOff).isDone(), "Wax Off must recognize " + Registries.BLOCK.getId(block));
        }
        player.discard();
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void axeAndHoneycombInteractions(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) {
                BlockState original = configured(copper(family, level, false));
                place(context, pos, original);
                // Powered bulbs only retain LIT when actual power agrees with POWERED.
                if (original.contains(Properties.LIT)) {
                    world.setBlockState(pos.east(), Blocks.REDSTONE_BLOCK.getDefaultState());
                    original = world.getBlockState(pos);
                }
                player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
                var waxResult = Items.HONEYCOMB.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                Block waxed = copper(family, level, true);
                context.assertTrue(waxResult.isAccepted() && world.getBlockState(pos).equals(waxed.getStateWithProperties(original)), "Waxing preserves properties: " + family);
                context.assertTrue(player.getMainHandStack().getCount() == 1, "Waxing consumes one honeycomb");
                player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                var unwaxResult = Items.IRON_AXE.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                context.assertTrue(unwaxResult.isAccepted() && world.getBlockState(pos).equals(original), "Axe removes wax before oxidation: " + family);
                if (level > 0) {
                    Items.IRON_AXE.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                    context.assertTrue(world.getBlockState(pos).equals(copper(family, level - 1, false).getStateWithProperties(original)), "Scraping removes one oxidation level: " + family);
                } else {
                    var result = Items.IRON_AXE.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                    context.assertTrue(!result.isAccepted(), "Unaffected copper cannot be scraped");
                }
                world.removeBlock(pos.east(), false);
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void weatheringAndWaxProtection(TestContext context) {
        var world = context.getWorld();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var random = new CheckedRandom(0) { @Override public float nextFloat() { return 0.0F; } };
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) {
                Block original = copper(family, level, false);
                place(context, pos, original.getDefaultState());
                if (level < 3) {
                    original.randomTick(world.getBlockState(pos), world, pos, random);
                    context.assertTrue(world.getBlockState(pos).isOf(copper(family, level + 1, false)), "Random tick oxidizes: " + family);
                    if (original instanceof DoorBlock) {
                        context.assertTrue(world.getBlockState(pos.up()).isOf(copper(family, level + 1, false)), "Weathering synchronizes door halves");
                        BlockState upper = world.getBlockState(pos.up());
                        upper.getBlock().randomTick(upper, world, pos.up(), random);
                        context.assertTrue(world.getBlockState(pos.up()).equals(upper), "Door upper halves do not weather independently");
                    }
                    place(context, pos, original.getDefaultState());
                    if (level > 0) {
                        world.setBlockState(pos.east(2), copper("copper_block", level - 1, false).getDefaultState());
                        original.randomTick(world.getBlockState(pos), world, pos, random);
                        context.assertTrue(world.getBlockState(pos).isOf(original), "Less oxidized nearby copper prevents weathering: " + family);
                        world.removeBlock(pos.east(2), false);
                    }
                } else {
                    context.assertTrue(!original.hasRandomTicks(original.getDefaultState()), "Fully oxidized copper stops weathering");
                }
                Block waxed = copper(family, level, true);
                context.assertTrue(!(waxed instanceof Oxidizable) && !waxed.hasRandomTicks(waxed.getDefaultState()), "Wax prevents weathering: " + family);
                context.assertTrue(Oxidizable.getDecreasedOxidationBlock(waxed).isEmpty(), "Lightning cannot scrape waxed copper");
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void doorsAndTrapdoorsRespondToRedstoneAndPlayers(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        for (String family : new String[]{"copper_door", "copper_trapdoor"}) {
            for (int level = 0; level < LEVELS.length; level++) {
                for (boolean waxed : new boolean[]{false, true}) {
                    Block block = copper(family, level, waxed);
                    for (Direction side : Direction.Type.HORIZONTAL) {
                        place(context, pos, block.getDefaultState());
                        world.setBlockState(pos.offset(side), Blocks.REDSTONE_BLOCK.getDefaultState());
                        context.assertTrue(world.getBlockState(pos).get(Properties.OPEN) && world.getBlockState(pos).get(Properties.POWERED), "Redstone opens " + family);
                        block.onUse(world.getBlockState(pos), world, pos, player, Hand.MAIN_HAND, hit);
                        context.assertTrue(!world.getBlockState(pos).get(Properties.OPEN), "Player can close powered " + family);
                        block.onUse(world.getBlockState(pos), world, pos, player, Hand.MAIN_HAND, hit);
                        context.assertTrue(world.getBlockState(pos).get(Properties.OPEN), "Player can reopen powered " + family);
                        world.removeBlock(pos.offset(side), false);
                        context.assertTrue(!world.getBlockState(pos).get(Properties.OPEN) && !world.getBlockState(pos).get(Properties.POWERED), "Power removal closes " + family);
                    }
                }
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void allCopperLoot(TestContext context) {
        var world = context.getWorld();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) {
                for (boolean waxed : new boolean[]{false, true}) {
                    Block block = copper(family, level, waxed);
                    BlockState state = block.getDefaultState();
                    context.assertTrue(state.getHardness(world, pos) == 3.0F && block.getBlastResistance() == 6.0F, "Copper strength: " + family);
                    var drops = Block.getDroppedStacks(state, world, pos, null, null, Items.STONE_PICKAXE.getDefaultStack());
                    context.assertTrue(drops.size() == 1 && drops.get(0).isOf(block.asItem()) && drops.get(0).getCount() == 1, "Copper drops itself: " + family);
                    if (block instanceof SlabBlock) {
                        var doubleDrops = Block.getDroppedStacks(state.with(SlabBlock.TYPE, SlabType.DOUBLE), world, pos, null, null, Items.STONE_PICKAXE.getDefaultStack());
                        context.assertTrue(doubleDrops.stream().mapToInt(ItemStack::getCount).sum() == 2, "Double copper slabs drop two slabs");
                    }
                    if (block instanceof DoorBlock) {
                        var upperDrops = Block.getDroppedStacks(state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER), world, pos, null, null, Items.STONE_PICKAXE.getDefaultStack());
                        context.assertTrue(upperDrops.isEmpty(), "Only one door item drops per door");
                    }
                }
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void doorsRejectUnsuitableToolsAndDropOnSupportLoss(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (int level = 0; level < LEVELS.length; level++) {
            for (boolean waxed : new boolean[]{false, true}) {
                Block door = copper("copper_door", level, waxed);
                context.assertTrue(door.getDefaultState().getPistonBehavior() == PistonBehavior.DESTROY, "Pistons destroy copper doors");
                place(context, pos, door.getDefaultState());
                world.getEntitiesByClass(ItemEntity.class, new Box(pos).expand(3), entity -> true).forEach(ItemEntity::discard);
                player.setStackInHand(Hand.MAIN_HAND, Items.WOODEN_PICKAXE.getDefaultStack());
                BlockState upper = world.getBlockState(pos.up());
                door.onBreak(world, pos.up(), upper, player);
                world.removeBlock(pos.up(), false);
                context.assertTrue(world.getBlockState(pos).isAir(), "Breaking upper half removes lower half");
                context.assertTrue(world.getEntitiesByClass(ItemEntity.class, new Box(pos).expand(3), entity -> entity.getStack().isOf(door.asItem())).isEmpty(), "Unsuitable tool must not drop a door from its other half");
                place(context, pos, door.getDefaultState());
                world.removeBlock(pos.down(), false);
                context.assertTrue(world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir(), "Unsupported door disappears");
                int drops = world.getEntitiesByClass(ItemEntity.class, new Box(pos).expand(3), entity -> entity.getStack().isOf(door.asItem())).stream().mapToInt(entity -> entity.getStack().getCount()).sum();
                context.assertTrue(drops == 1, "Support removal drops exactly one door");
            }
        }
        context.complete();
    }
}
