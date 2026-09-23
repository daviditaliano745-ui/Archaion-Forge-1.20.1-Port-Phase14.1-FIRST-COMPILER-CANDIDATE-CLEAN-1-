package com.ratrod.archaion;

import com.ratrod.archaion.entities.Brave;
import com.ratrod.archaion.entities.DeepslateSentinel;
import com.ratrod.archaion.entities.Grimoray;
import com.ratrod.archaion.entities.Haunter;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.Slated;
import com.ratrod.archaion.entities.Wight;
import com.ratrod.archaion.registry.ACEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

public final class ACCommonSetup {
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(ACEntityTypes.SLATED.get(), Slated.createAttributes().build());
        event.put(ACEntityTypes.WIGHT.get(), Wight.createAttributes().build());
        event.put(ACEntityTypes.BRAVE.get(), Brave.createAttributes().build());
        event.put(ACEntityTypes.GRIMORAY.get(), Grimoray.createAttributes().build());
        event.put(ACEntityTypes.DEEPSLATE_SENTINEL.get(), DeepslateSentinel.createAttributes().build());
        event.put(ACEntityTypes.HAUNTER.get(), Haunter.createAttributes().build());
        event.put(ACEntityTypes.LAST_OF_DEEPSLATE.get(), LastOfDeepslate.createAttributes().build());
    }

    private ACCommonSetup() { }
}
