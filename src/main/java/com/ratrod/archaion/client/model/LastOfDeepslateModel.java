package com.ratrod.archaion.client.model;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.SleepingState;
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

/** 1.20.1 backport of Archaion's authored Last of Deepslate geometry. */
public final class LastOfDeepslateModel extends HierarchicalModel<LastOfDeepslate> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("lastofdeepslatemodel"), "main");
    public static final ModelLayerLocation CHARGED_LAYER_LOCATION = new ModelLayerLocation(Archaion.prefix("chargedlastofdeepslate"), "main");
    private final ModelPart root, bod, upperbod, centeredbod, core, armr, arml, legr, legl;

    public LastOfDeepslateModel(ModelPart bakedRoot) {
        this.root = bakedRoot.getChild("root");
        this.bod = root.getChild("bod");
        this.upperbod = bod.getChild("upperbod");
        this.centeredbod = upperbod.getChild("centeredbod");
        this.core = centeredbod.getChild("core");
        this.armr = centeredbod.getChild("armr");
        this.arml = centeredbod.getChild("arml");
        this.legr = root.getChild("legr");
        this.legl = root.getChild("legl");
    }

    public static LayerDefinition createBodyLayer() { return createBodyLayer(CubeDeformation.NONE); }
    public static LayerDefinition createBodyLayer(CubeDeformation deform) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(3.0F, -33.75F, -2.4167F));
        PartDefinition bod = root.addOrReplaceChild("bod", CubeListBuilder.create().texOffs(290, 200).addBox(-19.0F, -14.0F, -10.0F, 38.0F, 15.0F, 20.0F, deform), PartPose.offset(-3.0F, 31.75F, 2.4167F));
        PartDefinition upperbod = bod.addOrReplaceChild("upperbod", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition centeredbod = upperbod.addOrReplaceChild("centeredbod", CubeListBuilder.create().texOffs(130, 0).addBox(-32.0F, -34.0F, -32.0F, 16.0F, 64.0F, 64.0F, deform)
                .texOffs(130, 128).addBox(16.0F, -34.0F, -32.0F, 16.0F, 64.0F, 64.0F, deform)
                .texOffs(290, 0).addBox(-16.0F, 14.0F, -32.0F, 32.0F, 16.0F, 42.0F, deform)
                .texOffs(0, 256).addBox(-16.0F, -34.0F, -32.0F, 32.0F, 16.0F, 64.0F, deform)
                .texOffs(0, 161).addBox(-16.0F, -18.0F, 1.0F, 32.0F, 48.0F, 31.0F, deform)
                .texOffs(292, 335).addBox(-16.0F, -46.0F, -30.0F, 32.0F, 12.0F, 16.0F, deform), PartPose.offset(0.0F, -30.0F, 0.0F));
        centeredbod.addOrReplaceChild("core", CubeListBuilder.create().texOffs(290, 58).addBox(-22.0F, -20.0F, -14.0F, 44.0F, 43.0F, 22.0F, deform), PartPose.offset(0.0F, -3.0F, -22.0F));
        PartDefinition armr = centeredbod.addOrReplaceChild("armr", CubeListBuilder.create().texOffs(192, 256).addBox(-24.0F, -20.0F, -14.0F, 22.0F, 102.0F, 28.0F, deform)
                .texOffs(290, 123).addBox(-37.0F, -4.0F, -14.0F, 13.0F, 49.0F, 28.0F, deform), PartPose.offset(-38.0F, -26.0F, 0.0F));
        armr.addOrReplaceChild("palmr", CubeListBuilder.create(), PartPose.offset(-14.0F, 73.0F, 0.0F));
        PartDefinition arml = centeredbod.addOrReplaceChild("arml", CubeListBuilder.create().texOffs(0, 0).addBox(2.0F, -34.0F, -16.0F, 33.0F, 129.0F, 32.0F, deform), PartPose.offset(38.0F, -26.0F, 0.0F));
        arml.addOrReplaceChild("palml", CubeListBuilder.create(), PartPose.offset(19.0F, 83.0F, 0.0F));
        root.addOrReplaceChild("legr", CubeListBuilder.create().texOffs(292, 235).addBox(-17.0F, -2.0F, -12.0F, 22.0F, 28.0F, 22.0F, deform), PartPose.offset(-20.0F, 31.75F, 2.4167F));
        root.addOrReplaceChild("legl", CubeListBuilder.create().texOffs(292, 235).addBox(-6.0F, -2.0F, -11.0F, 22.0F, 28.0F, 22.0F, deform), PartPose.offset(15.0F, 31.75F, 2.4167F));
        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override public ModelPart root() { return root; }

    @Override
    public void setupAnim(LastOfDeepslate entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        if (entity.getSleepingState() == SleepingState.SLEEPING) {
            upperbod.xRot = 0.10F;
            centeredbod.y = -28.0F + Mth.sin(ageInTicks * 0.035F) * 0.7F;
            core.z = -21.0F + Mth.sin(ageInTicks * 0.04F) * 0.5F;
            armr.zRot = -0.08F;
            arml.zRot = 0.08F;
            return;
        }
        float amount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float phase = limbSwing * 0.75F;
        legr.xRot = Mth.cos(phase) * 0.45F * amount;
        legl.xRot = Mth.cos(phase + Mth.PI) * 0.45F * amount;
        armr.xRot = Mth.cos(phase + Mth.PI) * 0.20F * amount;
        arml.xRot = Mth.cos(phase) * 0.20F * amount;
        centeredbod.yRot = Mth.sin(ageInTicks * 0.035F) * 0.018F;
        core.z = -22.0F + Mth.sin(ageInTicks * 0.06F) * 0.35F;
    }
}
