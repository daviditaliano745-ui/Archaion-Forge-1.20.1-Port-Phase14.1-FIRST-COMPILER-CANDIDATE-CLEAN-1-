package com.ratrod.archaion.client;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Shared 1.20.1 equivalent for Archaion effect/projectile renderers whose
 * original 1.21 implementations intentionally returned shouldRender=false.
 */
public class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public InvisibleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return false;
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }
}
