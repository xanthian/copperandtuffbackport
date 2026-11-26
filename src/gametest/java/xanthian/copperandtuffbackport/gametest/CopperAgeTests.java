package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;

@net.minecraftforge.gametest.GameTestHolder(Initialise.MOD_ID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class CopperAgeTests {
    private static final LegacyRandomSource ALWAYS_WEATHER = new LegacyRandomSource(0) {
        @Override public float nextFloat() { return 0; }
    };

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void copperAgeRecipesAndComparator(GameTestHelper context) {
        var world = context.getLevel();
        var recipes = world.getRecipeManager();
        for (String family : new String[]{"copper_bars", "copper_chest"}) {
            var recipe = recipes.byKey(new ResourceLocation(Initialise.MOD_ID, family)).orElseThrow();
            int ingots = 0;
            int chests = 0;
            for (var ingredient : recipe.getIngredients()) {
                if (ingredient.test(Items.COPPER_INGOT.getDefaultInstance())) ingots++;
                else if (ingredient.test(Items.CHEST.getDefaultInstance())) chests++;
                else context.assertTrue(false, "Unexpected Copper Age crafting ingredient");
            }
            context.assertTrue(ingots == (family.equals("copper_bars") ? 6 : 8) && chests == (family.equals("copper_bars") ? 0 : 1), "Vanilla crafting inputs");
            context.assertTrue(recipe.getResultItem(world.registryAccess()).getCount() == (family.equals("copper_bars") ? 16 : 1), "Vanilla crafting output");
            for (String prefix : ModCopperAgeBlocks.PREFIXES) {
                var waxing = recipes.byKey(new ResourceLocation(Initialise.MOD_ID, "waxed_" + prefix + family + "_from_honeycomb")).orElseThrow();
                context.assertTrue(waxing.getIngredients().size() == 2 && waxing.getResultItem(world.registryAccess()).getCount() == 1, "Waxing recipes exist for all oxidation levels");
            }
        }
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        for (var chest : ModCopperAgeBlocks.CHESTS) {
            world.setBlockAndUpdate(pos, chest.defaultBlockState());
            var entity = (CopperChestBlockEntity)world.getBlockEntity(pos);
            entity.clearContent();
            context.assertTrue(chest.getAnalogOutputSignal(world.getBlockState(pos), world, pos) == 0, "Empty chest comparator output");
            for (int slot = 0; slot < entity.getContainerSize(); slot++) entity.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            context.assertTrue(chest.getAnalogOutputSignal(world.getBlockState(pos), world, pos) == 15, "Full chest comparator output");
            world.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            context.assertTrue(chest.getMenuProvider(world.getBlockState(pos), world, pos) == null, "Solid blocks obstruct opening");
            world.removeBlock(pos.above(), false);
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void barsWeatherWaxConnectAndDrop(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (int i = 0; i < 8; i++) {
            Block bars = ModCopperAgeBlocks.BARS[i];
            world.setBlockAndUpdate(pos, bars.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
            world.setBlockAndUpdate(pos.east(), Blocks.IRON_BARS.defaultBlockState());
            world.setBlockAndUpdate(pos.west(), Blocks.STONE.defaultBlockState());
            context.assertTrue(world.getBlockState(pos).getValue(IronBarsBlock.EAST) && world.getBlockState(pos).getValue(IronBarsBlock.WEST), "Copper bars connect to iron bars and solid faces");
            context.assertTrue(Items.WOODEN_PICKAXE.getDefaultInstance().isCorrectToolForDrops(bars.defaultBlockState()), "Wooden pickaxes harvest bars");
            context.assertTrue(bars.defaultBlockState().getDestroySpeed(world, pos) == 5, "Bars hardness");
            context.assertTrue(bars.defaultBlockState().getMapColor(world, pos) == MapColor.NONE, "All bar variants have no map colour, matching 1.21.9");
            var drops = Block.getDrops(bars.defaultBlockState(), world, pos, null, null, Items.WOODEN_PICKAXE.getDefaultInstance());
            context.assertTrue(drops.size() == 1 && drops.get(0).is(bars.asItem()), "Bars drop their exact variant");
            BlockState state = world.getBlockState(pos);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(i < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            Block target = ModCopperAgeBlocks.BARS[i < 4 ? i + 4 : i - 4];
            context.assertTrue(world.getBlockState(pos).equals(target.withPropertiesOf(state)), "Waxing and unwaxing preserve bar connections and water");
            world.setBlockAndUpdate(pos, state);
            if (i < 3) {
                bars.randomTick(state, world, pos, ALWAYS_WEATHER);
                context.assertTrue(world.getBlockState(pos).equals(ModCopperAgeBlocks.BARS[i + 1].withPropertiesOf(state)), "Weathering preserves bar connections");
            } else {
                context.assertTrue(!bars.isRandomlyTicking(state), "Waxed and oxidized bars stop weathering");
            }
            world.removeBlock(pos.east(), false);
            world.removeBlock(pos.west(), false);
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void chestInventorySurvivesTransformationsAndSave(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (int i = 0; i < 8; i++) {
            world.removeBlock(pos, false);
            Block chest = ModCopperAgeBlocks.CHESTS[i];
            world.setBlockAndUpdate(pos, chest.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
            var entity = (CopperChestBlockEntity)world.getBlockEntity(pos);
            entity.setItem(0, new ItemStack(Items.DIAMOND, 23));
            entity.setCustomName(Component.literal("Stored treasure"));
            context.assertTrue(entity.getContainerSize() == 27, "Single copper chest holds 27 stacks");
            context.assertTrue(chest.defaultBlockState().requiresCorrectToolForDrops() && !Items.WOODEN_PICKAXE.getDefaultInstance().isCorrectToolForDrops(chest.defaultBlockState())
                    && Items.STONE_PICKAXE.getDefaultInstance().isCorrectToolForDrops(chest.defaultBlockState()), "Chests require stone pickaxes");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(i < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            context.assertTrue(world.getBlockEntity(pos) == entity && entity.getItem(0).getCount() == 23, "Waxing preserves the inventory entity");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            Items.IRON_AXE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            context.assertTrue(world.getBlockEntity(pos) == entity && entity.getItem(0).is(Items.DIAMOND), "Scraping preserves inventory");
            context.assertTrue(world.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED), "Chest transformations preserve waterlogging");
            CompoundTag nbt = entity.saveWithoutMetadata();
            var restored = new CopperChestBlockEntity(pos, world.getBlockState(pos));
            restored.load(nbt);
            context.assertTrue(restored.getItem(0).getCount() == 23 && restored.getName().getString().equals("Stored treasure"), "Saved chest retains inventory and name");
            var drops = Block.getDrops(world.getBlockState(pos), world, pos, entity, null, Items.STONE_PICKAXE.getDefaultInstance());
            context.assertTrue(drops.size() == 1 && drops.get(0).getHoverName().getString().equals("Stored treasure"), "Dropped chest retains custom name");
            world.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), e -> true).forEach(ItemEntity::discard);
            world.destroyBlock(pos, true);
            int diamonds = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), e -> e.getItem().is(Items.DIAMOND))
                    .stream().mapToInt(e -> e.getItem().getCount()).sum();
            context.assertTrue(diamonds == 23, "Breaking chest scatters stored items exactly once");
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void doubleChestsMergeAndKeepBothInventories(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos first = context.absolutePos(new BlockPos(3, 3, 3));
        BlockPos second = first.east();
        for (int a = 0; a < 8; a++) {
            for (int b = 0; b < 8; b++) {
                world.removeBlock(first, false);
                world.removeBlock(second, false);
                world.setBlockAndUpdate(first, ModCopperAgeBlocks.CHESTS[a].defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
                var firstEntity = (CopperChestBlockEntity)world.getBlockEntity(first);
                firstEntity.setItem(0, new ItemStack(Items.DIAMOND, 7));
                player.setYRot(0); // Placement faces north.
                var blockHit = new BlockHitResult(Vec3.atCenterOf(first), Direction.EAST, first, false);
                var stack = new ItemStack(ModCopperAgeBlocks.CHESTS[b]);
                var placement = new BlockPlaceContext(world, player, InteractionHand.MAIN_HAND, stack, blockHit);
                BlockState state = ModCopperAgeBlocks.CHESTS[b].getStateForPlacement(placement);
                world.setBlockAndUpdate(second, state);
                int expected = (a >= 4 && b >= 4 ? 4 : 0) + Math.min(a % 4, b % 4);
                context.assertTrue(world.getBlockState(first).is(ModCopperAgeBlocks.CHESTS[expected])
                        && world.getBlockState(second).is(ModCopperAgeBlocks.CHESTS[expected]), "Double chest adopts least oxidation and only retains wax if both were waxed");
                var secondEntity = (CopperChestBlockEntity)world.getBlockEntity(second);
                secondEntity.setItem(0, new ItemStack(Items.EMERALD, 9));
                context.assertTrue(world.getBlockEntity(first) == firstEntity && firstEntity.getItem(0).getCount() == 7, "Merging retains existing inventory");
                var inventory = ChestBlock.getContainer(ModCopperAgeBlocks.CHESTS[expected], world.getBlockState(first), world, first, false);
                context.assertTrue(inventory != null && inventory.getContainerSize() == 54, "Double copper chest exposes 54 slots");
                var hit = new BlockHitResult(Vec3.atCenterOf(first), Direction.UP, first, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(expected < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
                player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                context.assertTrue(world.getBlockState(first).getBlock() == world.getBlockState(second).getBlock(), "Transforming one half transforms both");
                context.assertTrue(world.getBlockEntity(first) == firstEntity && world.getBlockEntity(second) == secondEntity
                        && secondEntity.getItem(0).getCount() == 9, "Both inventories survive double chest transformation");
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void chestWeatheringPausesWhileOpen(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 3; i++) {
            world.setBlockAndUpdate(pos, ModCopperAgeBlocks.CHESTS[i].defaultBlockState());
            var entity = (CopperChestBlockEntity)world.getBlockEntity(pos);
            entity.setItem(0, new ItemStack(Items.DIAMOND));
            entity.startOpen(player);
            ModCopperAgeBlocks.CHESTS[i].randomTick(world.getBlockState(pos), world, pos, ALWAYS_WEATHER);
            context.assertTrue(world.getBlockState(pos).is(ModCopperAgeBlocks.CHESTS[i]), "Open chests do not weather");
            entity.stopOpen(player);
            ModCopperAgeBlocks.CHESTS[i].randomTick(world.getBlockState(pos), world, pos, ALWAYS_WEATHER);
            context.assertTrue(world.getBlockState(pos).is(ModCopperAgeBlocks.CHESTS[i + 1])
                    && world.getBlockEntity(pos) == entity && entity.getItem(0).is(Items.DIAMOND), "Closed chest weathers without losing inventory");
        }
        context.succeed();
    }
}
