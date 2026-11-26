package xanthian.copperandtuffbackport.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;

public final class CopperChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Map<Block, CopperChestBlockEntity> entities = new HashMap<>();
    public CopperChestItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    @Override public void renderByItem(ItemStack stack, ItemDisplayContext mode, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        Block block = ((BlockItem)stack.getItem()).getBlock();
        var entity = entities.computeIfAbsent(block, b -> new CopperChestBlockEntity(BlockPos.ZERO,
                b.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH)));
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(entity, poses, buffers, light, overlay);
    }
}
