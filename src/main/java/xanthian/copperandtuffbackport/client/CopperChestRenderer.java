package xanthian.copperandtuffbackport.client;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.custom.CopperChestBlock;

public class CopperChestRenderer implements BlockEntityRenderer<CopperChestBlockEntity> {
    private final ModelPart single;
    private final ModelPart left;
    private final ModelPart right;
    private static final String[] TEXTURES = {"copper", "copper_exposed", "copper_weathered", "copper_oxidized"};

    public CopperChestRenderer(BlockEntityRendererFactory.Context context) {
        single = context.getLayerModelPart(EntityModelLayers.CHEST);
        left = context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT);
        right = context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT);
    }

    @Override
    public void render(CopperChestBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider consumers, int light, int overlay) {
        var state = entity.getCachedState();
        if (!(state.getBlock() instanceof CopperChestBlock chest)) return;
        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        float open = entity.getAnimationProgress(tickDelta);
        if (entity.hasWorld()) {
            var source = chest.getBlockEntitySource(state, entity.getWorld(), entity.getPos(), true);
            open = source.apply(ChestBlock.getAnimationProgressRetriever(entity)).get(tickDelta);
            light = source.apply(new LightmapCoordinatesRetriever<>()).applyAsInt(light);
        }
        float eased = 1.0F - (float)Math.pow(1.0F - open, 3);
        ModelPart model = type == ChestType.LEFT ? left : type == ChestType.RIGHT ? right : single;
        model.getChild("lid").pitch = -(eased * (float)Math.PI / 2);
        model.getChild("lock").pitch = model.getChild("lid").pitch;
        String suffix = type == ChestType.LEFT ? "_left" : type == ChestType.RIGHT ? "_right" : "";
        Identifier texture = new Identifier(Initialise.MOD_ID, "textures/entity/chest/" + TEXTURES[chest.getOxidationLevel().ordinal()] + suffix + ".png");
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.get(ChestBlock.FACING).asRotation()));
        matrices.translate(-0.5, -0.5, -0.5);
        var vertices = consumers.getBuffer(RenderLayer.getEntityCutout(texture));
        model.getChild("lid").render(matrices, vertices, light, overlay);
        model.getChild("lock").render(matrices, vertices, light, overlay);
        model.getChild("bottom").render(matrices, vertices, light, overlay);
        matrices.pop();
    }
}
