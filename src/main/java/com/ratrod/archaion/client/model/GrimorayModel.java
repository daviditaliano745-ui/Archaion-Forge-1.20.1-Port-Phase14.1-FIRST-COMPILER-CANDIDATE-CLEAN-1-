package com.ratrod.archaion.client.model;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Grimoray;
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

/** 1.20.1 backport of Archaion's authored Grimoray model. */
public final class GrimorayModel extends HierarchicalModel<Grimoray> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("grimoraymodel"), "main");
    private final ModelPart base, arml, armr;

    public GrimorayModel(ModelPart root) {
        this.base = root.getChild("base");
        this.arml = base.getChild("arml");
        this.armr = base.getChild("armr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(10, 28).addBox(-1.0F, -6.0F, 0.0F, 2.0F, 13.0F, 1.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 17.0F, 0.0F));
        PartDefinition arml = base.addOrReplaceChild("arml", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -7.0F, -0.4F, 9.0F, 13.0F, 1.0F, CubeDeformation.NONE)
                .texOffs(20, 24).addBox(9.6F, -7.0F, -2.4F, 0.0F, 13.0F, 5.0F, CubeDeformation.NONE), PartPose.offset(1.0F, 1.0F, 0.4F));
        arml.addOrReplaceChild("thin", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -6.5F, 0.2F, 9.0F, 12.0F, 0.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(-1.0F, 0.0F, -0.6F, 0.0F, 0.0873F, 0.0F));
        PartDefinition armr = base.addOrReplaceChild("armr", CubeListBuilder.create().texOffs(0, 14).addBox(-9.0F, -7.0F, -0.4F, 9.0F, 13.0F, 1.0F, CubeDeformation.NONE)
                .texOffs(0, 28).addBox(-9.6F, -7.0F, -2.4F, 0.0F, 13.0F, 5.0F, CubeDeformation.NONE), PartPose.offset(-1.0F, 1.0F, 0.4F));
        armr.addOrReplaceChild("thin2", CubeListBuilder.create().texOffs(20, 12).addBox(-9.0F, -6.5F, 0.2F, 9.0F, 12.0F, 0.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(1.0F, 0.0F, -0.6F, 0.0F, -0.0873F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override public ModelPart root() { return base; }
    @Override public void setupAnim(Grimoray entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        float flap = Mth.sin(ageInTicks * 0.22F) * 0.22F;
        arml.zRot = -0.12F + flap;
        armr.zRot = 0.12F - flap;
        arml.yRot = 0.08F + flap * 0.35F;
        armr.yRot = -0.08F - flap * 0.35F;
        base.y = 17.0F + Mth.sin(ageInTicks * 0.12F) * 0.8F;
        base.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.15F;
    }
}
