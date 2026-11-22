package xanthian.copperandtuffbackport;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import java.nio.file.Path;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;

public class ClientRenderingSmokeTest implements ClientModInitializer {
    private boolean finished;
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (finished || client.getOverlay() != null || client.currentScreen == null) return;
            finished = true;
            client.getFramebuffer().beginWrite(true);
            RenderSystem.clearColor(0.15F, 0.19F, 0.24F, 1);
            RenderSystem.clear(16640, false);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, 480, 450, 0, 1000, 21000), VertexSorter.BY_Z);
            var modelView = RenderSystem.getModelViewStack();
            modelView.push();
            modelView.loadIdentity();
            modelView.translate(0, 0, -11000);
            RenderSystem.applyModelViewMatrix();
            DiffuseLighting.enableGuiDepthLighting();
            var draw = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
            draw.fill(0, 0, 480, 450, 0xff26313d);
            for (int i = 0; i < 8; i++) {
                draw.getMatrices().push();
                draw.getMatrices().scale(2, 2, 2);
                draw.drawItem(new ItemStack(ModCopperAgeBlocks.BARS[i]), 12 + i * 27, 12);
                draw.drawItem(new ItemStack(ModCopperAgeBlocks.CHESTS[i]), 12 + i * 27, 40);
                draw.drawItem(new ItemStack(ModCopperDecorations.LANTERNS[i]), 12 + i * 27, 135);
                draw.drawItem(new ItemStack(ModCopperDecorations.CHAINS[i]), 12 + i * 27, 160);
                draw.drawItem(new ItemStack(ModCopperDecorations.RODS[i]), 12 + i * 27, 185);
                if (i == 0) {
                    draw.drawItem(new ItemStack(ModCopperDecorations.NUGGET), 12, 210);
                    draw.drawItem(new ItemStack(ModCopperDecorations.TORCH), 39, 210);
                }
                draw.getMatrices().pop();
                for (int half = 0; half < 2; half++) {
                    ChestType type = half == 0 ? ChestType.LEFT : ChestType.RIGHT;
                    var entity = new CopperChestBlockEntity(BlockPos.ORIGIN, ModCopperAgeBlocks.CHESTS[i].getDefaultState()
                            .with(ChestBlock.FACING, Direction.SOUTH).with(ChestBlock.CHEST_TYPE, type));
                    var matrices = draw.getMatrices();
                    matrices.push();
                    matrices.translate(24 + i * 54, 160 + half * 60, 100);
                    matrices.scale(28, -28, 28);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    client.getBlockEntityRenderDispatcher().renderEntity(entity, matrices, client.getBufferBuilders().getEntityVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV);
                    draw.draw();
                    matrices.pop();
                }
            }
            draw.draw();
            modelView.pop();
            RenderSystem.applyModelViewMatrix();
            try (var screenshot = ScreenshotRecorder.takeScreenshot(client.getFramebuffer())) {
                screenshot.writeTo(Path.of("../copper-age-render-preview.png"));
            } catch (Exception e) { throw new IllegalStateException("Unable to save rendering smoke test", e); }
            System.out.println("COPPER_AGE_CLIENT_SMOKE_PASSED");
            client.scheduleStop();
        });
    }
}
