package com.jack.automateddefense.events;

import com.jack.automateddefense.AutomatedDefense;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutomatedDefense.MOD_ID)
public class AutomatedDefenseEvents {

    @SubscribeEvent
    public static void onPlayerCloneEvent(PlayerEvent.Clone event) {
        if(!event.getOriginal().getEntity().level.isClientSide()) {
            event.getPlayer().getPersistentData().putIntArray(AutomatedDefense.MOD_ID + "homepos",
                    event.getOriginal().getPersistentData().getIntArray(AutomatedDefense.MOD_ID + "homepos"));
        }
    }
}