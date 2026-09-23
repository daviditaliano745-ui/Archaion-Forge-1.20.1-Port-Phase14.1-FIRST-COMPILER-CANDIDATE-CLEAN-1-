package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Brave;
import com.ratrod.archaion.entities.DeepslateSentinel;
import com.ratrod.archaion.entities.Grimoray;
import com.ratrod.archaion.entities.Haunter;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.Slated;
import com.ratrod.archaion.entities.Wight;
import com.ratrod.archaion.entities.effect.LODFallingBlock;
import com.ratrod.archaion.entities.effect.LODSlamEffect;
import com.ratrod.archaion.entities.projectile.GrimoraySpellProjectile;
import com.ratrod.archaion.entities.projectile.EchoStarProjectile;
import com.ratrod.archaion.entities.projectile.LODInterceptBlast;
import com.ratrod.archaion.entities.projectile.ThrownImpactPearl;
import com.ratrod.archaion.entities.projectile.ThrownEchoMace;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ACEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Archaion.MODID);

    public static final RegistryObject<EntityType<Slated>> SLATED = ENTITY_TYPES.register("slated", () ->
            EntityType.Builder.of(Slated::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8)
                    .build(Archaion.MODID + ":slated"));
    public static final RegistryObject<EntityType<Wight>> WIGHT = ENTITY_TYPES.register("wight", () ->
            EntityType.Builder.of(Wight::new, MobCategory.MONSTER).sized(0.6F, 1.99F).clientTrackingRange(8)
                    .build(Archaion.MODID + ":wight"));
    public static final RegistryObject<EntityType<Brave>> BRAVE = ENTITY_TYPES.register("brave", () ->
            EntityType.Builder.of(Brave::new, MobCategory.MONSTER).sized(1.0F, 1.77F).clientTrackingRange(10)
                    .build(Archaion.MODID + ":brave"));
    public static final RegistryObject<EntityType<Grimoray>> GRIMORAY = ENTITY_TYPES.register("grimoray", () ->
            EntityType.Builder.of(Grimoray::new, MobCategory.MONSTER).sized(0.9F, 1.0F).clientTrackingRange(8)
                    .build(Archaion.MODID + ":grimoray"));
    public static final RegistryObject<EntityType<DeepslateSentinel>> DEEPSLATE_SENTINEL = ENTITY_TYPES.register("deepslate_sentinel", () ->
            EntityType.Builder.of(DeepslateSentinel::new, MobCategory.MONSTER).sized(2.75F, 2.6F).clientTrackingRange(10)
                    .build(Archaion.MODID + ":deepslate_sentinel"));
    public static final RegistryObject<EntityType<Haunter>> HAUNTER = ENTITY_TYPES.register("haunter", () ->
            EntityType.Builder.of(Haunter::new, MobCategory.MONSTER).sized(0.8F, 2.6F).clientTrackingRange(10)
                    .build(Archaion.MODID + ":haunter"));
    public static final RegistryObject<EntityType<LastOfDeepslate>> LAST_OF_DEEPSLATE = ENTITY_TYPES.register("last_of_deepslate", () ->
            EntityType.Builder.of(LastOfDeepslate::new, MobCategory.MONSTER).sized(6.0F, 6.5F).clientTrackingRange(12)
                    .build(Archaion.MODID + ":last_of_deepslate"));

    public static final RegistryObject<EntityType<GrimoraySpellProjectile>> GRIMORAY_SPELL = ENTITY_TYPES.register("grimoray_spell", () ->
            EntityType.Builder.<GrimoraySpellProjectile>of(GrimoraySpellProjectile::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.5F, 0.5F).clientTrackingRange(8).build(Archaion.MODID + ":grimoray_spell"));
    public static final RegistryObject<EntityType<ThrownImpactPearl>> THROWN_IMPACT_PEARL = ENTITY_TYPES.register("thrown_impact_pearl", () ->
            EntityType.Builder.<ThrownImpactPearl>of(ThrownImpactPearl::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(8).build(Archaion.MODID + ":thrown_impact_pearl"));
    public static final RegistryObject<EntityType<ThrownEchoMace>> THROWN_ECHO_MACE = ENTITY_TYPES.register("thrown_echo_mace", () ->
            EntityType.Builder.<ThrownEchoMace>of(ThrownEchoMace::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.5F, 0.5F).clientTrackingRange(8).build(Archaion.MODID + ":thrown_echo_mace"));
    public static final RegistryObject<EntityType<EchoStarProjectile>> ECHO_STAR = ENTITY_TYPES.register("echo_star", () ->
            EntityType.Builder.<EchoStarProjectile>of(EchoStarProjectile::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.5F, 0.5F).clientTrackingRange(8).build(Archaion.MODID + ":echo_star"));
    public static final RegistryObject<EntityType<LODInterceptBlast>> LOD_INTERCEPT_BLAST = ENTITY_TYPES.register("lod_intercept_blast", () ->
            EntityType.Builder.<LODInterceptBlast>of(LODInterceptBlast::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.1F, 0.1F).clientTrackingRange(8).build(Archaion.MODID + ":lod_intercept_blast"));
    public static final RegistryObject<EntityType<LODFallingBlock>> LOD_FALLING_BLOCK = ENTITY_TYPES.register("lod_falling_block", () ->
            EntityType.Builder.<LODFallingBlock>of(LODFallingBlock::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.98F, 0.98F).clientTrackingRange(8).build(Archaion.MODID + ":lod_falling_block"));
    public static final RegistryObject<EntityType<LODSlamEffect>> LOD_SLAM = ENTITY_TYPES.register("lod_slam", () ->
            EntityType.Builder.<LODSlamEffect>of(LODSlamEffect::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.1F, 0.1F).clientTrackingRange(8).build(Archaion.MODID + ":lod_slam"));

    private ACEntityTypes() { }
}
