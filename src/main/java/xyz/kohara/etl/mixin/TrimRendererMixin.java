package xyz.kohara.etl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.kohara.etl.Config;

@Mixin(HumanoidArmorLayer.class)
public class TrimRendererMixin {

    @WrapOperation(
            method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"
            )
    )
    private void fullBrightTrims(
            Model instance,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int pPackedLight,
            int overlayTexture,
            float pRed, float pBlue, float pGreen, float pAlpha,
            Operation<Void> original
    ) {
        int lightAmount = (Config.emissiveTrims) ? 0xF000F0 : pPackedLight;
        original.call(instance, poseStack, vertexConsumer, lightAmount, overlayTexture, pRed, pBlue, pGreen, pAlpha);
    }
}
