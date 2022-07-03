package com.jack.automateddefense;

import com.jack.automateddefense.entity.AutomatedDefenseEntityRegistry;
import com.jack.automateddefense.entity.render.CannonRenderer;
import com.jack.automateddefense.item.AutomatedDefenseItemRegistry;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AutomatedDefense.MOD_ID)
public class AutomatedDefense {

    public static final String MOD_ID = "automateddefense";
    private static final Logger LOGGER = LogManager.getLogger();


    public AutomatedDefense() {

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);
        eventBus.addListener(this::setupClient);

        AutomatedDefenseEntityRegistry.register(eventBus);
        AutomatedDefenseItemRegistry.register(eventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setupClient(final FMLClientSetupEvent event) {

        RenderingRegistry.registerEntityRenderingHandler(AutomatedDefenseEntityRegistry.CANNON_SENTRY.get(), CannonRenderer::new);

    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

}
