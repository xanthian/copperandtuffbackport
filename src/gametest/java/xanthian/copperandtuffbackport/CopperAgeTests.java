package xanthian.copperandtuffbackport;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.CheckedRandom;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;

public class CopperAgeTests implements FabricGameTest {
    private static final CheckedRandom ALWAYS_WEATHER = new CheckedRandom(0) {
        @Override public float nextFloat() { return 0; }
    };

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void copperAgeRecipesAndComparator(TestContext context) {
        var world = context.getWorld();
        var recipes = world.getRecipeManager();
        for (String family : new String[]{"copper_bars", "copper_chest"}) {
            var recipe = recipes.get(new Identifier(Initialise.MOD_ID, family)).orElseThrow();
            int ingots = 0;
            int chests = 0;
            for (var ingredient : recipe.getIngredients()) {
                if (ingredient.test(Items.COPPER_INGOT.getDefaultStack())) ingots++;
                else if (ingredient.test(Items.CHEST.getDefaultStack())) chests++;
                else context.assertTrue(false, "Unexpected Copper Age crafting ingredient");
            }
            context.assertTrue(ingots == (family.equals("copper_bars") ? 6 : 8) && chests == (family.equals("copper_bars") ? 0 : 1), "Vanilla crafting inputs");
            context.assertTrue(recipe.getOutput(world.getRegistryManager()).getCount() == (family.equals("copper_bars") ? 16 : 1), "Vanilla crafting output");
            for (String prefix : ModCopperAgeBlocks.PREFIXES) {
                var waxing = recipes.get(new Identifier(Initialise.MOD_ID, "waxed_" + prefix + family + "_from_honeycomb")).orElseThrow();
                context.assertTrue(waxing.getIngredients().size() == 2 && waxing.getOutput(world.getRegistryManager()).getCount() == 1, "Waxing recipes exist for all oxidation levels");
            }
        }
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (var chest : ModCopperAgeBlocks.CHESTS) {
            world.setBlockState(pos, chest.getDefaultState());
            var entity = (CopperChestBlockEntity)world.getBlockEntity(pos);
            entity.clear();
            context.assertTrue(chest.getComparatorOutput(world.getBlockState(pos), world, pos) == 0, "Empty chest comparator output");
            for (int slot = 0; slot < entity.size(); slot++) entity.setStack(slot, new ItemStack(Items.COBBLESTONE, 64));
            context.assertTrue(chest.getComparatorOutput(world.getBlockState(pos), world, pos) == 15, "Full chest comparator output");
            world.setBlockState(pos.up(), Blocks.STONE.getDefaultState());
            context.assertTrue(chest.createScreenHandlerFactory(world.getBlockState(pos), world, pos) == null, "Solid blocks obstruct opening");
            world.removeBlock(pos.up(), false);
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void barsWeatherWaxConnectAndDrop(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        for (int i = 0; i < 8; i++) {
            Block bars = ModCopperAgeBlocks.BARS[i];
            world.setBlockState(pos, bars.getDefaultState().with(Properties.WATERLOGGED, true));
            world.setBlockState(pos.east(), Blocks.IRON_BARS.getDefaultState());
            world.setBlockState(pos.west(), Blocks.STONE.getDefaultState());
            context.assertTrue(world.getBlockState(pos).get(PaneBlock.EAST) && world.getBlockState(pos).get(PaneBlock.WEST), "Copper bars connect to iron bars and solid faces");
            context.assertTrue(Items.WOODEN_PICKAXE.getDefaultStack().isSuitableFor(bars.getDefaultState()), "Wooden pickaxes harvest bars");
            context.assertTrue(bars.getDefaultState().getHardness(world, pos) == 5, "Bars hardness");
            context.assertTrue(bars.getDefaultState().getMapColor(world, pos) == MapColor.CLEAR, "All bar variants have no map colour, matching 1.21.9");
            var drops = Block.getDroppedStacks(bars.getDefaultState(), world, pos, null, null, Items.WOODEN_PICKAXE.getDefaultStack());
            context.assertTrue(drops.size() == 1 && drops.get(0).isOf(bars.asItem()), "Bars drop their exact variant");
            BlockState state = world.getBlockState(pos);
            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(i < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
            player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
            Block target = ModCopperAgeBlocks.BARS[i < 4 ? i + 4 : i - 4];
            context.assertTrue(world.getBlockState(pos).equals(target.getStateWithProperties(state)), "Waxing and unwaxing preserve bar connections and water");
            world.setBlockState(pos, state);
            if (i < 3) {
                bars.randomTick(state, world, pos, ALWAYS_WEATHER);
                context.assertTrue(world.getBlockState(pos).equals(ModCopperAgeBlocks.BARS[i + 1].getStateWithProperties(state)), "Weathering preserves bar connections");
            } else {
                context.assertTrue(!bars.hasRandomTicks(state), "Waxed and oxidized bars stop weathering");
            }
            world.removeBlock(pos.east(), false);
            world.removeBlock(pos.west(), false);
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chestInventorySurvivesTransformationsAndSave(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        for (int i = 0; i < 8; i++) {
            world.removeBlock(pos, false);
            Block chest = ModCopperAgeBlocks.CHESTS[i];
            world.setBlockState(pos, chest.getDefaultState().with(Properties.WATERLOGGED, true));
            var entity = (CopperChestBlockEntity)world.getBlockEntity(pos);
            entity.setStack(0, new ItemStack(Items.DIAMOND, 23));
            entity.setCustomName(Text.literal("Stored treasure"));
            context.assertTrue(entity.size() == 27, "Single copper chest holds 27 stacks");
            context.assertTrue(chest.getDefaultState().isToolRequired() && !Items.WOODEN_PICKAXE.getDefaultStack().isSuitableFor(chest.getDefaultState())
                    && Items.STONE_PICKAXE.getDefaultStack().isSuitableFor(chest.getDefaultState()), "Chests require stone pickaxes");
            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(i < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
            player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
            context.assertTrue(world.getBlockEntity(pos) == entity && entity.getStack(0).getCount() == 23, "Waxing preserves the inventory entity");
            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            Items.IRON_AXE.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
            context.assertTrue(world.getBlockEntity(pos) == entity && entity.getStack(0).isOf(Items.DIAMOND), "Scraping preserves inventory");
            context.assertTrue(world.getBlockState(pos).get(Properties.WATERLOGGED), "Chest transformations preserve waterlogging");
            NbtCompound nbt = entity.createNbt();
            var restored = new CopperChestBlockEntity(pos, world.getBlockState(pos));
            restored.readNbt(nbt);
            context.assertTrue(restored.getStack(0).getCount() == 23 && restored.getName().getString().equals("Stored treasure"), "Saved chest retains inventory and name");
            var drops = Block.getDroppedStacks(world.getBlockState(pos), world, pos, entity, null, Items.STONE_PICKAXE.getDefaultStack());
            context.assertTrue(drops.size() == 1 && drops.get(0).getName().getString().equals("Stored treasure"), "Dropped chest retains custom name");
            world.getEntitiesByClass(ItemEntity.class, new Box(pos).expand(3), e -> true).forEach(ItemEntity::discard);
            world.breakBlock(pos, true);
            int diamonds = world.getEntitiesByClass(ItemEntity.class, new Box(pos).expand(3), e -> e.getStack().isOf(Items.DIAMOND))
                    .stream().mapToInt(e -> e.getStack().getCount()).sum();
            context.assertTrue(diamonds == 23, "Breaking chest scatters stored items exactly once");
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void doubleChestsMergeAndKeepBothInventories(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos first = context.getAbsolutePos(new BlockPos(3, 3, 3));
        BlockPos second = first.east();
        for (int a = 0; a < 8; a++) {
            for (int b = 0; b < 8; b++) {
                world.removeBlock(first, false);
                world.removeBlock(second, false);
                world.setBlockState(first, ModCopperAgeBlocks.CHESTS[a].getDefaultState().with(ChestBlock.FACING, Direction.NORTH));
                var firstEntity = (CopperChestBlockEntity)world.getBlockEntity(first);
                firstEntity.setStack(0, new ItemStack(Items.DIAMOND, 7));
                player.setYaw(0); // Placement faces north.
                var blockHit = new BlockHitResult(Vec3d.ofCenter(first), Direction.EAST, first, false);
                var stack = new ItemStack(ModCopperAgeBlocks.CHESTS[b]);
                var placement = new ItemPlacementContext(world, player, Hand.MAIN_HAND, stack, blockHit);
                BlockState state = ModCopperAgeBlocks.CHESTS[b].getPlacementState(placement);
                world.setBlockState(second, state);
                int expected = (a >= 4 && b >= 4 ? 4 : 0) + Math.min(a % 4, b % 4);
                context.assertTrue(world.getBlockState(first).isOf(ModCopperAgeBlocks.CHESTS[expected])
                        && world.getBlockState(second).isOf(ModCopperAgeBlocks.CHESTS[expected]), "Double chest adopts least oxidation and only retains wax if both were waxed");
                var secondEntity = (CopperChestBlockEntity)world.getBlockEntity(second);
                secondEntity.setStack(0, new ItemStack(Items.EMERALD, 9));
                context.assertTrue(world.getBlockEntity(first) == firstEntity && firstEntity.getStack(0).getCount() == 7, "Merging retains existing inventory");
                var inventory = ChestBlock.getInventory(ModCopperAgeBlocks.CHESTS[expected], world.getBlockState(first), world, first, false);
                context.assertTrue(inventory != null && inventory.size() == 54, "Double copper chest exposes 54 slots");
                var hit = new BlockHitResult(Vec3d.ofCenter(first), Direction.UP, first, false);
                player.setStackInHand(Hand.MAIN_HAND, new ItemStack(expected < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
                player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                context.assertTrue(world.getBlockState(first).getBlock() == world.getBlockState(second).getBlock(), "Transforming one half transforms both");
                context.assertTrue(world.getBlockEntity(first) == firstEntity && world.getBlockEntity(second) == secondEntity
                        && secondEntity.getStack(0).getCount() == 9, "Both inventories survive double chest transformation");
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chestWeatheringPausesWhileOpen(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 3; i++) {
            world.setBlockState(pos, ModCopperAgeBlocks.CHESTS[i].getDefaultState());
            var entity = (CopperChestBlockEntity)world.getBlockEntity(pos);
            entity.setStack(0, new ItemStack(Items.DIAMOND));
            entity.onOpen(player);
            ModCopperAgeBlocks.CHESTS[i].randomTick(world.getBlockState(pos), world, pos, ALWAYS_WEATHER);
            context.assertTrue(world.getBlockState(pos).isOf(ModCopperAgeBlocks.CHESTS[i]), "Open chests do not weather");
            entity.onClose(player);
            ModCopperAgeBlocks.CHESTS[i].randomTick(world.getBlockState(pos), world, pos, ALWAYS_WEATHER);
            context.assertTrue(world.getBlockState(pos).isOf(ModCopperAgeBlocks.CHESTS[i + 1])
                    && world.getBlockEntity(pos) == entity && entity.getStack(0).isOf(Items.DIAMOND), "Closed chest weathers without losing inventory");
        }
        context.complete();
    }
}
