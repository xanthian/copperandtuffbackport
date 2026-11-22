package xanthian.copperandtuffbackport;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.poi.PointOfInterestTypes;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;

public class CopperDecorationTests implements FabricGameTest, net.fabricmc.api.ModInitializer {
    private static final Item LEGACY_NUGGET = new Item(new Item.Settings());
    private static final Item MODERN_NUGGET = new Item(new Item.Settings());

    @Override public void onInitialize() {
        net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.ITEM,
                new Identifier("copperandtuffbackport_test", "legacy_copper_nugget"), LEGACY_NUGGET);
        net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.ITEM,
                new Identifier("copperandtuffbackport_test", "modern_copper_nugget"), MODERN_NUGGET);
    }
    private static final CheckedRandom WEATHER = new CheckedRandom(0) {
        @Override public float nextFloat() { return 0; }
    };

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void decorationsPreserveStatesAndDropsThroughTransformations(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockSurvivalPlayer();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
        world.setBlockState(pos.up(), Blocks.STONE.getDefaultState());
        var hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        for (Block[] family : new Block[][]{ModCopperDecorations.LANTERNS, ModCopperDecorations.CHAINS, ModCopperDecorations.RODS}) {
            for (int i = 0; i < 8; i++) {
                Block block = family[i];
                BlockState state = block.getDefaultState().with(Properties.WATERLOGGED, true);
                if (state.contains(Properties.HANGING)) state = state.with(Properties.HANGING, true);
                if (state.contains(Properties.AXIS)) state = state.with(Properties.AXIS, Direction.Axis.X);
                if (state.contains(Properties.FACING)) state = state.with(Properties.FACING, Direction.EAST).with(Properties.POWERED, true);
                world.setBlockState(pos, state);
                player.setStackInHand(Hand.MAIN_HAND, new ItemStack(i < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
                player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                context.assertTrue(world.getBlockState(pos).equals(family[i < 4 ? i + 4 : i - 4].getStateWithProperties(state)), "Waxing preserves all decoration properties: " + block);
                world.setBlockState(pos, state);
                if (i > 0 && i < 4) {
                    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                    player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit));
                    context.assertTrue(world.getBlockState(pos).equals(family[i - 1].getStateWithProperties(state)), "Axe removes one oxidation stage: " + block);
                }
                world.setBlockState(pos, state);
                if (i < 3) {
                    block.randomTick(state, world, pos, WEATHER);
                    context.assertTrue(world.getBlockState(pos).equals(family[i + 1].getStateWithProperties(state)), "Weathering preserves all properties: " + block);
                } else context.assertTrue(!block.hasRandomTicks(state), "Waxed and fully oxidized decorations stop weathering");
                var drops = Block.getDroppedStacks(state, world, pos, null, null, Items.STONE_PICKAXE.getDefaultStack());
                context.assertTrue(drops.size() == 1 && drops.get(0).isOf(block.asItem()), "Decoration drops its exact variant: " + block);
                if (block instanceof LanternBlock) {
                    context.assertTrue(state.getLuminance() == 15 && !state.isToolRequired(), "Copper lantern light and hand harvesting match release");
                }
                world.removeBlock(pos, false);
            }
        }
        context.assertTrue(ModCopperDecorations.TORCH.getDefaultState().getLuminance() == 14, "Copper torch emits level 14");
        var drops = Block.getDroppedStacks(ModCopperDecorations.WALL_TORCH.getDefaultState(), world, pos, null);
        context.assertTrue(drops.size() == 1 && drops.get(0).isOf(ModCopperDecorations.TORCH.asItem()), "Wall torch drops the standing torch item");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void everyRodAttractsLightningPowersAndCleans(TestContext context) {
        var world = context.getWorld();
        BlockPos origin = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 8; i++) {
            BlockPos pos = origin.up(i * 2);
            world.setBlockState(pos, ModCopperDecorations.RODS[i].getDefaultState());
            context.assertTrue(world.getPointOfInterestStorage().getType(pos).orElseThrow().matchesKey(PointOfInterestTypes.LIGHTNING_ROD), "Every rod participates in lightning attraction");
            var lightning = EntityType.LIGHTNING_BOLT.create(world);
            lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos.up()));
            lightning.tick();
            BlockState struck = world.getBlockState(pos);
            context.assertTrue(struck.isOf(ModCopperDecorations.RODS[i < 4 ? 0 : i]), "Lightning cleans unwaxed rods and preserves waxed rods: index=" + i + " state=" + struck);
            context.assertTrue(struck.get(Properties.POWERED), "Lightning powers every rod variant");
            context.assertTrue(struck.getWeakRedstonePower(world, pos, Direction.NORTH) == 15, "Struck rod emits redstone strength 15");
            struck.getBlock().scheduledTick(struck, world, pos, world.random);
            context.assertTrue(!world.getBlockState(pos).get(Properties.POWERED), "Rod pulse resets after scheduled tick");
            lightning.discard();
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void copperDecorationRecipes(TestContext context) {
        var world = context.getWorld();
        String[] names = {"copper_nugget", "copper_ingot_from_nuggets", "copper_torch", "copper_lantern", "copper_chain", "name_tag"};
        Item[] outputs = {ModCopperDecorations.NUGGET, Items.COPPER_INGOT, ModCopperDecorations.TORCH.asItem(), ModCopperDecorations.LANTERNS[0].asItem(), ModCopperDecorations.CHAINS[0].asItem(), Items.NAME_TAG};
        int[] counts = {9, 1, 4, 1, 1, 1};
        int[][] inputs = {{1, 0, 0, 0, 0}, {0, 9, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 8, 0, 0, 1}, {1, 2, 0, 0, 0}};
        Item[] ingredients = {Items.COPPER_INGOT, ModCopperDecorations.NUGGET, Items.COAL, Items.STICK, ModCopperDecorations.TORCH.asItem()};
        for (int i = 0; i < names.length; i++) {
            var recipe = world.getRecipeManager().get(new Identifier(Initialise.MOD_ID, names[i])).orElseThrow();
            var output = recipe.getOutput(world.getRegistryManager());
            context.assertTrue(output.isOf(outputs[i]) && output.getCount() == counts[i], "Correct crafting output: " + names[i]);
            if (i < inputs.length) {
                for (int j = 0; j < ingredients.length; j++) {
                    int found = 0;
                    for (var ingredient : recipe.getIngredients()) if (ingredient.test(ingredients[j].getDefaultStack())) found++;
                    context.assertTrue(found == inputs[i][j], "Correct ingredient count: " + names[i] + "/" + ingredients[j]);
                }
            } else {
                context.assertTrue(recipe.getIngredients().stream().filter(x -> x.test(Items.PAPER.getDefaultStack())).count() == 1, "Name tag uses one paper");
                for (Item nugget : new Item[]{Items.IRON_NUGGET, Items.GOLD_NUGGET, ModCopperDecorations.NUGGET})
                    context.assertTrue(recipe.getIngredients().stream().anyMatch(x -> x.test(nugget.getDefaultStack())), "Name tag accepts each metal nugget");
            }
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
    public void rodPulsesResetAfterActualScheduledTicks(TestContext context) {
        var world = context.getWorld();
        BlockPos origin = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 8; i++) {
            BlockPos pos = origin.up(i * 2);
            world.setBlockState(pos, ModCopperDecorations.RODS[i].getDefaultState());
            var lightning = EntityType.LIGHTNING_BOLT.create(world);
            lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos.up()));
            lightning.tick();
            lightning.discard();
        }
        context.waitAndRun(1, () -> {
            for (int i = 0; i < 8; i++) context.assertTrue(world.getBlockState(origin.up(i * 2)).get(Properties.POWERED), "Every rod retains its lightning pulse after oxidation removal");
        });
        context.waitAndRun(9, () -> {
            for (int i = 0; i < 8; i++) context.assertTrue(!world.getBlockState(origin.up(i * 2)).get(Properties.POWERED), "Every rod resets its pulse through the real tick scheduler");
            context.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void sharedNuggetsCraftAndUnlockRecipes(TestContext context) {
        var world = context.getWorld();
        var player = context.createMockCreativeServerPlayerInWorld();
        var grid = new net.minecraft.inventory.CraftingInventory(new net.minecraft.screen.ScreenHandler(null, -1) {
            @Override public boolean canUse(net.minecraft.entity.player.PlayerEntity player) { return true; }
            @Override public ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int slot) { return ItemStack.EMPTY; }
        }, 3, 3);
        for (String name : new String[]{"copper_ingot_from_nuggets", "copper_torch", "copper_lantern", "copper_chain", "name_tag"}) {
            var recipe = (net.minecraft.recipe.ShapedRecipe)world.getRecipeManager().get(new Identifier(Initialise.MOD_ID, name)).orElseThrow();
            for (int pass = 0; pass < 3; pass++) {
                grid.clear();
                for (int slot = 0; slot < recipe.getIngredients().size(); slot++) {
                    var ingredient = recipe.getIngredients().get(slot);
                    if (ingredient.isEmpty()) continue;
                    ItemStack stack = ingredient.getMatchingStacks()[0].copy();
                    if (ingredient.test(ModCopperDecorations.NUGGET.getDefaultStack())) {
                        Item replacement = pass == 0 ? LEGACY_NUGGET : pass == 1 ? MODERN_NUGGET
                                : new Item[]{LEGACY_NUGGET, MODERN_NUGGET, ModCopperDecorations.NUGGET}[slot % 3];
                        stack = replacement.getDefaultStack();
                        context.assertTrue(ingredient.test(stack), "Both shared tag conventions must be accepted by " + name);
                        if (!name.equals("name_tag")) {
                            context.assertTrue(!ingredient.test(Items.IRON_NUGGET.getDefaultStack()) && !ingredient.test(Items.GOLD_NUGGET.getDefaultStack()), "Copper recipes reject other metals");
                        }
                    }
                    grid.setStack(slot % recipe.getWidth() + slot / recipe.getWidth() * 3, stack);
                }
                context.assertTrue(recipe.matches(grid, world), "External and mixed nuggets craft " + name);
            }
            if (name.equals("copper_ingot_from_nuggets") || name.equals("copper_torch") || name.equals("copper_chain")) {
                var advancement = world.getServer().getAdvancementLoader().get(new Identifier(Initialise.MOD_ID,
                        "recipes/" + (name.equals("copper_ingot_from_nuggets") ? "misc/" : "decorations/") + name));
                for (Item nugget : new Item[]{LEGACY_NUGGET, MODERN_NUGGET}) {
                    player.getRecipeBook().remove(recipe);
                    for (String criterion : advancement.getCriteria().keySet()) player.getAdvancementTracker().revokeCriterion(advancement, criterion);
                    player.getInventory().clear();
                    player.getInventory().setStack(0, nugget.getDefaultStack());
                    net.minecraft.advancement.criterion.Criteria.INVENTORY_CHANGED.trigger(player, player.getInventory(), nugget.getDefaultStack());
                    context.assertTrue(player.getRecipeBook().contains(recipe), "External nugget unlocks " + name);
                }
            }
        }
        player.discard();
        context.complete();
    }
}
