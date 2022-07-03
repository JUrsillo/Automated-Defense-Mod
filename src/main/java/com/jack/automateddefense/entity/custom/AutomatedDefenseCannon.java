package com.jack.automateddefense.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AutomatedDefenseCannon extends GolemEntity implements IMob {

    private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
    private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0D, AttributeModifier.Operation.ADDITION);
    protected static final DataParameter<Direction> DATA_ATTACH_FACE_ID = EntityDataManager.defineId(AutomatedDefenseCannon.class, DataSerializers.DIRECTION);
    protected static final DataParameter<Optional<BlockPos>> DATA_ATTACH_POS_ID = EntityDataManager.defineId(AutomatedDefenseCannon.class, DataSerializers.OPTIONAL_BLOCK_POS);
    protected static final DataParameter<Byte> DATA_PEEK_ID = EntityDataManager.defineId(AutomatedDefenseCannon.class, DataSerializers.BYTE);
    protected static final DataParameter<Byte> DATA_COLOR_ID = EntityDataManager.defineId(AutomatedDefenseCannon.class, DataSerializers.BYTE);
    private float currentPeekAmountO;
    private float currentPeekAmount;
    private BlockPos oldAttachPosition = null;
    private int clientSideTeleportInterpolation;

    public AutomatedDefenseCannon(EntityType<? extends GolemEntity> p_i48569_1_, World p_i48569_2_) {
        super(p_i48569_1_, p_i48569_2_);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(4, new AutomatedDefenseCannon.AttackGoal());
        this.goalSelector.addGoal(7, new AutomatedDefenseCannon.PeekGoal());
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(2, new AutomatedDefenseCannon.AttackNearestGoal(this));
        this.targetSelector.addGoal(3, new AutomatedDefenseCannon.DefenseAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this,
                MobEntity.class, 1, false, false,
                (p_234199_0_) -> p_234199_0_ instanceof IMob && !(p_234199_0_ instanceof AutomatedDefenseCannon)));



    }

    protected boolean isMovementNoisy() {
        return false;
    }

    public SoundCategory getSoundSource() {
        return SoundCategory.HOSTILE;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHULKER_AMBIENT;
    }

    /**
     * Plays living's sound at its position
     */
    public void playAmbientSound() {
        if (!this.isClosed()) {
            super.playAmbientSound();
        }

    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SHULKER_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return this.isClosed() ? SoundEvents.SHULKER_HURT_CLOSED : SoundEvents.SHULKER_HURT;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
        this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
        this.entityData.define(DATA_PEEK_ID, (byte)0);
        this.entityData.define(DATA_COLOR_ID, (byte)16);
    }

    protected BodyController createBodyControl() {
        return new AutomatedDefenseCannon.BodyHelperController(this);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundNBT pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.entityData.set(DATA_ATTACH_FACE_ID, Direction.from3DDataValue(pCompound.getByte("AttachFace")));
        this.entityData.set(DATA_PEEK_ID, pCompound.getByte("Peek"));
        this.entityData.set(DATA_COLOR_ID, pCompound.getByte("Color"));
        if (pCompound.contains("APX")) {
            int i = pCompound.getInt("APX");
            int j = pCompound.getInt("APY");
            int k = pCompound.getInt("APZ");
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(i, j, k)));
        } else {
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
        }

    }

    public void addAdditionalSaveData(CompoundNBT pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putByte("AttachFace", (byte)this.entityData.get(DATA_ATTACH_FACE_ID).get3DDataValue());
        pCompound.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
        pCompound.putByte("Color", this.entityData.get(DATA_COLOR_ID));
        BlockPos blockpos = this.getAttachPosition();
        if (blockpos != null) {
            pCompound.putInt("APX", blockpos.getX());
            pCompound.putInt("APY", blockpos.getY());
            pCompound.putInt("APZ", blockpos.getZ());
        }

    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();
        BlockPos blockpos = this.entityData.get(DATA_ATTACH_POS_ID).orElse((BlockPos)null);
        if (blockpos == null && !this.level.isClientSide) {
            blockpos = this.blockPosition();
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockpos));
        }

        if (this.isPassenger()) {
            blockpos = null;
            float f = this.getVehicle().yRot;
            this.yRot = f;
            this.yBodyRot = f;
            this.yBodyRotO = f;
            this.clientSideTeleportInterpolation = 0;
        } else if (!this.level.isClientSide) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (!blockstate.isAir(this.level, blockpos)) {
                if (blockstate.is(Blocks.MOVING_PISTON)) {
                    Direction direction = blockstate.getValue(PistonBlock.FACING);
                    if (this.level.isEmptyBlock(blockpos.relative(direction))) {
                        blockpos = blockpos.relative(direction);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockpos));
                    }
                } else if (blockstate.is(Blocks.PISTON_HEAD)) {
                    Direction direction3 = blockstate.getValue(PistonHeadBlock.FACING);
                    if (this.level.isEmptyBlock(blockpos.relative(direction3))) {
                        blockpos = blockpos.relative(direction3);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockpos));
                    }
                }
            }

            Direction direction4 = this.getAttachFace();
            if (!this.canAttachOnBlockFace(blockpos, direction4)) {
                Direction direction1 = this.findAttachableFace(blockpos);
                if (direction1 != null) {
                    this.entityData.set(DATA_ATTACH_FACE_ID, direction1);
                }
            }
        }

        float f1 = (float)this.getRawPeekAmount() * 0.01F;
        this.currentPeekAmountO = this.currentPeekAmount;
        if (this.currentPeekAmount > f1) {
            this.currentPeekAmount = MathHelper.clamp(this.currentPeekAmount - 0.05F, f1, 1.0F);
        } else if (this.currentPeekAmount < f1) {
            this.currentPeekAmount = MathHelper.clamp(this.currentPeekAmount + 0.05F, 0.0F, f1);
        }

        if (blockpos != null) {
            if (this.level.isClientSide) {
                if (this.clientSideTeleportInterpolation > 0 && this.oldAttachPosition != null) {
                    --this.clientSideTeleportInterpolation;
                } else {
                    this.oldAttachPosition = blockpos;
                }
            }

            this.setPosAndOldPos((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D);
            double d2 = 0.5D - (double)MathHelper.sin((0.5F + this.currentPeekAmount) * (float)Math.PI) * 0.5D;
            double d0 = 0.5D - (double)MathHelper.sin((0.5F + this.currentPeekAmountO) * (float)Math.PI) * 0.5D;
            if (this.isAddedToWorld() && this.level instanceof net.minecraft.world.server.ServerWorld) ((net.minecraft.world.server.ServerWorld)this.level).updateChunkPos(this); // Forge - Process chunk registration after moving.
            Direction direction2 = this.getAttachFace().getOpposite();
            this.setBoundingBox((new AxisAlignedBB(this.getX() - 0.5D, this.getY(), this.getZ() - 0.5D, this.getX() + 0.5D, this.getY() + 1.0D, this.getZ() + 0.5D)).expandTowards((double)direction2.getStepX() * d2, (double)direction2.getStepY() * d2, (double)direction2.getStepZ() * d2));
            double d1 = d2 - d0;
            if (d1 > 0.0D) {
                List<Entity> list = this.level.getEntities(this, this.getBoundingBox());
                if (!list.isEmpty()) {
                    for(Entity entity : list) {
                        if (!(entity instanceof AutomatedDefenseCannon) && !entity.noPhysics) {
                            entity.move(MoverType.SHULKER, new Vector3d(d1 * (double)direction2.getStepX(), d1 * (double)direction2.getStepY(), d1 * (double)direction2.getStepZ()));
                        }
                    }
                }
            }
        }

    }

    /**
     * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
     */
    public void setPos(double pX, double pY, double pZ) {
        super.setPos(pX, pY, pZ);
        if (this.entityData != null && this.tickCount != 0) {
            Optional<BlockPos> optional = this.entityData.get(DATA_ATTACH_POS_ID);
            if (this.isAddedToWorld() && this.level instanceof net.minecraft.world.server.ServerWorld) ((net.minecraft.world.server.ServerWorld)this.level).updateChunkPos(this); // Forge - Process chunk registration after moving.
            Optional<BlockPos> optional1 = Optional.of(new BlockPos(pX, pY, pZ));
            if (!optional1.equals(optional)) {
                this.entityData.set(DATA_ATTACH_POS_ID, optional1);
                this.entityData.set(DATA_PEEK_ID, (byte)0);
                this.hasImpulse = true;
            }

        }
    }

    @Nullable
    protected Direction findAttachableFace(BlockPos pPos) {
        for(Direction direction : Direction.values()) {
            if (this.canAttachOnBlockFace(pPos, direction)) {
                return direction;
            }
        }

        return null;
    }

    private boolean canAttachOnBlockFace(BlockPos pPos, Direction pDirection) {
        return this.level.loadedAndEntityCanStandOnFace(pPos.relative(pDirection), this, pDirection.getOpposite()) && this.level.noCollision(this, ShulkerAABBHelper.openBoundingBox(pPos, pDirection.getOpposite()));
    }


    /**
     * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
     * react to sunlight and start to burn.
     */
    public void aiStep() {
        super.aiStep();
        this.setDeltaMovement(Vector3d.ZERO);
        if (!this.isNoAi()) {
            this.yBodyRotO = 0.0F;
            this.yBodyRot = 0.0F;
        }

    }

    public void onSyncedDataUpdated(DataParameter<?> pKey) {
        if (DATA_ATTACH_POS_ID.equals(pKey) && this.level.isClientSide && !this.isPassenger()) {
            BlockPos blockpos = this.getAttachPosition();
            if (blockpos != null) {
                if (this.oldAttachPosition == null) {
                    this.oldAttachPosition = blockpos;
                } else {
                    this.clientSideTeleportInterpolation = 6;
                }

                this.setPosAndOldPos((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D);
            }
        }

        super.onSyncedDataUpdated(pKey);
    }

    /**
     * Sets a target for the client to interpolate towards over the next few ticks
     */
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
        this.lerpSteps = 0;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isClosed()) {
            Entity entity = pSource.getDirectEntity();
            if (entity instanceof AbstractArrowEntity) {
                return false;
            }
        }

        return super.hurt(pSource, pAmount);
    }

    private boolean isClosed() {
        return this.getRawPeekAmount() == 0;
    }

    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    public Direction getAttachFace() {
        return this.entityData.get(DATA_ATTACH_FACE_ID);
    }

    @Nullable
    public BlockPos getAttachPosition() {
        return this.entityData.get(DATA_ATTACH_POS_ID).orElse((BlockPos)null);
    }

    public void setAttachPosition(@Nullable BlockPos pPos) {
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(pPos));
    }

    public int getRawPeekAmount() {
        return this.entityData.get(DATA_PEEK_ID);
    }

    /**
     * Applies or removes armor modifier
     */
    public void setRawPeekAmount(int pPeekAmount) {
        if (!this.level.isClientSide) {
            this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
            if (pPeekAmount == 0) {
                this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
                this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
            } else {
                this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
            }
        }

        this.entityData.set(DATA_PEEK_ID, (byte)pPeekAmount);
    }

    @OnlyIn(Dist.CLIENT)
    public float getClientPeekAmount(float pPartialTick) {
        return MathHelper.lerp(pPartialTick, this.currentPeekAmountO, this.currentPeekAmount);
    }

    @OnlyIn(Dist.CLIENT)
    public int getClientSideTeleportInterpolation() {
        return this.clientSideTeleportInterpolation;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getOldAttachPosition() {
        return this.oldAttachPosition;
    }

    protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
        return 0.5F;
    }

    /**
     * The speed it takes to move the entityliving's head rotation through the faceEntity method.
     */
    public int getMaxHeadXRot() {
        return 180;
    }

    public int getMaxHeadYRot() {
        return 180;
    }

    /**
     * Applies a velocity to the entities, to push them away from eachother.
     */
    public void push(Entity pEntity) {
    }

    public float getPickRadius() {
        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasValidInterpolationPositions() {
        return this.oldAttachPosition != null && this.getAttachPosition() != null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public DyeColor getColor() {
        Byte obyte = this.entityData.get(DATA_COLOR_ID);
        return obyte != 16 && obyte <= 15 ? DyeColor.byId(obyte) : null;
    }

    class AttackGoal extends Goal {
        private int attackTime;

        public AttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            LivingEntity livingentity = AutomatedDefenseCannon.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                return AutomatedDefenseCannon.this.level.getDifficulty() != Difficulty.PEACEFUL;
            } else {
                return false;
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.attackTime = 20;
            AutomatedDefenseCannon.this.setRawPeekAmount(100);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            AutomatedDefenseCannon.this.setRawPeekAmount(0);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            if (AutomatedDefenseCannon.this.level.getDifficulty() != Difficulty.PEACEFUL) {
                --this.attackTime;
                LivingEntity livingentity = AutomatedDefenseCannon.this.getTarget();
                AutomatedDefenseCannon.this.getLookControl().setLookAt(livingentity, 180.0F, 180.0F);
                double d0 = AutomatedDefenseCannon.this.distanceToSqr(livingentity);
                if (d0 < 400.0D) {
                    if (this.attackTime <= 0) {
                        this.attackTime = 20 + AutomatedDefenseCannon.this.random.nextInt(10) * 20 / 2;
                        Vector3d vector3d = AutomatedDefenseCannon.this.getViewVector(1.0F);
                        double d2 = livingentity.getX() - (AutomatedDefenseCannon.this.getX());
                        double d3 = livingentity.getY(0.5D) - (0.5D + AutomatedDefenseCannon.this.getY(0.5D));
                        double d4 = livingentity.getZ() - (AutomatedDefenseCannon.this.getZ());

                        FireballEntity fireballentity = new FireballEntity(level, AutomatedDefenseCannon.this, d2, d3, d4);
                        fireballentity.setPos(AutomatedDefenseCannon.this.getX(), AutomatedDefenseCannon.this.getY(0.5D) + 0.5D, fireballentity.getZ());
                        level.addFreshEntity(fireballentity);
                        
                        AutomatedDefenseCannon.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (AutomatedDefenseCannon.this.random.nextFloat() - AutomatedDefenseCannon.this.random.nextFloat()) * 0.2F + 1.0F);
                    }
                } else {
                    AutomatedDefenseCannon.this.setTarget((LivingEntity)null);
                }

                super.tick();
            }
        }
    }

    class AttackNearestGoal extends NearestAttackableTargetGoal<PlayerEntity> {
        public AttackNearestGoal(AutomatedDefenseCannon pShulker) {
            super(pShulker, PlayerEntity.class, true);
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return AutomatedDefenseCannon.this.level.getDifficulty() == Difficulty.PEACEFUL ? false : super.canUse();
        }

        protected AxisAlignedBB getTargetSearchArea(double pTargetDistance) {
            Direction direction = ((AutomatedDefenseCannon)this.mob).getAttachFace();
            if (direction.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0D, pTargetDistance, pTargetDistance);
            } else {
                return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(pTargetDistance, pTargetDistance, 4.0D) : this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
            }
        }
    }

    class BodyHelperController extends BodyController {
        public BodyHelperController(MobEntity pMob) {
            super(pMob);
        }

        /**
         * Update the Head and Body rendenring angles
         */
        public void clientTick() {
        }
    }

    static class DefenseAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
        public DefenseAttackGoal(AutomatedDefenseCannon pShulker) {
            super(pShulker, LivingEntity.class, 10, true, false, (p_200826_0_) -> {
                return p_200826_0_ instanceof IMob;
            });
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */

        public boolean canUse() {
            return this.mob.getTeam() == null ? false : super.canUse();
        }

        protected AxisAlignedBB getTargetSearchArea(double pTargetDistance) {
            Direction direction = ((AutomatedDefenseCannon)this.mob).getAttachFace();
            if (direction.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0D, pTargetDistance, pTargetDistance);
            } else {
                return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(pTargetDistance, pTargetDistance, 4.0D) : this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
            }
        }
    }

    class PeekGoal extends Goal {
        private int peekTime;

        private PeekGoal() {
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return AutomatedDefenseCannon.this.getTarget() == null && AutomatedDefenseCannon.this.random.nextInt(40) == 0;
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return AutomatedDefenseCannon.this.getTarget() == null && this.peekTime > 0;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.peekTime = 20 * (1 + AutomatedDefenseCannon.this.random.nextInt(3));
            AutomatedDefenseCannon.this.setRawPeekAmount(30);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            if (AutomatedDefenseCannon.this.getTarget() == null) {
                AutomatedDefenseCannon.this.setRawPeekAmount(0);
            }

        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            --this.peekTime;
        }
    }
}
