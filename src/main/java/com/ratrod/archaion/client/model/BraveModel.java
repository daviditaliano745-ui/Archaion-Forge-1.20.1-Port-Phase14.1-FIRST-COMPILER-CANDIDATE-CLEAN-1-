package com.ratrod.archaion.client.model;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Brave;
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

/** 1.20.1 backport of Archaion's authored Brave Blockbench model. */
public final class BraveModel extends HierarchicalModel<Brave> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("brave"), "main");
    public static final ModelLayerLocation CHARGED_LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("chargedbrave"), "main");
    private final ModelPart base;
    private final ModelPart head;
    private final ModelPart rods;
    private final ModelPart rod2;
    private final ModelPart rod1;
    private final ModelPart rodgroup;
    private final ModelPart rod3;
    private final ModelPart rod4;
    private final ModelPart ring;

    public BraveModel(ModelPart root) {
        this.base = root.getChild("base");
        this.head = this.base.getChild("head");
        this.rods = this.base.getChild("rods");
        this.rod2 = this.rods.getChild("rod2");
        this.rod1 = this.rods.getChild("rod1");
        this.rodgroup = this.rods.getChild("rodgroup");
        this.rod3 = this.rodgroup.getChild("rod3");
        this.rod4 = this.rodgroup.getChild("rod4");
        this.ring = this.base.getChild("ring");
    }

    public static LayerDefinition createBodyLayer() { return createBodyLayer(CubeDeformation.NONE); }
    public static LayerDefinition createBodyLayer(CubeDeformation deform) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
        base.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 30).addBox(-6.0F, -26.0F, -6.0F, 12.0F, 26.0F, 12.0F, deform), PartPose.offset(0.0F, 18.0F, 0.0F));
        PartDefinition rods = base.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        rods.addOrReplaceChild("rod2", CubeListBuilder.create().texOffs(48, 30).addBox(-2.6F, -5.0F, -2.6F, 5.0F, 10.0F, 5.0F, deform), PartPose.offset(0.0F, -1.0F, 14.6F));
        rods.addOrReplaceChild("rod1", CubeListBuilder.create().texOffs(48, 30).addBox(-2.6F, -5.0F, -2.4F, 5.0F, 10.0F, 5.0F, deform), PartPose.offset(0.0F, -1.0F, -14.6F));
        PartDefinition rodgroup = rods.addOrReplaceChild("rodgroup", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.0F, -0.4F, 0.0F, -1.5708F, 0.0F));
        rodgroup.addOrReplaceChild("rod3", CubeListBuilder.create().texOffs(48, 30).addBox(-2.6F, -5.0F, -2.6F, 5.0F, 10.0F, 5.0F, deform), PartPose.offset(0.0F, 0.0F, 15.0F));
        rodgroup.addOrReplaceChild("rod4", CubeListBuilder.create().texOffs(48, 30).addBox(-2.6F, -5.0F, -2.4F, 5.0F, 10.0F, 5.0F, deform), PartPose.offset(0.0F, 0.0F, -14.2F));
        base.addOrReplaceChild("ring", CubeListBuilder.create().texOffs(1, 0).addBox(-15.0F, 0.0F, -15.0F, 30.0F, 0.0F, 30.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 3.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override public ModelPart root() { return base; }

    @Override
    public void setupAnim(Brave entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        ring.yRot = ageInTicks * 0.2F;
        ring.zRot = Mth.sin(ageInTicks * 0.2F) * 15.0F * Mth.DEG_TO_RAD;
        rods.yRot = -ageInTicks * 0.2F;
        rod1.y = -3.0F + Mth.sin(ageInTicks * 0.1F) * 3.0F;
        rod2.y = -3.0F + Mth.sin((ageInTicks + 20.0F) * 0.1F) * 3.0F;
        rod3.y = -3.0F + Mth.sin((ageInTicks + 40.0F) * 0.1F) * 3.0F;
        rod4.y = -3.0F + Mth.sin((ageInTicks + 80.0F) * 0.1F) * 3.0F;
        float phase = ageInTicks * ((float)Math.PI / 40.0F);
        head.x = 2.0F * Mth.cos(phase);
        head.y = 20.0F + 2.0F * Mth.sin(phase);
        head.zRot = 5.0F * Mth.sin(phase - ((float)Math.PI / 2.0F)) * Mth.DEG_TO_RAD;
    }
}
