package com.example.arcana.client.model;

import com.example.arcana.ArcanaMod;
import com.example.arcana.client.animation.TheFoolAnimations;
import com.example.arcana.content.entity.boss.TheFoolEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TheFoolModel extends HierarchicalModel<TheFoolEntity> {

    private static final float DEG_TO_RAD = (float) Math.PI / 180F;

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "the_fool"),
                    "main"
            );

    private final ModelPart root;
    private final ModelPart theFool;
    private final ModelPart waist;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TheFoolModel(ModelPart root) {
        this.root = root;

        this.theFool = root.getChild("The_Fool");
        this.waist = theFool.getChild("Waist");

        this.head = waist.getChild("Head");
        this.body = waist.getChild("Body");
        this.rightArm = waist.getChild("RightArm");
        this.leftArm = waist.getChild("LeftArm");

        this.rightLeg = theFool.getChild("RightLeg");
        this.leftLeg = theFool.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition theFool = root.addOrReplaceChild(
                "The_Fool",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F)
        );

        PartDefinition waist = theFool.addOrReplaceChild(
                "Waist",
                CubeListBuilder.create(),
                PartPose.ZERO
        );

        waist.addOrReplaceChild(
                "Head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4, -8, -4, 8, 8, 8)
                        .texOffs(32, 0)
                        .addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.25F)),
                PartPose.offset(0, -12, 0)
        );

        waist.addOrReplaceChild(
                "Body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4, 0, -2, 8, 12, 4)
                        .texOffs(16, 32)
                        .addBox(-4, 0, -2, 8, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offset(0, -12, 0)
        );

        waist.addOrReplaceChild(
                "RightArm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.3F, -2, -2, 4, 12, 4)
                        .texOffs(40, 32)
                        .addBox(-3.3F, -2, -2, 4, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offset(-5, -10, 0)
        );

        waist.addOrReplaceChild(
                "LeftArm",
                CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-0.7F, -2, -2, 4, 12, 4)
                        .texOffs(48, 48)
                        .addBox(-0.7F, -2, -2, 4, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offset(5, -10, 0)
        );

        theFool.addOrReplaceChild(
                "RightLeg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2, 0, -2, 4, 12, 4)
                        .texOffs(0, 32)
                        .addBox(-2, 0, -2, 4, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(-1.9F, 0, 0, 0, 0, 0.0349F)
        );

        theFool.addOrReplaceChild(
                "LeftLeg",
                CubeListBuilder.create()
                        .texOffs(16, 48)
                        .addBox(-2, 0, -2, 4, 12, 4)
                        .texOffs(0, 48)
                        .addBox(-2, 0, -2, 4, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offset(1.9F, 0, 0)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(
            @NotNull TheFoolEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        root().getAllParts().forEach(ModelPart::resetPose);

        head.yRot = netHeadYaw * DEG_TO_RAD;
        head.xRot = headPitch * DEG_TO_RAD;

        applyAnimations(entity, ageInTicks);
    }

    private void applyAnimations(TheFoolEntity entity, float ageInTicks) {
        if (entity.isAggressive() && entity.getAttackAnimationTick() > 0) {
            if (entity.isRightAttack()) {
                animate(entity.rightAttackAnimationState, TheFoolAnimations.RIGHT_ATTACK, ageInTicks);
            } else {
                animate(entity.leftAttackAnimationState, TheFoolAnimations.LEFT_ATTACK, ageInTicks);
            }
            return;
        }

        if (entity.isUsingCubicDomain()) {
            animate(entity.cubicDomainAnimationState, TheFoolAnimations.CUBIC_DOMAIN, ageInTicks);
            return;
        }

        animate(entity.floatingAnimationState, TheFoolAnimations.FLOATING, ageInTicks);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    @Override
    public void renderToBuffer(
            @NotNull PoseStack poseStack,
            @NotNull VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        theFool.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
