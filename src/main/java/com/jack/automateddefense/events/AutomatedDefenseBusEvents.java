package com.jack.automateddefense.events;

import com.jack.automateddefense.AutomatedDefense;
import com.jack.automateddefense.entity.AutomatedDefenseEntityRegistry;
import com.jack.automateddefense.entity.custom.AutomatedDefenseCannon;
import com.jack.automateddefense.item.custom.AutomatedDefenseSpawnEggs;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutomatedDefense.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AutomatedDefenseBusEvents {
    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(AutomatedDefenseEntityRegistry.CANNON_SENTRY.get(), AutomatedDefenseCannon.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterEntities(RegistryEvent.Register<EntityType<?>> event) {
        AutomatedDefenseSpawnEggs.initSpawnEggs();
    }
}