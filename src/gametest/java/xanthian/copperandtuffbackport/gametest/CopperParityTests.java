package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@net.minecraftforge.gametest.GameTestHolder(Initialise.MOD_ID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class CopperParityTests {
    private static final String[] LEVELS = {"", "exposed_", "weathered_", "oxidized_"};
    private static final String[] FAMILIES = {"copper_block", "cut_copper", "cut_copper_slab", "cut_copper_stairs",
            "chiseled_copper", "copper_door", "copper_trapdoor", "copper_grate", "copper_bulb"};

    private static Block copper(String family, int level, boolean waxed) {
        String name = LEVELS[level] + (family.equals("copper_block") && level > 0 ? "copper" : family);
        boolean vanilla = family.equals("copper_block") || family.startsWith("cut_copper");
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(vanilla ? "minecraft" : Initialise.MOD_ID, (waxed ? "waxed_" : "") + name));
    }

    private static BlockState configured(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) state = state.setValue(BlockStateProperties.WATERLOGGED, true);
        if (state.hasProperty(BlockStateProperties.OPEN)) state = state.setValue(BlockStateProperties.OPEN, true);
        if (state.hasProperty(BlockStateProperties.POWERED)) state = state.setValue(BlockStateProperties.POWERED, true);
        if (state.hasProperty(BlockStateProperties.LIT)) state = state.setValue(BlockStateProperties.LIT, true);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
        return state;
    }

    private static void place(GameTestHelper context, BlockPos pos, BlockState state) {
        var world = context.getLevel();
        // Remove both halves before changing to another family.
        world.removeBlock(pos.above(), false);
        world.removeBlock(pos, false);
        world.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        world.setBlockAndUpdate(pos, state);
        if (state.getBlock() instanceof DoorBlock) {
            world.setBlockAndUpdate(pos.above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void recipeAdvancementsUnlockTheCorrectRecipes(GameTestHelper context) {
        var world = context.getLevel();
        var player = ForgeTestPlayers.creative(context);
        var tracker = player.getAdvancements();
        int checked = 0;
        for (var advancement : world.getServer().getAdvancements().getAllAdvancements()) {
            var id = advancement.getId();
            if (!id.getNamespace().equals(Initialise.MOD_ID) || !id.getPath().startsWith("recipes/")) continue;
            String name = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
            var recipe = world.getRecipeManager().byKey(new ResourceLocation(Initialise.MOD_ID, name)).orElseThrow();
            player.getRecipeBook().remove(recipe);
            tracker.revoke(advancement, "has_the_recipe");
            tracker.award(advancement, "has_the_recipe");
            context.assertTrue(player.getRecipeBook().contains(recipe), "Advancement must unlock mod recipe: " + name);
            checked++;
        }
        context.assertTrue(checked == 136, "All 136 mod recipe advancements must be tested");
        player.discard();
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void waxingAwardsVanillaAdvancements(GameTestHelper context) {
        var world = context.getLevel();
        var player = ForgeTestPlayers.creative(context);
        var tracker = player.getAdvancements();
        var waxOn = world.getServer().getAdvancements().getAdvancement(new ResourceLocation("minecraft", "husbandry/wax_on"));
        var waxOff = world.getServer().getAdvancements().getAdvancement(new ResourceLocation("minecraft", "husbandry/wax_off"));
        context.assertTrue(waxOn != null && waxOff != null, "Vanilla waxing advancements must load");
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
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
            tracker.revoke(waxOn, "wax_on");
            tracker.revoke(waxOff, "wax_off");
            place(context, pos, block.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
            Items.HONEYCOMB.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            context.assertTrue(tracker.getOrStartProgress(waxOn).isDone(), "Wax On must recognize " + BuiltInRegistries.BLOCK.getKey(block));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            Items.IRON_AXE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            context.assertTrue(tracker.getOrStartProgress(waxOff).isDone(), "Wax Off must recognize " + BuiltInRegistries.BLOCK.getKey(block));
        }
        player.discard();
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void axeAndHoneycombInteractions(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) {
                BlockState original = configured(copper(family, level, false));
                place(context, pos, original);
                // Powered bulbs only retain LIT when actual power agrees with POWERED.
                if (original.hasProperty(BlockStateProperties.LIT)) {
                    world.setBlockAndUpdate(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                    original = world.getBlockState(pos);
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
                var waxResult = Items.HONEYCOMB.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                Block waxed = copper(family, level, true);
                context.assertTrue(waxResult.consumesAction() && world.getBlockState(pos).equals(waxed.withPropertiesOf(original)), "Waxing preserves properties: " + family);
                context.assertTrue(player.getMainHandItem().getCount() == 1, "Waxing consumes one honeycomb");
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                var unwaxResult = Items.IRON_AXE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                context.assertTrue(unwaxResult.consumesAction() && world.getBlockState(pos).equals(original), "Axe removes wax before oxidation: " + family);
                if (level > 0) {
                    Items.IRON_AXE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                    context.assertTrue(world.getBlockState(pos).equals(copper(family, level - 1, false).withPropertiesOf(original)), "Scraping removes one oxidation level: " + family);
                } else {
                    var result = Items.IRON_AXE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                    context.assertTrue(!result.consumesAction(), "Unaffected copper cannot be scraped");
                }
                world.removeBlock(pos.east(), false);
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void weatheringAndWaxProtection(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var random = new LegacyRandomSource(0) { @Override public float nextFloat() { return 0.0F; } };
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) {
                Block original = copper(family, level, false);
                place(context, pos, original.defaultBlockState());
                if (level < 3) {
                    original.randomTick(world.getBlockState(pos), world, pos, random);
                    context.assertTrue(world.getBlockState(pos).is(copper(family, level + 1, false)), "Random tick oxidizes: " + family);
                    if (original instanceof DoorBlock) {
                        context.assertTrue(world.getBlockState(pos.above()).is(copper(family, level + 1, false)), "Weathering synchronizes door halves");
                        BlockState upper = world.getBlockState(pos.above());
                        upper.getBlock().randomTick(upper, world, pos.above(), random);
                        context.assertTrue(world.getBlockState(pos.above()).equals(upper), "Door upper halves do not weather independently");
                    }
                    place(context, pos, original.defaultBlockState());
                    if (level > 0) {
                        world.setBlockAndUpdate(pos.east(2), copper("copper_block", level - 1, false).defaultBlockState());
                        original.randomTick(world.getBlockState(pos), world, pos, random);
                        context.assertTrue(world.getBlockState(pos).is(original), "Less oxidized nearby copper prevents weathering: " + family);
                        world.removeBlock(pos.east(2), false);
                    }
                } else {
                    context.assertTrue(!original.isRandomlyTicking(original.defaultBlockState()), "Fully oxidized copper stops weathering");
                }
                Block waxed = copper(family, level, true);
                context.assertTrue(!(waxed instanceof WeatheringCopper) && !waxed.isRandomlyTicking(waxed.defaultBlockState()), "Wax prevents weathering: " + family);
                context.assertTrue(WeatheringCopper.getPrevious(waxed).isEmpty(), "Lightning cannot scrape waxed copper");
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void doorsAndTrapdoorsRespondToRedstoneAndPlayers(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (String family : new String[]{"copper_door", "copper_trapdoor"}) {
            for (int level = 0; level < LEVELS.length; level++) {
                for (boolean waxed : new boolean[]{false, true}) {
                    Block block = copper(family, level, waxed);
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        place(context, pos, block.defaultBlockState());
                        world.setBlockAndUpdate(pos.relative(side), Blocks.REDSTONE_BLOCK.defaultBlockState());
                        context.assertTrue(world.getBlockState(pos).getValue(BlockStateProperties.OPEN) && world.getBlockState(pos).getValue(BlockStateProperties.POWERED), "Redstone opens " + family);
                        block.use(world.getBlockState(pos), world, pos, player, InteractionHand.MAIN_HAND, hit);
                        context.assertTrue(!world.getBlockState(pos).getValue(BlockStateProperties.OPEN), "Player can close powered " + family);
                        block.use(world.getBlockState(pos), world, pos, player, InteractionHand.MAIN_HAND, hit);
                        context.assertTrue(world.getBlockState(pos).getValue(BlockStateProperties.OPEN), "Player can reopen powered " + family);
                        world.removeBlock(pos.relative(side), false);
                        context.assertTrue(!world.getBlockState(pos).getValue(BlockStateProperties.OPEN) && !world.getBlockState(pos).getValue(BlockStateProperties.POWERED), "Power removal closes " + family);
                    }
                }
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void allCopperLoot(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        for (String family : FAMILIES) {
            for (int level = 0; level < LEVELS.length; level++) {
                for (boolean waxed : new boolean[]{false, true}) {
                    Block block = copper(family, level, waxed);
                    BlockState state = block.defaultBlockState();
                    context.assertTrue(state.getDestroySpeed(world, pos) == 3.0F && block.getExplosionResistance() == 6.0F, "Copper strength: " + family);
                    var drops = Block.getDrops(state, world, pos, null, null, Items.STONE_PICKAXE.getDefaultInstance());
                    context.assertTrue(drops.size() == 1 && drops.get(0).is(block.asItem()) && drops.get(0).getCount() == 1, "Copper drops itself: " + family);
                    if (block instanceof SlabBlock) {
                        var doubleDrops = Block.getDrops(state.setValue(SlabBlock.TYPE, SlabType.DOUBLE), world, pos, null, null, Items.STONE_PICKAXE.getDefaultInstance());
                        context.assertTrue(doubleDrops.stream().mapToInt(ItemStack::getCount).sum() == 2, "Double copper slabs drop two slabs");
                    }
                    if (block instanceof DoorBlock) {
                        var upperDrops = Block.getDrops(state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), world, pos, null, null, Items.STONE_PICKAXE.getDefaultInstance());
                        context.assertTrue(upperDrops.isEmpty(), "Only one door item drops per door");
                    }
                }
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void doorsRejectUnsuitableToolsAndDropOnSupportLoss(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        for (int level = 0; level < LEVELS.length; level++) {
            for (boolean waxed : new boolean[]{false, true}) {
                Block door = copper("copper_door", level, waxed);
                context.assertTrue(door.defaultBlockState().getPistonPushReaction() == PushReaction.DESTROY, "Pistons destroy copper doors");
                place(context, pos, door.defaultBlockState());
                world.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), entity -> true).forEach(ItemEntity::discard);
                player.setItemInHand(InteractionHand.MAIN_HAND, Items.WOODEN_PICKAXE.getDefaultInstance());
                BlockState upper = world.getBlockState(pos.above());
                door.playerWillDestroy(world, pos.above(), upper, player);
                world.removeBlock(pos.above(), false);
                context.assertTrue(world.getBlockState(pos).isAir(), "Breaking upper half removes lower half");
                context.assertTrue(world.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), entity -> entity.getItem().is(door.asItem())).isEmpty(), "Unsuitable tool must not drop a door from its other half");
                place(context, pos, door.defaultBlockState());
                world.removeBlock(pos.below(), false);
                context.assertTrue(world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir(), "Unsupported door disappears");
                int drops = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), entity -> entity.getItem().is(door.asItem())).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
                context.assertTrue(drops == 1, "Support removal drops exactly one door");
            }
        }
        context.succeed();
    }
}
