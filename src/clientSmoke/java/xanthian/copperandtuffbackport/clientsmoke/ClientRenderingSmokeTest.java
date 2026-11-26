package xanthian.copperandtuffbackport.clientsmoke;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;

import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.joml.Matrix4f;
import java.nio.file.Path;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;

@Mod("copperandtuffbackport_client_smoke")
@Mod.EventBusSubscriber(modid = "copperandtuffbackport_client_smoke", value = Dist.CLIENT)
public class ClientRenderingSmokeTest {
    private static boolean finished;
    @SubscribeEvent public static void render(TickEvent.RenderTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            var client = Minecraft.getInstance();
            if (finished || client.getOverlay() != null || client.screen == null) return;
            finished = true;
            client.getMainRenderTarget().bindWrite(true);
            RenderSystem.clearColor(0.15F, 0.19F, 0.24F, 1);
            RenderSystem.clear(16640, false);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, 480, 450, 0, 1000, 21000), VertexSorting.ORTHOGRAPHIC_Z);
            var modelView = RenderSystem.getModelViewStack();
            modelView.pushPose();
            modelView.setIdentity();
            modelView.translate(0, 0, -11000);
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            var draw = new GuiGraphics(client, client.renderBuffers().bufferSource());
            draw.fill(0, 0, 480, 450, 0xff26313d);
            for (int i = 0; i < 8; i++) {
                draw.pose().pushPose();
                draw.pose().scale(2, 2, 2);
                draw.renderItem(new ItemStack(ModCopperAgeBlocks.BARS[i]), 12 + i * 27, 12);
                draw.renderItem(new ItemStack(ModCopperAgeBlocks.CHESTS[i]), 12 + i * 27, 40);
                draw.renderItem(new ItemStack(ModCopperDecorations.LANTERNS[i]), 12 + i * 27, 135);
                draw.renderItem(new ItemStack(ModCopperDecorations.CHAINS[i]), 12 + i * 27, 160);
                draw.renderItem(new ItemStack(ModCopperDecorations.RODS[i]), 12 + i * 27, 185);
                if (i == 0) {
                    draw.renderItem(new ItemStack(ModCopperDecorations.NUGGET), 12, 210);
                    draw.renderItem(new ItemStack(ModCopperDecorations.TORCH), 39, 210);
                }
                draw.pose().popPose();
                for (int half = 0; half < 2; half++) {
                    ChestType type = half == 0 ? ChestType.LEFT : ChestType.RIGHT;
                    var entity = new CopperChestBlockEntity(BlockPos.ZERO, ModCopperAgeBlocks.CHESTS[i].defaultBlockState()
                            .setValue(ChestBlock.FACING, Direction.SOUTH).setValue(ChestBlock.TYPE, type));
                    var matrices = draw.pose();
                    matrices.pushPose();
                    matrices.translate(24 + i * 54, 160 + half * 60, 100);
                    matrices.scale(28, -28, 28);
                    matrices.mulPose(Axis.XP.rotationDegrees(30));
                    matrices.mulPose(Axis.YP.rotationDegrees(45));
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    client.getBlockEntityRenderDispatcher().renderItem(entity, matrices, client.renderBuffers().bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
                    draw.flush();
                    matrices.popPose();
                }
            }
            draw.flush();
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            try (var screenshot = Screenshot.takeScreenshot(client.getMainRenderTarget())) {
                screenshot.writeToFile(Path.of("../forge-copper-age-render-preview.png"));
            } catch (Exception e) { throw new IllegalStateException("Unable to save rendering smoke test", e); }
            System.out.println("COPPER_AGE_CLIENT_SMOKE_PASSED");
            try { java.nio.file.Files.writeString(Path.of("smoke-success.flag"), "passed"); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
            client.stop();
    }
}
