package xanthian.copperandtuffbackport;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public class ItemGroupTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void creativeMembershipAndReleaseOrdering(TestContext context) throws Exception {
        JsonObject reference;
        // Official 1.21.1 layout, with additions inserted using 1.21.9 ItemGroups.
        // Retain 1.21.1's iron-before-copper ingot order rather than adopting 1.21.9's reorder.
        try (var reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/expected-item-groups.json")), StandardCharsets.UTF_8)) {
            reference = JsonParser.parseReader(reader).getAsJsonObject();
        }
        var world = context.getWorld();
        ItemGroups.updateDisplayContext(world.getEnabledFeatures(), false, world.getRegistryManager());
        Set<String> visibleModItems = new HashSet<>();
        for (var entry : reference.entrySet()) {
            List<String> expected = new ArrayList<>();
            entry.getValue().getAsJsonArray().forEach(value -> expected.add(value.getAsString()));
            Set<String> watched = new HashSet<>(expected);
            // Redstone uses the waxed rod in 1.21.9, replacing the older unwaxed entry.
            if (entry.getKey().equals("redstone_blocks")) watched.add("minecraft:lightning_rod");
            var group = Registries.ITEM_GROUP.get(new Identifier(entry.getKey()));
            List<String> actual = new ArrayList<>();
            Set<String> unique = new HashSet<>();
            for (var stack : group.getDisplayStacks()) {
                String id = Registries.ITEM.getId(stack.getItem()).toString();
                if (id.startsWith(Initialise.MOD_ID + ":")) {
                    context.assertTrue(unique.add(id), "No duplicate creative entries: " + id);
                    visibleModItems.add(id);
                }
                if (watched.contains(id) || id.startsWith(Initialise.MOD_ID + ":")) actual.add(id);
            }
            context.assertTrue(actual.equals(expected), "Release ordering and membership for " + entry.getKey() + ": expected=" + expected + ", actual=" + actual);
            Set<String> search = new HashSet<>();
            group.getSearchTabStacks().forEach(stack -> search.add(Registries.ITEM.getId(stack.getItem()).toString()));
            for (String id : expected) context.assertTrue(search.contains(id), "Creative search includes " + id);
        }
        Set<String> registered = new HashSet<>();
        Registries.ITEM.getIds().stream().filter(id -> id.getNamespace().equals(Initialise.MOD_ID)).forEach(id -> registered.add(id.toString()));
        context.assertTrue(visibleModItems.equals(registered), "Every registered mod item is visible in its release tab");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void registryIdsRemainStable(TestContext context) throws Exception {
        Set<String> expectedBlocks = new TreeSet<>();
        try (var reader = new java.io.BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/expected-block-ids.txt")), StandardCharsets.UTF_8))) {
            reader.lines().filter(line -> !line.isBlank()).forEach(expectedBlocks::add);
        }
        Set<String> actualBlocks = new TreeSet<>();
        Registries.BLOCK.getIds().stream().filter(id -> id.getNamespace().equals(Initialise.MOD_ID)).forEach(id -> actualBlocks.add(id.getPath()));
        context.assertTrue(actualBlocks.equals(expectedBlocks), "All existing block IDs are preserved");
        Set<String> expectedItems = new TreeSet<>(expectedBlocks);
        expectedItems.remove("copper_wall_torch");
        expectedItems.add("copper_nugget");
        Set<String> actualItems = new TreeSet<>();
        Registries.ITEM.getIds().stream().filter(id -> id.getNamespace().equals(Initialise.MOD_ID)).forEach(id -> actualItems.add(id.getPath()));
        context.assertTrue(actualItems.equals(expectedItems), "All existing item IDs are preserved");
        context.complete();
    }
}
