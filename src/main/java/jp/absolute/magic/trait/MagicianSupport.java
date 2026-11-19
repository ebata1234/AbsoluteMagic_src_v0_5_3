package jp.absolute.magic.trait;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import jp.absolute.magic.config.MagicianConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;

/**
 * 0.5.3: while fighting, a Magician supports nearby monsters. Two independent timers:
 *  - HEAL (cooldown 45 s): the most wounded allies are healed by (healPercentPerRank x rank) of their max HP,
 *    never more than maxHealPerCast (200) HP in one heal.
 *  - BUFF (every 12 s, lasts 10 s): rank 2 Speed, rank 3 Strength, rank 4 Resistance, rank 5 Regeneration (all level I).
 * Instant, no wind-up, never interrupts the attack spells. Only vanilla effects are used.
 */
final class MagicianSupport {
    private static final class Timers {
        int heal;
        int buff;
    }

    private static final Map<Mob, Timers> TIMERS = new WeakHashMap<>();

    static void tick(Mob mob, int rank) {
        if (!MagicianConfig.SUPPORT_ENABLED.get() || !mob.isAlive()) {
            return;
        }
        Timers t = TIMERS.computeIfAbsent(mob, m -> initial(m));
        if (t.heal > 0) t.heal--;
        if (t.buff > 0) t.buff--;
        if (t.heal > 0 && t.buff > 0) {
            return;
        }
        // only support during a fight
        if (mob.getTarget() == null || !mob.getTarget().isAlive() || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        double r = MagicianConfig.SUPPORT_RADIUS.get();
        List<Mob> allies = level.getEntitiesOfClass(Mob.class, new AABB(mob.blockPosition()).inflate(r), other -> isAlly(mob, other));
        if (allies.isEmpty()) {
            if (t.heal <= 0) t.heal = 40;  // look again in 2 s
            if (t.buff <= 0) t.buff = 40;
            return;
        }
        // most wounded first
        allies.sort(Comparator.comparingDouble(a -> a.getHealth() / Math.max(1.0F, a.getMaxHealth())));
        int maxTargets = MagicianConfig.SUPPORT_MAX_TARGETS_BASE.get() + rank;
        boolean acted = false;

        if (t.heal <= 0) {
            double fraction = MagicianConfig.SUPPORT_HEAL_PERCENT_PER_RANK.get() * rank;
            float cap = MagicianConfig.SUPPORT_MAX_HEAL_PER_CAST.get().floatValue();
            int healed = 0;
            for (Mob ally : allies) {
                if (healed >= maxTargets) break;
                if (ally.getHealth() >= ally.getMaxHealth()) continue;
                float amount = Math.min(cap, (float) (ally.getMaxHealth() * fraction));
                if (amount <= 0.0F) break;
                ally.heal(amount);
                level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(0.8D), ally.getZ(), 4, 0.3D, 0.4D, 0.3D, 0.0D);
                healed++;
            }
            // start the cooldown only if somebody was actually healed; otherwise look again in 2 s
            t.heal = healed > 0 ? MagicianConfig.SUPPORT_HEAL_COOLDOWN_SECONDS.get() * 20 : 40;
            acted |= healed > 0;
        }

        if (t.buff <= 0) {
            int buffTicks = MagicianConfig.SUPPORT_BUFF_SECONDS.get() * 20;
            int count = Math.min(maxTargets, allies.size());
            for (int i = 0; i < count; i++) {
                Mob ally = allies.get(i);
                if (rank >= 2) ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, buffTicks, 0, false, true));
                if (rank >= 3) ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, buffTicks, 0, false, true));
                if (rank >= 4) ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, buffTicks, 0, false, true));
                if (rank >= 5) ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, buffTicks, 0, false, true));
                if (rank >= 2) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY(0.5D), ally.getZ(), 8, 0.4D, 0.5D, 0.4D, 0.02D);
                }
            }
            t.buff = MagicianConfig.SUPPORT_BUFF_INTERVAL_SECONDS.get() * 20;
            acted |= rank >= 2;
        }

        if (acted) {
            mob.swing(InteractionHand.OFF_HAND);
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.2F);
        }
    }

    private static boolean isAlly(Mob self, Mob other) {
        if (other == self || !other.isAlive() || !(other instanceof Enemy)) {
            return false;
        }
        return MagicianConfig.SUPPORT_AFFECT_BOSSES.get() || !other.getType().is(Tags.EntityTypes.BOSSES);
    }

    /** stagger the first supports so a group of magicians does not all fire at once */
    private static Timers initial(Mob mob) {
        Timers t = new Timers();
        t.buff = 60 + mob.getRandom().nextInt(100);
        t.heal = 100 + mob.getRandom().nextInt(200);
        return t;
    }

    private MagicianSupport() {
    }
}
