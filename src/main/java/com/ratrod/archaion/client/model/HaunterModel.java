package com.ratrod.archaion.client.model;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Haunter;
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

/** 1.20.1 backport of Archaion's authored Haunter model. */
public final class HaunterModel extends HierarchicalModel<Haunter> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("hauntermodel"), "main");
    public static final ModelLayerLocation CHARGED_LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("hauntermodel"), "charged");
    private final ModelPart mob, head, leftLeg, rightLeg;

    public HaunterModel(ModelPart root) {
        this.mob = root.getChild("mob");
        this.head = mob.getChild("head");
        this.leftLeg = mob.getChild("left_leg");
        this.rightLeg = mob.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() { return createBodyLayer(CubeDeformation.NONE); }
    public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition mob = partdefinition.addOrReplaceChild("mob", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition head = mob.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -29.0F, -4.0F, 10.0F, 29.0F, 8.0F, deformation), PartPose.offset(0.0F, -12.0F, 0.0F));
        head.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(36, 15).addBox(-15.0F, -6.0F, 0.0F, 15.0F, 15.0F, 0.0F, deformation), PartPose.offset(-5.0F, -27.0F, 0.0F));
        head.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(36, 0).addBox(0.0F, -6.0F, 0.0F, 15.0F, 15.0F, 0.0F, deformation), PartPose.offset(5.0F, -27.0F, 0.0F));
        mob.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 30).addBox(-1.0F, 0.0F, -1.1F, 2.0F, 12.0F, 2.0F, deformation), PartPose.offset(3.0F, -12.0F, 0.1F));
        mob.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 37).addBox(-1.0F, 0.0F, -1.1F, 2.0F, 12.0F, 2.0F, deformation), PartPose.offset(-3.0F, -12.0F, 0.1F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override public ModelPart root() { return mob; }
    @Override public void setupAnim(Haunter entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        head.zRot += Mth.sin(ageInTicks * 0.08F) * 0.06F;
        head.y += Mth.sin(ageInTicks * 0.12F) * 0.45F;
        float amount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        leftLeg.xRot = Mth.cos(limbSwing * 0.9F) * 0.75F * amount;
        rightLeg.xRot = Mth.cos(limbSwing * 0.9F + Mth.PI) * 0.75F * amount;
    }
}
