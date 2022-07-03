package com.jack.automateddefense.entity;

import com.jack.automateddefense.AutomatedDefense;
import com.jack.automateddefense.entity.custom.AutomatedDefenseCannon;
import com.jack.automateddefense.entity.custom.AutomatedDefenseCannonball;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AutomatedDefenseEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, AutomatedDefense.MOD_ID);

    public static final RegistryObject<EntityType<AutomatedDefenseCannon>> CANNON_SENTRY =
            ENTITY_TYPES.register("cannon_sentry", () -> EntityType.Builder
                    .of(AutomatedDefenseCannon::new, EntityClassification.MISC)
                    .sized(1F,1F).build(new ResourceLocation
                            (AutomatedDefense.MOD_ID, "cannon_sentry").toString()));

    public static final RegistryObject<EntityType<AutomatedDefenseCannonball>> CANNON_BALL =
            ENTITY_TYPES.register("cannon_ball", () -> EntityType.Builder
                    .of(AutomatedDefenseCannonball::new, EntityClassification.MISC).sized(1.0F, 1.0F)
                    .clientTrackingRange(4).updateInterval(10).build(new ResourceLocation
                            (AutomatedDefense.MOD_ID, "cannon_ball").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
