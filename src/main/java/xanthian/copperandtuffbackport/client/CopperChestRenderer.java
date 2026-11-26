package xanthian.copperandtuffbackport.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.custom.CopperChestBlock;

public class CopperChestRenderer implements BlockEntityRenderer<CopperChestBlockEntity> {
    private final ModelPart single;
    private final ModelPart left;
    private final ModelPart right;
    private static final String[] TEXTURES = {"copper", "copper_exposed", "copper_weathered", "copper_oxidized"};

    public CopperChestRenderer(BlockEntityRendererProvider.Context context) {
        single = context.bakeLayer(ModelLayers.CHEST);
        left = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        right = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
    }

    @Override
    public void render(CopperChestBlockEntity entity, float tickDelta, PoseStack matrices,
                       MultiBufferSource consumers, int light, int overlay) {
        var state = entity.getBlockState();
        if (!(state.getBlock() instanceof CopperChestBlock chest)) return;
        ChestType type = state.getValue(ChestBlock.TYPE);
        float open = entity.getOpenNess(tickDelta);
        if (entity.hasLevel()) {
            var source = chest.combine(state, entity.getLevel(), entity.getBlockPos(), true);
            open = source.apply(ChestBlock.opennessCombiner(entity)).get(tickDelta);
            light = source.apply(new BrightnessCombiner<>()).applyAsInt(light);
        }
        float eased = 1.0F - (float)Math.pow(1.0F - open, 3);
        ModelPart model = type == ChestType.LEFT ? left : type == ChestType.RIGHT ? right : single;
        model.getChild("lid").xRot = -(eased * (float)Math.PI / 2);
        model.getChild("lock").xRot = model.getChild("lid").xRot;
        String suffix = type == ChestType.LEFT ? "_left" : type == ChestType.RIGHT ? "_right" : "";
        ResourceLocation texture = ResourceLocation.tryBuild(Initialise.MOD_ID, "textures/entity/chest/" + TEXTURES[chest.getOxidationLevel().ordinal()] + suffix + ".png");
        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(-state.getValue(ChestBlock.FACING).toYRot()));
        matrices.translate(-0.5, -0.5, -0.5);
        var vertices = consumers.getBuffer(RenderType.entityCutout(texture));
        model.getChild("lid").render(matrices, vertices, light, overlay);
        model.getChild("lock").render(matrices, vertices, light, overlay);
        model.getChild("bottom").render(matrices, vertices, light, overlay);
        matrices.popPose();
    }
}
