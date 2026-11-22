package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.particle.ParticleEffect;

public class CopperTorchBlock extends TorchBlock {
    public CopperTorchBlock(Settings settings, ParticleEffect particle) { super(settings, particle); }
    public static class CopperWallTorchBlock extends WallTorchBlock {
        public CopperWallTorchBlock(Settings settings, ParticleEffect particle) { super(settings, particle); }
    }
}
