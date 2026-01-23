package com.example.arcana.content.entity.boss;

import com.example.arcana.registry.ModItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TheFoolEntity extends Monster {

    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_TICK =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> USING_CUBIC_DOMAIN =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> ATTACK_RIGHT =
            SynchedEntityData.defineId(TheFoolEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState floatingAnimationState = new AnimationState();
    public final AnimationState rightAttackAnimationState = new AnimationState();
    public final AnimationState leftAttackAnimationState = new AnimationState();
    public final AnimationState cubicDomainAnimationState = new AnimationState();

    private final ServerBossEvent bossBar = new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );

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

    @Override
    protected void registerGoals() {
        registerMovementGoals();
        registerTargetGoals();
    }

    private void registerMovementGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    private void registerTargetGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return createFlyingNavigation(level);
    }

    private PathNavigation createFlyingNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_ANIMATION_TICK, 0);
        builder.define(USING_CUBIC_DOMAIN, false);
        builder.define(ATTACK_RIGHT, true);
    }

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

    private void updateClientAnimations() {
        if (isUsingCubicDomain()) {
            playCubicDomainAnimation();
        } else if (getAttackAnimationTick() > 0) {
            playAttackAnimation();
        } else {
            playIdleAnimation();
        }
    }

    private void playCubicDomainAnimation() {
        stopAllAnimations();
        this.cubicDomainAnimationState.startIfStopped(this.tickCount);
    }

    private void playAttackAnimation() {
        this.floatingAnimationState.stop();
        this.cubicDomainAnimationState.stop();

        if (isRightAttack()) {
            this.leftAttackAnimationState.stop();
            this.rightAttackAnimationState.startIfStopped(this.tickCount);
        } else {
            this.rightAttackAnimationState.stop();
            this.leftAttackAnimationState.startIfStopped(this.tickCount);
        }
    }

    private void playIdleAnimation() {
        stopAttackAnimations();
        this.cubicDomainAnimationState.stop();
        this.floatingAnimationState.startIfStopped(this.tickCount);
    }

    private void stopAllAnimations() {
        this.floatingAnimationState.stop();
        stopAttackAnimations();
        this.cubicDomainAnimationState.stop();
    }

    private void stopAttackAnimations() {
        this.rightAttackAnimationState.stop();
        this.leftAttackAnimationState.stop();
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (!this.level().isClientSide) {
            startAttackAnimation();
        }
        return super.doHurtTarget(target);
    }

    private void startAttackAnimation() {
        this.entityData.set(ATTACK_ANIMATION_TICK, 15);
        this.entityData.set(ATTACK_RIGHT, this.random.nextBoolean());
    }

    public boolean isRightAttack() {
        return this.entityData.get(ATTACK_RIGHT);
    }

    public int getAttackAnimationTick() {
        return this.entityData.get(ATTACK_ANIMATION_TICK);
    }

    public boolean isUsingCubicDomain() {
        return this.entityData.get(USING_CUBIC_DOMAIN);
    }

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

    @Override
    protected void dropCustomDeathLoot(
            @NotNull ServerLevel level,
            @NotNull DamageSource source,
            boolean hitByPlayer
    ) {
        super.dropCustomDeathLoot(level, source, hitByPlayer);
        dropSoul();
    }

    private void dropSoul() {
        this.spawnAtLocation(new ItemStack(ModItems.FOOL_SOUL.get()));
    }

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
