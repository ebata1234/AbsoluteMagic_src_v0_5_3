package jp.absolute.magic.registry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.xkmc.l2hostility.content.config.TraitConfig;
import dev.xkmc.l2hostility.init.entries.LHRegistrate;
import jp.absolute.magic.trait.MagicianTrait;
import net.minecraft.ChatFormatting;

public final class ModTraits {
    public static final LHRegistrate REGISTRATE = new LHRegistrate("absolutemagic");

    // IMPORTANT: L2Hostility's constructor is TraitConfig(id, COST, WEIGHT, MAX_RANK, MIN_LEVEL)  (checked in 2.5.19).
    // The values below must match data/absolutemagic/l2hostility_config/trait/magician.json:
    //   cost 45, weight 50, max_rank 5, min_level 75
    public static final RegistryEntry<MagicianTrait> MAGICIAN = REGISTRATE
            .regTrait("magician", () -> new MagicianTrait(ChatFormatting.LIGHT_PURPLE), rl -> new TraitConfig(rl, 45, 50, 5, 75))
            .lang("Magician")
            .register();

    public static void init() {
    }


    private ModTraits() {
    }
}
