package com.arcana.mod.content.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import com.arcana.mod.registry.ModItems;
import com.arcana.mod.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class TheFoolEntity extends Monster {

    private static final float  MAX_DAMAGE_PER_HIT   = 30.0f;
    private static final float  RECOIL_SPEED         = 0.5f;
    private static final int    STUN_DURATION        = 100;
    private static final int    SHIELD_DISABLE_TICKS = 300;
    private static final double DASH_MAX_RANGE       = 24.0;
    private static final int    DEATH_EXTEND_TICKS   = 100;

    private static final int   RAGE_HIT_THRESHOLD  = 4;
    private static final int   RAGE_WINDOW_TICKS   = 60;
    private static final int   SHOCKWAVE_COOLDOWN  = 120;
    private static final float COUNTER_CHANCE      = 0.35f;
    private static final int    PUSH_DURATION     = 12;
    private static final int    PUSH_HIT_TICK     = 6;
    private static final int    PUSH_COOLDOWN_MAX = 300;
    private static final double PUSH_RANGE        = 5.0;
    private static final int    DEATH_SPEAK_TICK  = 50;

    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_TICK =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACK_RIGHT =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_STUNNED =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_WINDING_UP =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PUSHING =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState floatingAnimationState    = new AnimationState();
    public final AnimationState rightAttackAnimationState = new AnimationState();
    public final AnimationState leftAttackAnimationState  = new AnimationState();
    public final AnimationState stunnedAnimationState     = new AnimationState();
    public final AnimationState chargingAnimationState    = new AnimationState();
    public final AnimationState windupAnimationState      = new AnimationState();
    public final AnimationState pushAnimationState        = new AnimationState();
    public final AnimationState deathAnimationState       = new AnimationState();

    private final ServerBossEvent bossBar = new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private static final int INTRO_DURATION    = 120;
    private static final int INTRO_LINE_1_TICK = 1;
    private static final int INTRO_LINE_2_TICK = 60;

    private static final double MAX_DASH_ANGLE_TICK = 0.03;

    private int     introTicks       = 0;
    private boolean introCompleted   = false;
    private int     stunTicks        = 0;
    private UUID   dashingTowardUuid = null;
    private Vec3   dashDirection    = null;
    private double dashSpeed        = 1.2;
    private boolean waitingForDeath  = false;
    private int    deathHoldTicks    = 0;
    private int    recentHitCount    = 0;
    private int    hitWindowTimer    = 0;
    private int    shockwaveCooldown = 0;
    private int    pushTicks         = 0;
    private int    pushCooldown      = 0;
    private UUID   pushTargetUuid    = null;

    public TheFoolEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 700.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.ATTACK_DAMAGE, 17.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TheFoolCombatGoal());
        this.goalSelector.addGoal(1, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private class TheFoolCombatGoal extends Goal {

        private enum Phase { RETREATING, CIRCLING, MELEE, WINDUP, DASHING, RECOVERING }
        private enum DashType { NORMAL, SUPER }

        private static final double MELEE_DIST        = 4.0;
        private static final double MEDIUM_DIST       = 12.0;
        private static final double NORMAL_DASH_SPEED  = 1.2;
        private static final double SUPER_DASH_SPEED   = 1.8;
        private static final int    WINDUP_TICKS       = 12;
        private static final int    WINDUP_CANCEL_TICKS = 12;
        private static final int    MELEE_TICKS       = 40;
        private static final int    RETREAT_TICKS     = 25;
        private static final int    RECOVER_TICKS     = 20;
        private static final double CIRCLE_RADIUS     = 6.0;
        private static final double FLOAT_HEIGHT      = 1.2;
        private static final double CIRCLE_SPEED      = 0.04;
        private static final int    CIRCLE_WAIT_MAX   = 80;
        private static final int    FORCED_INTERVAL   = 20;
        private static final float  FORCED_CHANCE     = 0.40f;
        private static final int    CIRCLE_GIVE_UP    = 200;
        private static final int    FULL_COOLDOWN     = 120;

        private Player   target;
        private Phase    phase       = Phase.CIRCLING;
        private DashType dashType    = DashType.NORMAL;
        private int      phaseTimer  = 0;
        private int      cooldown    = 0;
        private double   circleAngle = 0;
        private int      circleDir   = 1;

        TheFoolCombatGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) { cooldown--; return false; }
            if (isStunned() || isIntroInProgress()) return false;
            LivingEntity t = getTarget();
            return t instanceof Player p && p.isAlive() && distanceTo(p) <= DASH_MAX_RANGE;
        }

        @Override
        public void start() {
            target      = (Player) getTarget();
            if (target == null) return;
            circleAngle = Math.atan2(getZ() - target.getZ(), getX() - target.getX());
            circleDir   = random.nextBoolean() ? 1 : -1;
            evaluateState();
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null || !target.isAlive() || isStunned()) return false;
            return distanceTo(target) <= DASH_MAX_RANGE * 1.5;
        }

        @Override
        public void tick() {
            if (target == null) return;
            phaseTimer++;
            switch (phase) {
                case RETREATING -> tickRetreating();
                case CIRCLING   -> tickCircling();
                case MELEE      -> tickMelee();
                case WINDUP     -> tickWindup();
                case DASHING    -> tickDashing();
                case RECOVERING -> tickRecovering();
            }
        }

        private void evaluateState() {
            double dist = distanceTo(target);
            if (dist < MELEE_DIST) {
                if (target.isBlocking()) {
                    enterPhase(Phase.RETREATING);
                } else {
                    enterPhase(Phase.MELEE);
                }
            } else if (target.isBlocking()) {
                enterPhase(Phase.CIRCLING);
            } else {
                startWindup(dist);
            }
        }

        private void enterPhase(Phase p) {
            phase      = p;
            phaseTimer = 0;
        }

        private void startWindup(double dist) {
            dashType          = (dist > MEDIUM_DIST) ? DashType.SUPER : DashType.NORMAL;
            dashSpeed         = (dashType == DashType.SUPER) ? SUPER_DASH_SPEED : NORMAL_DASH_SPEED;
            dashingTowardUuid = target.getUUID();
            dashDirection     = null;
            enterPhase(Phase.WINDUP);
            entityData.set(IS_WINDING_UP, true);
        }

        private void tickRetreating() {
            getLookControl().setLookAt(target, 30f, 30f);
            Vec3 away = position().subtract(target.position()).normalize();
            Vec3 retreatPos = target.position().add(away.scale(CIRCLE_RADIUS));
            getMoveControl().setWantedPosition(retreatPos.x, target.getY() + FLOAT_HEIGHT, retreatPos.z, 1.0);
            if (phaseTimer >= RETREAT_TICKS || distanceTo(target) >= MELEE_DIST * 1.4) {
                evaluateState();
            }
        }

        private void tickCircling() {
            getLookControl().setLookAt(target, 30f, 30f);
            circleAngle += CIRCLE_SPEED * circleDir;
            double cx = target.getX() + Math.cos(circleAngle) * CIRCLE_RADIUS;
            double cz = target.getZ() + Math.sin(circleAngle) * CIRCLE_RADIUS;
            getMoveControl().setWantedPosition(cx, target.getY() + FLOAT_HEIGHT, cz, 1.0);

            double dist = distanceTo(target);
            if (!target.isBlocking()) {
                if (dist < MELEE_DIST) enterPhase(Phase.RETREATING);
                else startWindup(dist);
                return;
            }
            if (dist < MELEE_DIST) { enterPhase(Phase.RETREATING); return; }

            if (phaseTimer >= CIRCLE_WAIT_MAX
                    && phaseTimer % FORCED_INTERVAL == 0
                    && random.nextFloat() < FORCED_CHANCE) {
                startWindup(dist);
                return;
            }
            if (phaseTimer >= CIRCLE_GIVE_UP) stop();
        }

        private void tickMelee() {
            getLookControl().setLookAt(target, 30f, 30f);
            getMoveControl().setWantedPosition(target.getX(), target.getY() + 0.5, target.getZ(), 1.0);

            if (target.isBlocking()) {
                enterPhase(Phase.RETREATING);
                return;
            }
            if (distanceTo(target) < 2.5) {
                doHurtTarget(target);
                enterPhase(Phase.RECOVERING);
                return;
            }
            if (phaseTimer >= MELEE_TICKS) {
                evaluateState();
            }
        }

        private void tickWindup() {
            getLookControl().setLookAt(target, 30f, 30f);

            if (phaseTimer < WINDUP_CANCEL_TICKS && target.isBlocking()) {
                entityData.set(IS_WINDING_UP, false);
                dashingTowardUuid = null;
                dashDirection = null;
                enterPhase(Phase.CIRCLING);
                return;
            }

            if (phaseTimer >= WINDUP_TICKS) {
                entityData.set(IS_WINDING_UP, false);
                Player dashTarget = dashingTowardUuid != null ? level().getPlayerByUUID(dashingTowardUuid) : null;
                if (dashTarget != null && dashTarget.isAlive()) {
                    Vec3 toTarget = dashTarget.position().add(0, 1.0, 0).subtract(position());
                    dashDirection = toTarget.lengthSqr() > 1e-6 ? toTarget.normalize() : Vec3.ZERO;
                }
                entityData.set(IS_CHARGING, true);
                enterPhase(Phase.DASHING);
            }
        }

        private void tickDashing() {
            getLookControl().setLookAt(target, 30f, 30f);
            if (distanceTo(target) < 2.5) {
                doHurtTarget(target);
                endDash();
            } else if (phaseTimer >= 50) {
                endDash();
            }
        }

        private void endDash() {
            dashingTowardUuid = null;
            entityData.set(IS_CHARGING, false);
            enterPhase(Phase.RECOVERING);
        }

        private void tickRecovering() {
            getLookControl().setLookAt(target, 30f, 30f);
            if (phaseTimer >= RECOVER_TICKS) evaluateState();
        }

        @Override
        public void stop() {
            cooldown          = FULL_COOLDOWN;
            dashingTowardUuid = null;
            dashDirection     = null;
            entityData.set(IS_CHARGING, false);
            entityData.set(IS_WINDING_UP, false);
            target     = null;
            phaseTimer = 0;
            phase      = Phase.CIRCLING;
        }
    }

    @Override
    protected void customServerAiStep() {
        if (level().isClientSide || dashingTowardUuid == null) return;
        Player dashingToward = level().getPlayerByUUID(dashingTowardUuid);
        if (dashingToward == null || !dashingToward.isAlive()) return;

        if (isWindingUp()) {
            double targetY = dashingToward.getY() + 2.0;
            double dy      = targetY - this.getY();
            double vy      = Math.max(-0.35, Math.min(0.35, dy * 0.5));
            setDeltaMovement(0, vy, 0);
        } else if (isCharging()) {
            if (dashDirection == null) {
                Vec3 t = dashingToward.position().add(0, 1.0, 0).subtract(this.position());
                dashDirection = t.lengthSqr() > 1e-6 ? t.normalize() : Vec3.ZERO;
            }
            Vec3 toTarget = dashingToward.position().add(0, 1.0, 0).subtract(this.position());
            if (toTarget.lengthSqr() > 1e-6) {
                toTarget = toTarget.normalize();
                double dot   = Math.max(-1.0, Math.min(1.0, dashDirection.dot(toTarget)));
                double angle = Math.acos(dot);
                if (angle > MAX_DASH_ANGLE_TICK) {
                    double t = MAX_DASH_ANGLE_TICK / angle;
                    dashDirection = new Vec3(
                            dashDirection.x + (toTarget.x - dashDirection.x) * t,
                            dashDirection.y + (toTarget.y - dashDirection.y) * t,
                            dashDirection.z + (toTarget.z - dashDirection.z) * t
                    ).normalize();
                } else {
                    dashDirection = toTarget;
                }
            }
            setDeltaMovement(dashDirection.scale(dashSpeed));
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_ANIMATION_TICK, 0);
        builder.define(ATTACK_RIGHT, true);
        builder.define(IS_STUNNED, false);
        builder.define(IS_CHARGING, false);
        builder.define(IS_WINDING_UP, false);
        builder.define(IS_PUSHING, false);
    }

    @Override
    public void tick() {
        if (waitingForDeath && !level().isClientSide) {
            deathHoldTicks++;
            if (deathHoldTicks >= DEATH_EXTEND_TICKS + 20) {
                this.discard();
                return;
            }
            setDeltaMovement(0, 0, 0);
            if (deathHoldTicks == DEATH_SPEAK_TICK && level() instanceof ServerLevel serverLevel) {
                serverLevel.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(128))
                        .forEach(p -> p.sendSystemMessage(
                                Component.translatable("entity.arcana.the_fool.death")
                                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
                        ));
            }
        }
        super.tick();
        if (waitingForDeath && !level().isClientSide && deathHoldTicks < DEATH_EXTEND_TICKS) {
            deathTime = 0;
        }
        if (this.level().isClientSide) {
            updateClientAnimations();
        } else {
            updateBossBar();
            updateAttackAnimationTick();
            updateStun();
            updateRageTimers();
            if (pushTicks > 0 && !waitingForDeath) tickPush();
            else if (!isStunned() && !isCharging() && pushCooldown == 0 && !isIntroInProgress() && !waitingForDeath) checkPushTrigger();
            if (isIntroInProgress()) handleIntro();
        }
    }

    private void updateBossBar() {
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void updateAttackAnimationTick() {
        int tick = getAttackAnimationTick();
        if (tick > 0) this.entityData.set(ATTACK_ANIMATION_TICK, tick - 1);
    }

    private void updateStun() {
        if (stunTicks > 0) {
            stunTicks--;
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.3));
            if (stunTicks <= 0) this.entityData.set(IS_STUNNED, false);
        }
    }

    private boolean isIntroInProgress() {
        return !introCompleted;
    }

    private void handleIntro() {
        introTicks++;
        Player nearest = level().getNearestPlayer(this, 64);
        if (nearest != null) getLookControl().setLookAt(nearest, 30f, 30f);
        if (introTicks == INTRO_LINE_1_TICK) {
            broadcastMessage(Component.translatable("entity.arcana.the_fool.intro.line1")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else if (introTicks == INTRO_LINE_2_TICK) {
            broadcastMessage(Component.translatable("entity.arcana.the_fool.intro.line2")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }
        if (introTicks >= INTRO_DURATION) {
            introCompleted = true;
        }
    }

    private void broadcastMessage(Component message) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64))
                .forEach(p -> p.sendSystemMessage(message));
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        if (isIntroInProgress()) return null;
        if (isStunned()) return null;
        return super.getTarget();
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (isIntroInProgress()) return false;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) return false;
        boolean result = super.hurt(source, Math.min(amount, MAX_DAMAGE_PER_HIT));
        if (result && !level().isClientSide) trackRage(source);
        return result;
    }

    private void updateRageTimers() {
        if (hitWindowTimer > 0) hitWindowTimer--;
        else recentHitCount = 0;
        if (shockwaveCooldown > 0) shockwaveCooldown--;
        if (pushCooldown > 0) pushCooldown--;
    }

    private void trackRage(DamageSource source) {
        if (isStunned()) return;

        recentHitCount++;
        hitWindowTimer = RAGE_WINDOW_TICKS;

        if (random.nextFloat() < COUNTER_CHANCE) {
            Entity attacker = source.getDirectEntity();
            if (attacker instanceof LivingEntity le) {
                le.knockback(1.0, this.getX() - le.getX(), this.getZ() - le.getZ());
            }
        }

        if (recentHitCount >= 2 && pushTicks == 0 && pushCooldown == 0) {
            Entity attacker = source.getDirectEntity();
            if (attacker instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                pushTicks        = PUSH_DURATION;
                pushTargetUuid   = player.getUUID();
                this.entityData.set(IS_PUSHING, true);
            }
        }

        if (recentHitCount >= RAGE_HIT_THRESHOLD && shockwaveCooldown == 0) {
            emitRageShockwave();
            recentHitCount    = 0;
            shockwaveCooldown = SHOCKWAVE_COOLDOWN;
        }
    }

    private void emitRageShockwave() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(7))
                .forEach(p -> {
                    p.knockback(1.4, this.getX() - p.getX(), this.getZ() - p.getZ());
                    p.setDeltaMovement(p.getDeltaMovement().add(0, 0.3, 0));
                });
        this.playSound(ModSounds.THE_FOOL_SHOCKWAVE.get(), 1.2f, 0.6f);
    }

    private void checkPushTrigger() {
        if (!(level() instanceof ServerLevel sl)) return;
        for (Player p : sl.players()) {
            if (p.isAlive() && !p.isSpectator() && !p.isCreative() && this.distanceTo(p) < PUSH_RANGE) {
                pushTicks      = PUSH_DURATION;
                pushTargetUuid = p.getUUID();
                this.entityData.set(IS_PUSHING, true);
                return;
            }
        }
    }

    private void tickPush() {
        pushTicks--;
        Player pushTarget = pushTargetUuid != null ? level().getPlayerByUUID(pushTargetUuid) : null;
        if (pushTarget != null) getLookControl().setLookAt(pushTarget, 30f, 30f);
        if (pushTicks == PUSH_HIT_TICK && pushTarget != null && pushTarget.isAlive()
                && !pushTarget.isCreative() && !pushTarget.isSpectator()) {
            pushTarget.knockback(1.2, this.getX() - pushTarget.getX(), this.getZ() - pushTarget.getZ());
            pushTarget.setDeltaMovement(pushTarget.getDeltaMovement().add(0, 0.15, 0));
            if (pushTarget instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(pushTarget));
            }
        }
        if (pushTicks <= 0) {
            this.entityData.set(IS_PUSHING, false);
            pushTargetUuid = null;
            pushCooldown   = PUSH_COOLDOWN_MAX;
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (isStunned()) return false;
        if (!this.level().isClientSide) {
            if (target instanceof Player player && player.isBlocking()) {
                applyStun();
                disablePlayerShield(player);
                return false;
            }
        }
        boolean result = super.doHurtTarget(target);
        if (!this.level().isClientSide) {
            triggerAttackAnimation();
            if (result) applyRecoil(target);
        }
        return result;
    }

    private void applyRecoil(Entity target) {
        Vec3 dir = this.position().subtract(target.position()).normalize();
        this.setDeltaMovement(dir.scale(RECOIL_SPEED));
    }

    private void disablePlayerShield(Player player) {
        player.stopUsingItem();
        player.getCooldowns().addCooldown(Items.SHIELD, SHIELD_DISABLE_TICKS);
    }

    private void applyStun() {
        stunTicks = STUN_DURATION;
        this.entityData.set(IS_STUNNED, true);
        this.setDeltaMovement(0, 0, 0);
        dashingTowardUuid = null;
        dashDirection     = null;
        pushTargetUuid    = null;
        pushTicks         = 0;
        this.entityData.set(IS_CHARGING, false);
        this.entityData.set(IS_WINDING_UP, false);
        this.entityData.set(IS_PUSHING, false);
    }

    private void triggerAttackAnimation() {
        boolean last = this.entityData.get(ATTACK_RIGHT);
        this.entityData.set(ATTACK_ANIMATION_TICK, 15);
        this.entityData.set(ATTACK_RIGHT, !last);
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel serverLevel, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, source, recentlyHit);
        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(ModItems.FOOL_SOUL.get()));
    }

    @Override
    public void die(@NotNull DamageSource source) {
        super.die(source);
        waitingForDeath   = true;
        deathHoldTicks    = 0;
        setNoGravity(true);
        setDeltaMovement(0, 0, 0);
        dashingTowardUuid = null;
        dashDirection     = null;
        pushTargetUuid    = null;
        pushTicks         = 0;
        this.entityData.set(IS_CHARGING, false);
        this.entityData.set(IS_WINDING_UP, false);
        this.entityData.set(IS_PUSHING, false);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            stopAllExcept(deathAnimationState);
            deathAnimationState.startIfStopped(this.tickCount);
            return;
        }
        deathAnimationState.stop();

        if (isStunned()) {
            stopAllExcept(stunnedAnimationState);
            stunnedAnimationState.startIfStopped(this.tickCount);
            return;
        }
        stunnedAnimationState.stop();

        if (isPushing()) {
            stopAllExcept(pushAnimationState);
            pushAnimationState.startIfStopped(this.tickCount);
            return;
        }
        pushAnimationState.stop();

        if (isWindingUp()) {
            stopAllExcept(windupAnimationState);
            windupAnimationState.startIfStopped(this.tickCount);
            return;
        }
        windupAnimationState.stop();

        if (isCharging()) {
            stopAllExcept(chargingAnimationState);
            chargingAnimationState.startIfStopped(this.tickCount);
            return;
        }
        chargingAnimationState.stop();

        if (getAttackAnimationTick() > 0) {
            floatingAnimationState.stop();
            if (isRightAttack()) {
                leftAttackAnimationState.stop();
                rightAttackAnimationState.startIfStopped(this.tickCount);
            } else {
                rightAttackAnimationState.stop();
                leftAttackAnimationState.startIfStopped(this.tickCount);
            }
            return;
        }

        rightAttackAnimationState.stop();
        leftAttackAnimationState.stop();
        floatingAnimationState.startIfStopped(this.tickCount);
    }

    private void stopAllExcept(AnimationState keep) {
        for (AnimationState s : new AnimationState[]{
                floatingAnimationState, rightAttackAnimationState, leftAttackAnimationState,
                stunnedAnimationState, chargingAnimationState, windupAnimationState,
                pushAnimationState, deathAnimationState
        }) {
            if (s != keep) s.stop();
        }
    }

    public boolean isRightAttack()        { return this.entityData.get(ATTACK_RIGHT); }
    public int    getAttackAnimationTick() { return this.entityData.get(ATTACK_ANIMATION_TICK); }
    public boolean isStunned()            { return this.entityData.get(IS_STUNNED); }
    public boolean isCharging()           { return this.entityData.get(IS_CHARGING); }
    public boolean isWindingUp()          { return this.entityData.get(IS_WINDING_UP); }
    public boolean isPushing()            { return this.entityData.get(IS_PUSHING); }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(@NotNull ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override public boolean causeFallDamage(float d, float m, @NotNull DamageSource s) { return false; }
    @Override public boolean startRiding(@NotNull Entity vehicle, boolean force)        { return false; }
    @Override protected SoundEvent getAmbientSound()                                   { return ModSounds.THE_FOOL_AMBIENT.get(); }
    @Override protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource s)      { return ModSounds.THE_FOOL_HURT.get(); }
    @Override protected @NotNull SoundEvent getDeathSound()                            { return ModSounds.THE_FOOL_DEATH.get(); }
    @Override public    boolean canUsePortal(boolean d)                                { return false; }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("introCompleted", introCompleted);
        tag.putBoolean("waitingForDeath", waitingForDeath);
        tag.putInt("deathHoldTicks", deathHoldTicks);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        introCompleted = tag.getBoolean("introCompleted");
        if (introCompleted) introTicks = INTRO_DURATION;
        waitingForDeath = tag.getBoolean("waitingForDeath");
        deathHoldTicks = tag.getInt("deathHoldTicks");
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }
}
