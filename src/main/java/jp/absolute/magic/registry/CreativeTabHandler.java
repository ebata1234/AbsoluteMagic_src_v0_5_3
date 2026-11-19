package jp.absolute.magic.registry;

import dev.xkmc.l2hostility.init.registrate.LHBlocks;
import jp.absolute.magic.AbsoluteMagic;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Puts the "Magician" trait item into the L2Hostility creative tab (next to the other trait items). */
@Mod.EventBusSubscriber(modid = AbsoluteMagic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CreativeTabHandler {

    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(LHBlocks.TAB.getKey())) {
            // MobTrait implements ItemLike; asItem() resolves to the item "absolutemagic:magician"
            // that LHRegistrate.regTrait registers automatically (a TraitSymbol).
            event.accept(ModTraits.MAGICIAN.get());
        }
    }

    private CreativeTabHandler() {}
}
