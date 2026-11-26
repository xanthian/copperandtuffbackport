package xanthian.copperandtuffbackport.gametest;
import xanthian.copperandtuffbackport.Initialise;

import net.minecraft.sounds.SoundEvent;

/** Observations from test-only mixins simulating another mod at the same injection points. */
public final class CooperationProbe {
    public static int powerCalls;
    public static int cleaningCalls;
    public static int doorTagReads;
    public static int soundCalls;
    public static SoundEvent sound;
}
