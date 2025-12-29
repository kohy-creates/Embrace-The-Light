package xyz.kohara.etl.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.kohara.etl.Config;

import java.util.function.Supplier;

@Mixin(LightTexture.class)
public abstract class RecolorLigthmapMixin {

    @Unique
    private static final Supplier<Double> etl$undergroundLightAmount = Config.UNDERGROUND_LIGHT_AMOUNT;
    @Unique
    private static final Supplier<Double> etl$fullMoonLightAmount = Config.FULL_MOON;
    @Unique
    private static final Supplier<Double> etl$waningWaxingMoonLightAmount = Config.WANING_WAXING_MOON;
    @Unique
    private static final Supplier<Double> etl$quarterMoonLightAmount = Config.QUARTER_MOON;
    @Unique
    private static final Supplier<Double> etl$crescentMoonLightAmount = Config.CRESCENT_MOON;
    @Unique
    private static final Supplier<Double> etl$newMoonLightAmount = Config.UNDERGROUND_LIGHT_AMOUNT;

    @Shadow
    private static void clampColor(Vector3f pColor) {
    }

    @Unique
    private int etl$lightLevel(int block, int sky) {
        return Math.max(sky, block);
    }

    @Unique
    private float etl$getSunTransition(int timeOfDay) {
        // Sunset: fade from 1.0 → 0.0
        if (timeOfDay >= 12040 && timeOfDay <= 13670) {
            float t = (timeOfDay - 12040) / (13670f - 12040f);
            return 1.0f - t;
        }

        // Sunrise: fade from 0.0 → 1.0
        else if (timeOfDay >= 22331 && timeOfDay <= 23961) {
            return (timeOfDay - 22331) / (23961f - 22331f);
        }

        // Full night
        else if (timeOfDay > 13670 && timeOfDay < 22331) {
            return 0.0f;
        }

        // Full day
        else {
            return 1.0f;
        }
    }

    @Unique
    private boolean etl$isNight(int time) {
        return (time >= 12040 && time <= 23961);
    }

    @Unique
    private Vector3f etl$getMoonPhaseMultiplier(int phase) {
        // Full moon - default
        float col = etl$fullMoonLightAmount.get().floatValue();
        switch (phase) {
            // Waning/Waxing Gibbous
            case 1, 7 -> col = etl$waningWaxingMoonLightAmount.get().floatValue();
            // Quarter
            case 2, 6 -> col = etl$quarterMoonLightAmount.get().floatValue();
            // Crescent
            case 3, 5 -> col = etl$crescentMoonLightAmount.get().floatValue();
            // New
            case 4 -> col = etl$newMoonLightAmount.get().floatValue();
        }
        return new Vector3f(col, col, col);
    }

    @Inject(
            method = "updateLightTexture",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LightTexture;clampColor(Lorg/joml/Vector3f;)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 2),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void recolorLight(float pPartialTicks, //float partialTicks,
                              CallbackInfo ci, // CallbackInfo ci,
                              ClientLevel clientlevel, // ClientLevel clientLevel,
                              float f, // float f,
                              float f1, // float g,
                              float f2, // float h,
                              float f3, // float i,
                              float f4, // float j,
                              float f6, // float l,
                              float f5, // float k,
                              Vector3f vector3f, // Vector3f vector3f,
                              float f7, // float m,
                              Vector3f vector3f1, // Vector3f vector3f2,
                              int i, // int n,
                              int j, // int o,
                              float f8, // float p,
                              float f9, // float q,
                              float f10, // float r,
                              float f11, // float s,
                              boolean flag, // boolean bl,
                              float f14, // float t,
                              Vector3f vector3f5 // Vector3f vector3f5
    ) throws CloneNotSupportedException {

        // Slight remapping so that I don't forget what is what
        int blocklight = j;
        int skylight = i;
        float nightVisScale = f5;

        int time = Math.toIntExact(clientlevel.getDayTime() % 24000);

        Vector3f warmTint = new Vector3f(0.36F, 0.13F, -0.15F);
        float warmness = blocklight / 15f * // increase w/ blocklight
                (1f - vector3f.x() * (1 - skylight / 15f)) * // decrease in skylight w/ dayness
                Math.min((15 - blocklight) / 9f, 1f); // decrease for the 3 highest block light levels
        warmTint.mul(warmness);

        warmTint.add(1f, 1f, 1f);

        vector3f1.mul(warmTint);

        // Change - 'dramatic' factor does not affect GUI elements
        if (blocklight != 15) {
            Vector3f dramaticFactor = (Vector3f) vector3f1.clone();
            dramaticFactor.mul(0.2f);
            dramaticFactor.add(0.8f, 0.8f, 0.8f);
            vector3f1.mul(dramaticFactor);

            if (etl$isNight(time) && clientlevel.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
                Vector3f nightDarkness = new Vector3f(1f, 1f, 1f);
                float darkness = Math.max(Math.max(
                        blocklight / 15f,
                        etl$getSunTransition(time)), nightVisScale);
                nightDarkness.mul(darkness);
                nightDarkness.add(etl$getMoonPhaseMultiplier(clientlevel.getMoonPhase()));
                clampColor(nightDarkness);
                vector3f1.mul(nightDarkness);
            }
        }

        // New - caves and closed spaces are darker
        if (skylight != 15) {
            Vector3f caveDarkness = new Vector3f(1f, 1f, 1f);
            float darkness = Math.max(etl$lightLevel(blocklight, skylight) / 15f, nightVisScale);
            caveDarkness.mul(darkness);
            float undergroundLightAmount = etl$undergroundLightAmount.get().floatValue();
            caveDarkness.add(undergroundLightAmount, undergroundLightAmount, undergroundLightAmount);
            clampColor(caveDarkness);
            vector3f1.mul(caveDarkness);
        }
    }
}
