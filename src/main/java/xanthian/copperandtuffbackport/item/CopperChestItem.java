package xanthian.copperandtuffbackport.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import xanthian.copperandtuffbackport.client.CopperChestItemRenderer;

public final class CopperChestItem extends BlockItem {
    public CopperChestItem(Block block) { super(block, new Properties()); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private CopperChestItemRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new CopperChestItemRenderer();
                return renderer;
            }
        });
    }
}
