package xanthian.copperandtuffbackport;

import java.util.HashSet;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;
import xanthian.copperandtuffbackport.util.ModSounds;

public class MixinCooperationTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void lightningAndDoorHooksChainWithAnotherMod(TestContext context) {
        var world = context.getWorld();
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        world.setBlockState(pos, ModCopperDecorations.RODS[2].getDefaultState());
        int powerBefore = CooperationProbe.powerCalls;
        int cleaningBefore = CooperationProbe.cleaningCalls;
        var lightning = EntityType.LIGHTNING_BOLT.create(world);
        lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos.up()));
        lightning.tick();
        context.assertTrue(CooperationProbe.powerCalls == powerBefore + 1 && CooperationProbe.cleaningCalls == cleaningBefore + 1, "Other mod's lightning wrappers run exactly once");
        context.assertTrue(world.getBlockState(pos).isOf(Blocks.LIGHTNING_ROD) && world.getBlockState(pos).get(LightningRodBlock.POWERED), "Lightning still cleans and powers the rod");
        lightning.discard();
        world.removeBlock(pos, false);
        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
        BlockState door = ModCopperBlocks.COPPER_DOOR.getDefaultState().with(DoorBlock.OPEN, true);
        world.setBlockState(pos, door);
        world.setBlockState(pos.up(), door.with(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        var player = context.createMockSurvivalPlayer();
        player.refreshPositionAfterTeleport(Vec3d.ofCenter(pos));
        var doors = new HashSet<GlobalPos>();
        doors.add(GlobalPos.create(world.getRegistryKey(), pos));
        int tagsBefore = CooperationProbe.doorTagReads;
        OpenDoorsTask.pathToDoor(world, player, null, null, doors, Optional.empty());
        context.assertTrue(CooperationProbe.doorTagReads == tagsBefore + 1, "Other mod's door tag modifier still runs");
        context.assertTrue(!world.getBlockState(pos).get(DoorBlock.OPEN) && doors.isEmpty(), "Brain door task closes the copper door");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chestSoundsKeepVanillaBodyAndOtherModHooks(TestContext context) throws Exception {
        var playSound = ChestBlockEntity.class.getDeclaredMethod("playSound", World.class, BlockPos.class, BlockState.class, SoundEvent.class);
        playSound.setAccessible(true);
        BlockPos pos = context.getAbsolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 8; i++) {
            for (ChestType type : ChestType.values()) {
                for (boolean open : new boolean[]{true, false}) {
                    CooperationProbe.soundCalls = 0;
                    var state = ModCopperAgeBlocks.CHESTS[i].getDefaultState().with(ChestBlock.CHEST_TYPE, type);
                    playSound.invoke(null, context.getWorld(), pos, state, open ? SoundEvents.BLOCK_CHEST_OPEN : SoundEvents.BLOCK_CHEST_CLOSE);
                    if (type == ChestType.LEFT) context.assertTrue(CooperationProbe.soundCalls == 0, "Vanilla body suppresses duplicate double-chest sounds");
                    else {
                        int group = Math.max(0, i % 4 - 1);
                        var expected = (open ? ModSounds.COPPER_CHEST_OPEN : ModSounds.COPPER_CHEST_CLOSE)[group];
                        context.assertTrue(CooperationProbe.soundCalls == 1 && CooperationProbe.sound == expected, "Other mod's sound hook observes correct copper sound");
                    }
                }
            }
        }
        playSound.invoke(null, context.getWorld(), pos, ModCopperAgeBlocks.CHESTS[0].getDefaultState(), SoundEvents.BLOCK_BELL_USE);
        context.assertTrue(CooperationProbe.sound == SoundEvents.BLOCK_BELL_USE, "Another mod's custom sound is preserved");
        playSound.invoke(null, context.getWorld(), pos, Blocks.CHEST.getDefaultState(), SoundEvents.BLOCK_CHEST_OPEN);
        context.assertTrue(CooperationProbe.sound == SoundEvents.BLOCK_CHEST_OPEN, "Ordinary chest sounds stay unchanged");
        context.complete();
    }
}
