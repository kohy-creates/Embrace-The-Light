package xyz.kohara.etl.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.kohara.etl.Config;

@Mixin(HumanoidArmorLayer.class)
public class TrimRendererMixin {

    @Inject(
            method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void makeTrimsEmissive(
            ArmorMaterial pArmorMaterial,
            PoseStack pPoseStack,
            MultiBufferSource pBuffer,
            int pPackedLight,
            ArmorTrim pTrim,
            Model pModel,
            boolean pInnerTexture,
            CallbackInfo ci,
            TextureAtlasSprite textureatlassprite, //local
            VertexConsumer vertexconsumer //local
    ) {
        ci.cancel();
        int lightAmount = (Config.EMISSIVE_TRIMS.get()) ? 16777215 : pPackedLight;
        pModel.renderToBuffer(pPoseStack, vertexconsumer,lightAmount, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
