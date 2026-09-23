package com.ratrod.archaion.client.model;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.DeepslateSentinel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Exact authored geometry with a native 1.20.1 procedural walk fallback. */
public final class DeepslateSentinelModel extends HierarchicalModel<DeepslateSentinel> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("deepslatesentinelmodel"), "main");
    private final ModelPart base, head, frontLeft, frontRight, backLeft, backRight;

    public DeepslateSentinelModel(ModelPart root) {
        this.base = root.getChild("base");
        this.head = base.getChild("head");
        this.frontLeft = base.getChild("front_left_leg");
        this.frontRight = base.getChild("front_right_leg");
        this.backLeft = base.getChild("back_left_leg");
        this.backRight = base.getChild("back_right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.ZERO);
        base.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 82).addBox(-20.1F, -26.1F, -4.3F, 40.0F, 39.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(88, 92).addBox(-6.4F, -1.4F, -9.0F, 13.0F, 22.0F, 7.0F, CubeDeformation.NONE)
                .texOffs(88, 82).addBox(-16.4F, -11.9F, -8.0F, 33.0F, 6.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 1.0F, -23.4F));
        base.addOrReplaceChild("lower_beak", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, -38.4F));
        base.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-16.1F, -17.1F, -24.1F, 32.0F, 34.0F, 48.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.2F, 0.0F));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(88, 121).addBox(-3.8F, -1.2F, -4.8F, 8.0F, 12.0F, 10.0F, CubeDeformation.NONE);
        base.addOrReplaceChild("front_left_leg", leg, PartPose.offset(9.0F, 13.2F, -18.0F));
        base.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(88, 121).addBox(-3.8F, -1.2F, -4.8F, 8.0F, 12.0F, 10.0F, CubeDeformation.NONE), PartPose.offset(-9.0F, 13.2F, -18.0F));
        base.addOrReplaceChild("back_left_leg", CubeListBuilder.create().texOffs(88, 121).addBox(-3.8F, -1.2F, -4.8F, 8.0F, 12.0F, 10.0F, CubeDeformation.NONE), PartPose.offset(9.0F, 13.2F, 18.0F));
        base.addOrReplaceChild("back_right_leg", CubeListBuilder.create().texOffs(88, 121).addBox(-3.8F, -1.2F, -4.8F, 8.0F, 12.0F, 10.0F, CubeDeformation.NONE), PartPose.offset(-9.0F, 13.2F, 18.0F));
        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override public ModelPart root() { return base; }

    @Override
    public void setupAnim(DeepslateSentinel entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        float amount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float phase = limbSwing * 1.5F;
        float swing = Mth.cos(phase) * 0.55F * amount;
        frontLeft.xRot = swing; backRight.xRot = swing;
        frontRight.xRot = -swing; backLeft.xRot = -swing;
        base.y = Mth.abs(Mth.sin(phase)) * -1.1F * amount;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.25F;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.25F;
    }
}
