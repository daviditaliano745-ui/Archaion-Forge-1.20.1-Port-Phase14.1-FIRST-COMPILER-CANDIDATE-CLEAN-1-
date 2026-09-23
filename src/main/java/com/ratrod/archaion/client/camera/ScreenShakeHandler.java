package com.ratrod.archaion.client.camera;

import com.ratrod.archaion.Archaion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Client camera shake backport of Archaion's local shake helper. */
@Mod.EventBusSubscriber(modid = Archaion.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ScreenShakeHandler {
    private static final List<Instance> SHAKES = new ArrayList<>();

    public static void shakeLocal(float intensity, int duration, float frequency) {
        if (intensity <= 0.0F || duration <= 0) return;
        SHAKES.add(new Instance(intensity, duration, frequency));
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<Instance> iterator = SHAKES.iterator();
        while (iterator.hasNext()) {
            Instance shake = iterator.next();
            if (shake.isDone()) iterator.remove();
            else shake.tick();
        }
    }

    @SubscribeEvent
    public static void modifyCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        for (Instance shake : SHAKES) {
            float[] offsets = shake.getOffsets(event.getPartialTick());
            event.setPitch(event.getPitch() + offsets[0]);
            event.setYaw(event.getYaw() + offsets[1]);
        }
    }

    private static final class Instance {
        private final float intensity;
        private final float maxDuration;
        private final float frequency;
        private float duration;

        private Instance(float intensity, int duration, float frequency) {
            this.intensity = intensity;
            this.duration = duration;
            this.maxDuration = duration;
            this.frequency = frequency;
        }

        private void tick() { duration = Math.max(0.0F, duration - 1.0F); }
        private boolean isDone() { return duration <= 0.0F; }

        private float[] getOffsets(double partialTick) {
            float t = (maxDuration - duration) + (float) partialTick;
            float fade = maxDuration <= 0.0F ? 0.0F : duration / maxDuration;
            float pitch = (float) Math.sin(t * frequency * 0.1F) * intensity * fade;
            float yaw = (float) Math.cos(t * frequency * 0.1F + Math.PI / 4.0D) * intensity * fade;
            return new float[]{pitch, yaw};
        }
    }

    private ScreenShakeHandler() { }
}
