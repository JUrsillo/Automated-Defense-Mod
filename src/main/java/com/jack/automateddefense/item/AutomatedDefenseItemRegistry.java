package com.jack.automateddefense.item;

import com.jack.automateddefense.AutomatedDefense;
import com.jack.automateddefense.entity.AutomatedDefenseEntityRegistry;
import com.jack.automateddefense.item.custom.AutomatedDefenseSpawnEggs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AutomatedDefenseItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AutomatedDefense.MOD_ID);

    public static final RegistryObject<Item> CANNON_SENTRY_SPAWN_EGG = ITEMS.register("cannon_sentry_spawn_egg",
            () -> new AutomatedDefenseSpawnEggs(AutomatedDefenseEntityRegistry.CANNON_SENTRY, 0x464F56, 0x1D6336,
            new Item.Properties().stacksTo(64).tab(ItemGroup.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
