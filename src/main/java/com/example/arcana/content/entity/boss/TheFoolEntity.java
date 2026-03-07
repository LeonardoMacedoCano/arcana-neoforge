package com.example.arcana.content.entity.boss;

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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheFoolEntity extends Monster {

    // --- Synced data ---
    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_TICK =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACK_RIGHT =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);

    // --- Animation states (client-side) ---
    public final AnimationState floatingAnimationState = new AnimationState();
    public final AnimationState rightAttackAnimationState = new AnimationState();
    public final AnimationState leftAttackAnimationState = new AnimationState();
    public final AnimationState cubicDomainAnimationState = new AnimationState();

    // --- Boss bar ---
    private final ServerBossEvent bossBar = new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    // --- Intro dialogue (server-side only, no sync needed) ---
    private static final int INTRO_DURATION = 120; // 6 seconds
    private static final int INTRO_LINE_1_TICK = 1;
    private static final int INTRO_LINE_2_TICK = 60;
    private int introTicks = 0;

    // -------------------------------------------------------------------------

    public TheFoolEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        configureEntity();
    }

    private void configureEntity() {
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

    // -------------------------------------------------------------------------
    // Goals
    // -------------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        registerMovementGoals();
        registerTargetGoals();
    }

    private void registerMovementGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    private void registerTargetGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    // -------------------------------------------------------------------------
    // Synced data
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_ANIMATION_TICK, 0);
        builder.define(ATTACK_RIGHT, true);
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            updateClientAnimations();
        } else {
            updateServerLogic();
        }
    }

    private void updateServerLogic() {
        updateBossBar();
        updateAttackAnimationTick();
        if (!isIntroComplete()) {
            handleIntro();
        }
    }

    private void updateBossBar() {
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void updateAttackAnimationTick() {
        int tick = getAttackAnimationTick();
        if (tick > 0) {
            this.entityData.set(ATTACK_ANIMATION_TICK, tick - 1);
        }
    }

    // -------------------------------------------------------------------------
    // Intro dialogue
    // -------------------------------------------------------------------------

    private boolean isIntroComplete() {
        return introTicks >= INTRO_DURATION;
    }

    private void handleIntro() {
        introTicks++;

        if (introTicks == INTRO_LINE_1_TICK) {
            broadcastIntroMessage(Component.translatable("entity.arcana.the_fool.intro.line1")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else if (introTicks == INTRO_LINE_2_TICK) {
            broadcastIntroMessage(Component.translatable("entity.arcana.the_fool.intro.line2")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }
    }

    private void broadcastIntroMessage(Component message) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                this.getBoundingBox().inflate(64)
        ).forEach(player -> player.sendSystemMessage(message));
    }

    // Suppress targeting during intro so the boss doesn't attack while speaking
    @Override
    @Nullable
    public LivingEntity getTarget() {
        if (!isIntroComplete()) return null;
        return super.getTarget();
    }

    // -------------------------------------------------------------------------
    // Attack
    // -------------------------------------------------------------------------

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (!this.level().isClientSide) {
            triggerAttackAnimation();
        }
        return super.doHurtTarget(target);
    }

    private void triggerAttackAnimation() {
        // Alternate arms: flip the current side on each hit
        boolean lastWasRight = this.entityData.get(ATTACK_RIGHT);
        this.entityData.set(ATTACK_ANIMATION_TICK, 15);
        this.entityData.set(ATTACK_RIGHT, !lastWasRight);
    }

    public boolean isRightAttack() {
        return this.entityData.get(ATTACK_RIGHT);
    }

    public int getAttackAnimationTick() {
        return this.entityData.get(ATTACK_ANIMATION_TICK);
    }

    // -------------------------------------------------------------------------
    // Client animations
    // -------------------------------------------------------------------------

    private void updateClientAnimations() {
        if (getAttackAnimationTick() > 0) {
            playAttackAnimation();
        } else {
            playIdleAnimation();
        }
    }

    private void playAttackAnimation() {
        this.floatingAnimationState.stop();
        if (isRightAttack()) {
            this.leftAttackAnimationState.stop();
            this.rightAttackAnimationState.startIfStopped(this.tickCount);
        } else {
            this.rightAttackAnimationState.stop();
            this.leftAttackAnimationState.startIfStopped(this.tickCount);
        }
    }

    private void playIdleAnimation() {
        this.rightAttackAnimationState.stop();
        this.leftAttackAnimationState.stop();
        this.floatingAnimationState.startIfStopped(this.tickCount);
    }

    // -------------------------------------------------------------------------
    // Boss bar visibility
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Damage overrides
    // -------------------------------------------------------------------------

    /** Flying boss — never takes fall damage regardless of height. */
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    // -------------------------------------------------------------------------
    // Sounds
    // -------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    @Override
    public boolean canUsePortal(boolean defaultValue) {
        return false;
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }
}
