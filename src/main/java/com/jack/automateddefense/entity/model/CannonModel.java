package com.jack.automateddefense.entity.model;

import com.google.common.collect.ImmutableList;
import com.jack.automateddefense.entity.custom.AutomatedDefenseCannon;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class CannonModel <T extends AutomatedDefenseCannon> extends SegmentedModel<T> {

    private final ModelRenderer base;
    private final ModelRenderer lid = new ModelRenderer(64, 64, 0, 0);
    private final ModelRenderer head;

    public CannonModel() {
        super(RenderType::entityCutoutNoCullZOffset);
        this.base = new ModelRenderer(64, 64, 0, 28);
        this.head = new ModelRenderer(64, 64, 0, 52);
        this.lid.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F);
        this.lid.setPos(0.0F, 24.0F, 0.0F);
        this.base.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F);
        this.base.setPos(0.0F, 24.0F, 0.0F);
        this.head.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F);
        this.head.setPos(0.0F, 12.0F, 0.0F);
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        float f = pAgeInTicks - (float)pEntity.tickCount;
        float f1 = (0.5F + pEntity.getClientPeekAmount(f)) * (float)Math.PI;
        float f2 = -1.0F + MathHelper.sin(f1);
        float f3 = 0.0F;
        if (f1 > (float)Math.PI) {
            f3 = MathHelper.sin(pAgeInTicks * 0.1F) * 0.7F;
        }

        this.lid.setPos(0.0F, 16.0F + MathHelper.sin(f1) * 8.0F + f3, 0.0F);
        if (pEntity.getClientPeekAmount(f) > 0.3F) {
            this.lid.yRot = f2 * f2 * f2 * f2 * (float)Math.PI * 0.125F;
        } else {
            this.lid.yRot = 0.0F;
        }

        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
        this.head.yRot = (pEntity.yHeadRot - 180.0F - pEntity.yBodyRot) * ((float)Math.PI / 180F);
    }

    public Iterable<ModelRenderer> parts() {
        return ImmutableList.of(this.base, this.lid);
    }

    public ModelRenderer getBase() {
        return this.base;
    }

    public ModelRenderer getLid() {
        return this.lid;
    }

    public ModelRenderer getHead() {
        return this.head;
    }
}
