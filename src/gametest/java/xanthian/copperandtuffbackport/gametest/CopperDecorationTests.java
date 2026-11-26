package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;

import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;

@net.minecraftforge.gametest.GameTestHolder(Initialise.MOD_ID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class CopperDecorationTests {
    private static final Item LEGACY_NUGGET = new Item(new Item.Properties());
    private static final Item MODERN_NUGGET = new Item(new Item.Properties());
    private static final Item FORGE_NUGGET = new Item(new Item.Properties());

    public static void registerItems(net.minecraftforge.registries.RegisterEvent event) {
        event.register(net.minecraft.core.registries.Registries.ITEM,
                new ResourceLocation("copperandtuffbackport_test", "forge_copper_nugget"), () -> FORGE_NUGGET);
        event.register(net.minecraft.core.registries.Registries.ITEM,
                new ResourceLocation("copperandtuffbackport_test", "legacy_copper_nugget"), () -> LEGACY_NUGGET);
        event.register(net.minecraft.core.registries.Registries.ITEM,
                new ResourceLocation("copperandtuffbackport_test", "modern_copper_nugget"), () -> MODERN_NUGGET);
    }
    private static final LegacyRandomSource WEATHER = new LegacyRandomSource(0) {
        @Override public float nextFloat() { return 0; }
    };

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void decorationsPreserveStatesAndDropsThroughTransformations(GameTestHelper context) {
        var world = context.getLevel();
        var player = context.makeMockSurvivalPlayer();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        world.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        world.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (Block[] family : new Block[][]{ModCopperDecorations.LANTERNS, ModCopperDecorations.CHAINS, ModCopperDecorations.RODS}) {
            for (int i = 0; i < 8; i++) {
                Block block = family[i];
                BlockState state = block.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
                if (state.hasProperty(BlockStateProperties.HANGING)) state = state.setValue(BlockStateProperties.HANGING, true);
                if (state.hasProperty(BlockStateProperties.AXIS)) state = state.setValue(BlockStateProperties.AXIS, Direction.Axis.X);
                if (state.hasProperty(BlockStateProperties.FACING)) state = state.setValue(BlockStateProperties.FACING, Direction.EAST).setValue(BlockStateProperties.POWERED, true);
                world.setBlockAndUpdate(pos, state);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(i < 4 ? Items.HONEYCOMB : Items.IRON_AXE));
                player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                context.assertTrue(world.getBlockState(pos).equals(family[i < 4 ? i + 4 : i - 4].withPropertiesOf(state)), "Waxing preserves all decoration properties: " + block);
                world.setBlockAndUpdate(pos, state);
                if (i > 0 && i < 4) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                    player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                    context.assertTrue(world.getBlockState(pos).equals(family[i - 1].withPropertiesOf(state)), "Axe removes one oxidation stage: " + block);
                }
                world.setBlockAndUpdate(pos, state);
                if (i < 3) {
                    block.randomTick(state, world, pos, WEATHER);
                    context.assertTrue(world.getBlockState(pos).equals(family[i + 1].withPropertiesOf(state)), "Weathering preserves all properties: " + block);
                } else context.assertTrue(!block.isRandomlyTicking(state), "Waxed and fully oxidized decorations stop weathering");
                var drops = Block.getDrops(state, world, pos, null, null, Items.STONE_PICKAXE.getDefaultInstance());
                context.assertTrue(drops.size() == 1 && drops.get(0).is(block.asItem()), "Decoration drops its exact variant: " + block);
                if (block instanceof LanternBlock) {
                    context.assertTrue(state.getLightEmission() == 15 && !state.requiresCorrectToolForDrops(), "Copper lantern light and hand harvesting match release");
                }
                world.removeBlock(pos, false);
            }
        }
        context.assertTrue(ModCopperDecorations.TORCH.defaultBlockState().getLightEmission() == 14, "Copper torch emits level 14");
        var drops = Block.getDrops(ModCopperDecorations.WALL_TORCH.defaultBlockState(), world, pos, null);
        context.assertTrue(drops.size() == 1 && drops.get(0).is(ModCopperDecorations.TORCH.asItem()), "Wall torch drops the standing torch item");
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void everyRodAttractsLightningPowersAndCleans(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos origin = context.absolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 8; i++) {
            BlockPos pos = origin.above(i * 2);
            world.setBlockAndUpdate(pos, ModCopperDecorations.RODS[i].defaultBlockState());
            context.assertTrue(world.getPoiManager().getType(pos).orElseThrow().is(PoiTypes.LIGHTNING_ROD), "Every rod participates in lightning attraction");
            var lightning = EntityType.LIGHTNING_BOLT.create(world);
            lightning.moveTo(Vec3.atBottomCenterOf(pos.above()));
            lightning.tick();
            BlockState struck = world.getBlockState(pos);
            context.assertTrue(struck.is(ModCopperDecorations.RODS[i < 4 ? 0 : i]), "Lightning cleans unwaxed rods and preserves waxed rods: index=" + i + " state=" + struck);
            context.assertTrue(struck.getValue(BlockStateProperties.POWERED), "Lightning powers every rod variant");
            context.assertTrue(struck.getSignal(world, pos, Direction.NORTH) == 15, "Struck rod emits redstone strength 15");
            struck.getBlock().tick(struck, world, pos, world.random);
            context.assertTrue(!world.getBlockState(pos).getValue(BlockStateProperties.POWERED), "Rod pulse resets after scheduled tick");
            lightning.discard();
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void copperDecorationRecipes(GameTestHelper context) {
        var world = context.getLevel();
        String[] names = {"copper_nugget", "copper_ingot_from_nuggets", "copper_torch", "copper_lantern", "copper_chain", "name_tag"};
        Item[] outputs = {ModCopperDecorations.NUGGET, Items.COPPER_INGOT, ModCopperDecorations.TORCH.asItem(), ModCopperDecorations.LANTERNS[0].asItem(), ModCopperDecorations.CHAINS[0].asItem(), Items.NAME_TAG};
        int[] counts = {9, 1, 4, 1, 1, 1};
        int[][] inputs = {{1, 0, 0, 0, 0}, {0, 9, 0, 0, 0}, {0, 1, 1, 1, 0}, {0, 8, 0, 0, 1}, {1, 2, 0, 0, 0}};
        Item[] ingredients = {Items.COPPER_INGOT, ModCopperDecorations.NUGGET, Items.COAL, Items.STICK, ModCopperDecorations.TORCH.asItem()};
        for (int i = 0; i < names.length; i++) {
            var recipe = world.getRecipeManager().byKey(new ResourceLocation(Initialise.MOD_ID, names[i])).orElseThrow();
            var output = recipe.getResultItem(world.registryAccess());
            context.assertTrue(output.is(outputs[i]) && output.getCount() == counts[i], "Correct crafting output: " + names[i]);
            if (i < inputs.length) {
                for (int j = 0; j < ingredients.length; j++) {
                    int found = 0;
                    for (var ingredient : recipe.getIngredients()) if (ingredient.test(ingredients[j].getDefaultInstance())) found++;
                    context.assertTrue(found == inputs[i][j], "Correct ingredient count: " + names[i] + "/" + ingredients[j]);
                }
            } else {
                context.assertTrue(recipe.getIngredients().stream().filter(x -> x.test(Items.PAPER.getDefaultInstance())).count() == 1, "Name tag uses one paper");
                for (Item nugget : new Item[]{Items.IRON_NUGGET, Items.GOLD_NUGGET, ModCopperDecorations.NUGGET})
                    context.assertTrue(recipe.getIngredients().stream().anyMatch(x -> x.test(nugget.getDefaultInstance())), "Name tag accepts each metal nugget");
            }
        }
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test", timeoutTicks = 20)
    public void rodPulsesResetAfterActualScheduledTicks(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos origin = context.absolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 8; i++) {
            BlockPos pos = origin.above(i * 2);
            world.setBlockAndUpdate(pos, ModCopperDecorations.RODS[i].defaultBlockState());
            var lightning = EntityType.LIGHTNING_BOLT.create(world);
            lightning.moveTo(Vec3.atBottomCenterOf(pos.above()));
            lightning.tick();
            lightning.discard();
        }
        context.runAfterDelay(1, () -> {
            for (int i = 0; i < 8; i++) context.assertTrue(world.getBlockState(origin.above(i * 2)).getValue(BlockStateProperties.POWERED), "Every rod retains its lightning pulse after oxidation removal");
        });
        context.runAfterDelay(9, () -> {
            for (int i = 0; i < 8; i++) context.assertTrue(!world.getBlockState(origin.above(i * 2)).getValue(BlockStateProperties.POWERED), "Every rod resets its pulse through the real tick scheduler");
            context.succeed();
        });
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void sharedNuggetsCraftAndUnlockRecipes(GameTestHelper context) {
        var world = context.getLevel();
        var player = ForgeTestPlayers.creative(context);
        var grid = new net.minecraft.world.inventory.TransientCraftingContainer(new CraftingMenu(), 3, 3);
        for (String name : new String[]{"copper_ingot_from_nuggets", "copper_torch", "copper_lantern", "copper_chain", "name_tag"}) {
            var recipe = (net.minecraft.world.item.crafting.ShapedRecipe)world.getRecipeManager().byKey(new ResourceLocation(Initialise.MOD_ID, name)).orElseThrow();
            for (int pass = 0; pass < 4; pass++) {
                grid.clearContent();
                for (int slot = 0; slot < recipe.getIngredients().size(); slot++) {
                    var ingredient = recipe.getIngredients().get(slot);
                    if (ingredient.isEmpty()) continue;
                    ItemStack stack = ingredient.getItems()[0].copy();
                    if (ingredient.test(ModCopperDecorations.NUGGET.getDefaultInstance())) {
                        Item replacement = pass == 0 ? LEGACY_NUGGET : pass == 1 ? MODERN_NUGGET : pass == 2 ? FORGE_NUGGET
                                : new Item[]{LEGACY_NUGGET, MODERN_NUGGET, FORGE_NUGGET, ModCopperDecorations.NUGGET}[slot % 4];
                        stack = replacement.getDefaultInstance();
                        context.assertTrue(ingredient.test(stack), "Both shared tag conventions must be accepted by " + name);
                        if (!name.equals("name_tag")) {
                            context.assertTrue(!ingredient.test(Items.IRON_NUGGET.getDefaultInstance()) && !ingredient.test(Items.GOLD_NUGGET.getDefaultInstance()), "Copper recipes reject other metals");
                        }
                    }
                    grid.setItem(slot % recipe.getWidth() + slot / recipe.getWidth() * 3, stack);
                }
                context.assertTrue(recipe.matches(grid, world), "External and mixed nuggets craft " + name);
            }
            if (name.equals("copper_ingot_from_nuggets") || name.equals("copper_torch") || name.equals("copper_chain")) {
                var advancement = world.getServer().getAdvancements().getAdvancement(new ResourceLocation(Initialise.MOD_ID,
                        "recipes/" + (name.equals("copper_ingot_from_nuggets") ? "misc/" : "decorations/") + name));
                for (Item nugget : new Item[]{LEGACY_NUGGET, MODERN_NUGGET, FORGE_NUGGET}) {
                    player.getRecipeBook().remove(recipe);
                    for (String criterion : advancement.getCriteria().keySet()) player.getAdvancements().revoke(advancement, criterion);
                    player.getInventory().clearContent();
                    player.getInventory().setItem(0, nugget.getDefaultInstance());
                    net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), nugget.getDefaultInstance());
                    context.assertTrue(player.getRecipeBook().contains(recipe), "External nugget unlocks " + name);
                }
            }
        }
        player.discard();
        context.succeed();
    }
    private static final class CraftingMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
        private CraftingMenu() { super(null, -1); }
        @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return true; }
        @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int slot) { return ItemStack.EMPTY; }
    }
}
