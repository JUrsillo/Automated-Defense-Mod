package com.jack.automateddefense.entity.render;

import com.jack.automateddefense.AutomatedDefense;
import com.jack.automateddefense.entity.custom.AutomatedDefenseCannon;
import com.jack.automateddefense.entity.model.CannonModel;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class CannonRenderer extends MobRenderer<AutomatedDefenseCannon, CannonModel<AutomatedDefenseCannon>> {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(AutomatedDefense.MOD_ID, "assets/automateddefense/textures/entity/cannon_sentry.png");

    public CannonRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new CannonModel<>(), 0.4F);
    }


    @Override
    public ResourceLocation getTextureLocation(AutomatedDefenseCannon pEntity) {
        return TEXTURE;
    }
}
