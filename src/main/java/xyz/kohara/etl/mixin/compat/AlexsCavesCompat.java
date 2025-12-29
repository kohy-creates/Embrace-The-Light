package xyz.kohara.etl.mixin.compat;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessesCamera;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.DeepsightEffect;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kohara.etl.Config;

@Mixin(LightTexture.class)
public class AlexsCavesCompat {

    @Inject(
            method = {"getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F"},
            remap = true,
            cancellable = true,
            at = @At(value = "TAIL")
    )
    private static void ac_getBrightness(DimensionType dimensionType, int lightTextureIndex, CallbackInfoReturnable<Float> cir) {
        if (Config.ALEXS_CAVES_ENABLE_COLORED_LIGHT.get()) {
            float f = ClientProxy.lastBiomeAmbientLightAmountPrev + (ClientProxy.lastBiomeAmbientLightAmount - ClientProxy.lastBiomeAmbientLightAmountPrev) * Minecraft.getInstance().getFrameTime();
            float primordialBossAmount = AlexsCaves.PROXY.getPrimordialBossActiveAmount(Minecraft.getInstance().getFrameTime());
            if (Minecraft.getInstance().getCameraEntity() instanceof PossessesCamera || Minecraft.getInstance().getCameraEntity() instanceof LivingEntity afflicted && afflicted.hasEffect(ACEffectRegistry.DARKNESS_INCARNATE.get())) {
                f = Math.max(f, 0.35F);
            }
            if (Minecraft.getInstance().player.hasEffect(ACEffectRegistry.DEEPSIGHT.get()) && Minecraft.getInstance().player.isUnderWater()) {
                f = Math.min(1.0F, f + 0.05F * DeepsightEffect.getIntensity(Minecraft.getInstance().player, Minecraft.getInstance().getFrameTime()));
            }
            float light = f + cir.getReturnValue();
            if (primordialBossAmount > 0.0F) {
                cir.setReturnValue(Math.max(0.0F, light - primordialBossAmount * 0.06F));
            } else if (f != 0) {
                cir.setReturnValue(light);
            }
        }
    }

    @Inject(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;effects()Lnet/minecraft/client/renderer/DimensionSpecialEffects;",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void acInject1(float pPartialTicks, CallbackInfo ci, @Local(ordinal = 0) Vector3f vector3f1, @Local ClientLevel clientLevel) {
        if (Config.ALEXS_CAVES_ENABLE_COLORED_LIGHT.get()) {
            this.etl$applyACLightingColors(clientLevel, vector3f1);
        }
    }

    @WrapOperation(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Double;floatValue()F",
                    ordinal = 1
            )
    )
    private float acInject2(Double instance, Operation<Float> original) {
        float f14 = instance.floatValue();
        if (Config.ALEXS_CAVES_ENABLE_COLORED_LIGHT.get()) {
            float biomeAmbientLight = ClientProxy.lastBiomeAmbientLightAmountPrev + (ClientProxy.lastBiomeAmbientLightAmount - ClientProxy.lastBiomeAmbientLightAmountPrev) * Minecraft.getInstance().getFrameTime();
            if (biomeAmbientLight > 0.0F) {
                f14 = Mth.clamp(f14 + biomeAmbientLight, 0.0F, 1.0F);
            }
        }
        return f14;
    }

    @Unique
    private void etl$applyACLightingColors(ClientLevel clientLevel, Vector3f vector3f) {
        if (!clientLevel.effects().forceBrightLightmap()) {
            Vec3 in = new Vec3(vector3f);
            Vec3 to = ClientProxy.lastBiomeLightColorPrev.add(ClientProxy.lastBiomeLightColor.subtract(ClientProxy.lastBiomeLightColorPrev).scale(Minecraft.getInstance().getFrameTime()));
            vector3f.set(to.x * in.x, to.y * in.y, to.z * in.z);
        }
    }
}
