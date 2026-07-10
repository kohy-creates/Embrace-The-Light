package xyz.kohara.etl.mixin.compat;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kohara.etl.Config;

@Mixin(LightTexture.class)
public class AlexsCavesCompat {

    @Inject(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;adjustLightmapColors(Lnet/minecraft/client/multiplayer/ClientLevel;FFFFIILorg/joml/Vector3f;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void insertACCode1(float pPartialTicks, CallbackInfo ci, @Local ClientLevel clientLevel, @Local(ordinal = 1) Vector3f vector3f1) {
        if (Config.alexsCavesColoredLight) {
            etl$applyACLightingColors(clientLevel, vector3f1);
        }
    }

    @ModifyExpressionValue(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Double;floatValue()F",
                    ordinal = 1
            )
    )
    private float insertACCode2(float original) {
        if (Config.alexsCavesColoredLight) {
            float biomeAmbientLight = ClientProxy.lastBiomeAmbientLightAmountPrev + (ClientProxy.lastBiomeAmbientLightAmount - ClientProxy.lastBiomeAmbientLightAmountPrev) * Minecraft.getInstance().getFrameTime();
            if (biomeAmbientLight > 0.0F) {
                original = Mth.clamp(original + biomeAmbientLight, 0.0F, 1.0F);
            }
        }
        return original;
    }

    // Taken straight from AC
    @Unique
    private void etl$applyACLightingColors(ClientLevel clientLevel, Vector3f vector3f) {
        if (!clientLevel.effects().forceBrightLightmap()) {
            Vec3 in = new Vec3(vector3f);
            Vec3 to = ClientProxy.lastBiomeLightColorPrev.add(ClientProxy.lastBiomeLightColor.subtract(ClientProxy.lastBiomeLightColorPrev).scale(Minecraft.getInstance().getFrameTime()));
            vector3f.set(to.x * in.x, to.y * in.y, to.z * in.z);
        }
    }
}
