package xanthian.copperandtuffbackport.blocks;

import static xanthian.copperandtuffbackport.util.BlockRegistration.registerBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import xanthian.copperandtuffbackport.util.ModSounds;

import static net.minecraft.world.level.block.Blocks.TUFF;

public class ModTuffBlocks {

    public static final Block TUFF_SLAB = new SlabBlock(BlockBehaviour.Properties.copy(TUFF));
    public static final Block TUFF_STAIRS = new StairBlock(TUFF.defaultBlockState(), BlockBehaviour.Properties.copy(TUFF));
    public static final Block TUFF_WALL = new WallBlock(BlockBehaviour.Properties.copy(TUFF).forceSolidOn());
    public static final Block POLISHED_TUFF = new Block(BlockBehaviour.Properties.copy(TUFF).sound(ModSounds.POLISHED_TUFF));
    public static final Block POLISHED_TUFF_SLAB = new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_TUFF));
    public static final Block POLISHED_TUFF_STAIRS = new StairBlock(POLISHED_TUFF.defaultBlockState(), BlockBehaviour.Properties.copy(POLISHED_TUFF));
    public static final Block POLISHED_TUFF_WALL = new WallBlock(BlockBehaviour.Properties.copy(POLISHED_TUFF).forceSolidOn());
    public static final Block CHISELED_TUFF = new Block(BlockBehaviour.Properties.copy(TUFF));
    public static final Block TUFF_BRICKS = new Block(BlockBehaviour.Properties.copy(TUFF).sound(ModSounds.TUFF_BRICKS));
    public static final Block TUFF_BRICK_SLAB = new SlabBlock(BlockBehaviour.Properties.copy(TUFF_BRICKS));
    public static final Block TUFF_BRICK_STAIRS = new StairBlock(TUFF_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(TUFF_BRICKS));
    public static final Block TUFF_BRICK_WALL = new WallBlock(BlockBehaviour.Properties.copy(TUFF_BRICKS).forceSolidOn());
    public static final Block CHISELED_TUFF_BRICKS = new Block(BlockBehaviour.Properties.copy(TUFF_BRICKS));

    public static void registerBlocks() {
        registerBlock("tuff_slab", TUFF_SLAB);
        registerBlock("tuff_stairs", TUFF_STAIRS);
        registerBlock("tuff_wall", TUFF_WALL);
        registerBlock("polished_tuff", POLISHED_TUFF);
        registerBlock("polished_tuff_slab", POLISHED_TUFF_SLAB);
        registerBlock("polished_tuff_stairs", POLISHED_TUFF_STAIRS);
        registerBlock("polished_tuff_wall", POLISHED_TUFF_WALL);
        registerBlock("chiseled_tuff", CHISELED_TUFF);
        registerBlock("tuff_bricks", TUFF_BRICKS);
        registerBlock("tuff_brick_slab", TUFF_BRICK_SLAB);
        registerBlock("tuff_brick_stairs", TUFF_BRICK_STAIRS);
        registerBlock("tuff_brick_wall", TUFF_BRICK_WALL);
        registerBlock("chiseled_tuff_bricks", CHISELED_TUFF_BRICKS);
    }

}
