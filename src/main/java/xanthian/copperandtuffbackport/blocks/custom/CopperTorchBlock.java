package xanthian.copperandtuffbackport.blocks.custom;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;

public class CopperTorchBlock extends TorchBlock {
    public CopperTorchBlock(Properties settings, ParticleOptions particle) { super(settings, particle); }
    public static class CopperWallTorchBlock extends WallTorchBlock {
        public CopperWallTorchBlock(Properties settings, ParticleOptions particle) { super(settings, particle); }
    }
}
