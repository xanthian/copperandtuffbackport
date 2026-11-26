package xanthian.copperandtuffbackport.util;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xanthian.copperandtuffbackport.Initialise;
@Mod.EventBusSubscriber(modid = Initialise.MOD_ID)
public final class CopperToolEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void modify(BlockEvent.BlockToolModificationEvent event) {
        if (event.getFinalState() != event.getState() || !event.getContext().getItemInHand().canPerformAction(event.getToolAction())) return;
        if (event.getToolAction() == ToolActions.AXE_SCRAPE) CopperTransitions.previous(event.getState()).ifPresent(event::setFinalState);
        else if (event.getToolAction() == ToolActions.AXE_WAX_OFF) CopperTransitions.unwaxed(event.getState()).ifPresent(event::setFinalState);
    }
}
