package jp.absolute.magic.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

/** config/absolutemagic-magician.toml. 0.5.3: stronger power defaults + [support] section. */
public final class MagicianConfig {
    public static final ForgeConfigSpec SPEC;
    public static final int MAX_TRAIT_LEVEL = 5;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue INSTANT_WINDUP_TICKS;
    public static final ForgeConfigSpec.DoubleValue MAX_RANGE;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_LINE_OF_SIGHT;

    public static final ForgeConfigSpec.IntValue SPELL_LEVEL_OFFSET;
    public static final ForgeConfigSpec.IntValue MAX_SPELL_LEVEL;
    public static final ForgeConfigSpec.DoubleValue POWER_PER_TRAIT_LEVEL;
    public static final ForgeConfigSpec.DoubleValue POWER_PER_MOB_LEVEL;
    public static final ForgeConfigSpec.DoubleValue MAX_POWER_BONUS;

    public static final ForgeConfigSpec.BooleanValue SUPPORT_ENABLED;
    public static final ForgeConfigSpec.IntValue SUPPORT_BUFF_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue SUPPORT_HEAL_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue SUPPORT_MAX_HEAL_PER_CAST;
    public static final ForgeConfigSpec.DoubleValue SUPPORT_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SUPPORT_HEAL_PERCENT_PER_RANK;
    public static final ForgeConfigSpec.IntValue SUPPORT_BUFF_SECONDS;
    public static final ForgeConfigSpec.IntValue SUPPORT_MAX_TARGETS_BASE;
    public static final ForgeConfigSpec.BooleanValue SUPPORT_AFFECT_BOSSES;

    public static final List<ForgeConfigSpec.ConfigValue<List<? extends String>>> LEVEL_SPELLS;

    static {
        LEVEL_SPELLS = new ArrayList<>();
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("magician");
        ENABLED = b.comment("Master switch of the Magician trait behaviour.")
                .define("enabled", true);
        COOLDOWN_SECONDS = b.comment("Seconds a mob waits AFTER finishing a spell before it casts again (minimum 6).",
                        "Mobs have no mana, so this cooldown is the only limit.")
                .defineInRange("cooldownSeconds", 8, 6, 3600);
        INSTANT_WINDUP_TICKS = b.comment("Standing-still wind-up (ticks, 20 = 1s) before an INSTANT spell.",
                        "LONG spells use their own cast time. The mob swings its arm during the wind-up.")
                .defineInRange("instantWindupTicks", 20, 0, 200);
        MAX_RANGE = b.comment("The mob only casts when its target is within this many blocks.")
                .defineInRange("maxRange", 24.0D, 4.0D, 128.0D);
        REQUIRE_LINE_OF_SIGHT = b.comment("Only start casting when the mob can see its target.")
                .define("requireLineOfSight", true);
        b.pop();

        b.push("power");
        SPELL_LEVEL_OFFSET = b.comment("Spell level = trait level + this offset (then limited by maxSpellLevel and the spell's own max).",
                        "A list entry like \"irons_spellbooks:fireball:3\" fixes the spell level to 3.")
                .defineInRange("spellLevelOffset", 0, -10, 20);
        MAX_SPELL_LEVEL = b.defineInRange("maxSpellLevel", 10, 1, 50);
        POWER_PER_TRAIT_LEVEL = b.comment("Bonus added to the mob's irons_spellbooks:spell_power attribute per trait level.")
                .defineInRange("spellPowerPerTraitLevel", 0.25D, 0.0D, 100.0D);
        POWER_PER_MOB_LEVEL = b.comment("Bonus added to spell_power per L2Hostility mob level.",
                        "Normal difficulty: a Lv50 rank-1 magician's Firebolt/Icicle hits about 16 on a full diamond set.")
                .defineInRange("spellPowerPerMobLevel", 0.08D, 0.0D, 100.0D);
        MAX_POWER_BONUS = b.comment("Upper limit of the total spell_power bonus from the two values above.")
                .defineInRange("maxSpellPowerBonus", 30.0D, 0.0D, 1000.0D);
        b.pop();

        b.push("support");
        SUPPORT_ENABLED = b.comment("While fighting, the magician heals and buffs nearby monsters.")
                .define("enabled", true);
        SUPPORT_BUFF_INTERVAL_SECONDS = b.comment("Seconds between two BUFF casts of one magician.")
                .defineInRange("buffIntervalSeconds", 12, 2, 600);
        SUPPORT_HEAL_COOLDOWN_SECONDS = b.comment("Heal cooldown of one magician in seconds (independent of the buff interval).")
                .defineInRange("healCooldownSeconds", 45, 2, 3600);
        SUPPORT_MAX_HEAL_PER_CAST = b.comment("Upper limit of the HP restored to one ally by one heal. Nothing above this is ever healed.")
                .defineInRange("maxHealPerCast", 200.0D, 1.0D, 1.0E9D);
        SUPPORT_RADIUS = b.comment("Allies within this many blocks are supported.")
                .defineInRange("radius", 12.0D, 2.0D, 64.0D);
        SUPPORT_HEAL_PERCENT_PER_RANK = b.comment("Heal per cast = this ratio x trait rank x the ally's max HP (then limited by maxHealPerCast). 0.03 = 3% per rank (rank 5 = 15%).")
                .defineInRange("healPercentPerRank", 0.03D, 0.0D, 1.0D);
        SUPPORT_BUFF_SECONDS = b.comment("Buff duration. Rank 2+: Speed, rank 3+: Strength, rank 4+: Resistance, rank 5: Regeneration (all level I).")
                .defineInRange("buffSeconds", 10, 1, 120);
        SUPPORT_MAX_TARGETS_BASE = b.comment("Allies supported per cast = this + trait rank (most wounded first).")
                .defineInRange("maxTargetsBase", 1, 0, 20);
        SUPPORT_AFFECT_BOSSES = b.comment("false: monsters in the forge:bosses tag are never healed or buffed.")
                .define("affectBosses", false);
        b.pop();

        b.push("spells");
        b.comment("Spell lists per trait level. A mob with trait level N picks randomly from the lists of level 1..N (cumulative).",
                "Entry format: \"namespace:spell_id\" or \"namespace:spell_id:spellLevel\".",
                "Only INSTANT and LONG cast-type spells work. Continuous spells are skipped.");
        String[][] defaults = {
                {"irons_spellbooks:firebolt", "irons_spellbooks:icicle"},
                {"irons_spellbooks:lightning_lance", "irons_spellbooks:poison_arrow", "irons_spellbooks:blood_slash"},
                {"irons_spellbooks:fireball", "irons_spellbooks:wither_skull", "irons_spellbooks:chain_lightning"},
                {"irons_spellbooks:magma_bomb", "irons_spellbooks:lightning_bolt", "irons_spellbooks:ball_lightning"},
                {"irons_spellbooks:starfall", "irons_spellbooks:thunderstorm"}
        };
        for (int i = 0; i < 5; i++) {
            LEVEL_SPELLS.add(b.defineList("level" + (i + 1), List.of(defaults[i]), o -> o instanceof String));
        }
        b.pop();

        SPEC = b.build();
    }

    private MagicianConfig() {
    }
}
