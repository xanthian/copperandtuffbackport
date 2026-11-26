package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;

import java.util.HashSet;
import java.util.Optional;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;
import xanthian.copperandtuffbackport.util.ModSounds;

@net.minecraftforge.gametest.GameTestHolder(Initialise.MOD_ID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class MixinCooperationTests {
    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void lightningAndDoorHooksChainWithAnotherMod(GameTestHelper context) {
        var world = context.getLevel();
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        world.setBlockAndUpdate(pos, ModCopperDecorations.RODS[2].defaultBlockState());
        int powerBefore = CooperationProbe.powerCalls;
        int cleaningBefore = CooperationProbe.cleaningCalls;
        var lightning = EntityType.LIGHTNING_BOLT.create(world);
        lightning.moveTo(Vec3.atBottomCenterOf(pos.above()));
        lightning.tick();
        context.assertTrue(CooperationProbe.powerCalls == powerBefore + 1 && CooperationProbe.cleaningCalls == cleaningBefore + 1, "Other mod's lightning wrappers run exactly once");
        context.assertTrue(world.getBlockState(pos).is(Blocks.LIGHTNING_ROD) && world.getBlockState(pos).getValue(LightningRodBlock.POWERED), "Lightning still cleans and powers the rod");
        lightning.discard();
        world.removeBlock(pos, false);
        world.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        BlockState door = ModCopperBlocks.COPPER_DOOR.defaultBlockState().setValue(DoorBlock.OPEN, true);
        world.setBlockAndUpdate(pos, door);
        world.setBlockAndUpdate(pos.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        var player = context.makeMockSurvivalPlayer();
        player.moveTo(Vec3.atCenterOf(pos));
        var doors = new HashSet<GlobalPos>();
        doors.add(GlobalPos.of(world.dimension(), pos));
        int tagsBefore = CooperationProbe.doorTagReads;
        InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough(world, player, null, null, doors, Optional.empty());
        context.assertTrue(CooperationProbe.doorTagReads == tagsBefore + 1, "Other mod's door tag modifier still runs");
        context.assertTrue(!world.getBlockState(pos).getValue(DoorBlock.OPEN) && doors.isEmpty(), "Brain door task closes the copper door");
        context.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "copperandtuffbackport_test")
    public void chestSoundsKeepVanillaBodyAndOtherModHooks(GameTestHelper context) throws Exception {
        var playSound = ChestBlockEntity.class.getDeclaredMethod("playSound", Level.class, BlockPos.class, BlockState.class, SoundEvent.class);
        playSound.setAccessible(true);
        BlockPos pos = context.absolutePos(new BlockPos(3, 3, 3));
        for (int i = 0; i < 8; i++) {
            for (ChestType type : ChestType.values()) {
                for (boolean open : new boolean[]{true, false}) {
                    CooperationProbe.soundCalls = 0;
                    var state = ModCopperAgeBlocks.CHESTS[i].defaultBlockState().setValue(ChestBlock.TYPE, type);
                    playSound.invoke(null, context.getLevel(), pos, state, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE);
                    if (type == ChestType.LEFT) context.assertTrue(CooperationProbe.soundCalls == 0, "Vanilla body suppresses duplicate double-chest sounds");
                    else {
                        int group = Math.max(0, i % 4 - 1);
                        var expected = (open ? ModSounds.COPPER_CHEST_OPEN : ModSounds.COPPER_CHEST_CLOSE)[group];
                        context.assertTrue(CooperationProbe.soundCalls == 1 && CooperationProbe.sound == expected, "Other mod's sound hook observes correct copper sound");
                    }
                }
            }
        }
        playSound.invoke(null, context.getLevel(), pos, ModCopperAgeBlocks.CHESTS[0].defaultBlockState(), SoundEvents.BELL_BLOCK);
        context.assertTrue(CooperationProbe.sound == SoundEvents.BELL_BLOCK, "Another mod's custom sound is preserved");
        playSound.invoke(null, context.getLevel(), pos, Blocks.CHEST.defaultBlockState(), SoundEvents.CHEST_OPEN);
        context.assertTrue(CooperationProbe.sound == SoundEvents.CHEST_OPEN, "Ordinary chest sounds stay unchanged");
        context.succeed();
    }
}
