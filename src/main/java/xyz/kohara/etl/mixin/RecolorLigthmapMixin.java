package xyz.kohara.etl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
import xyz.kohara.etl.Config;
import xyz.kohara.etl.EmbraceTheLight;

@Mixin(LightTexture.class)
public abstract class RecolorLigthmapMixin {

    @Shadow
    private static void clampColor(Vector3f pColor) {
    }

    @Unique
    private int etl$getActualLightLevel(int block, int sky) {
        return Math.max(sky, block);
    }

    @Unique
    private boolean etl$isNight(int time) {
        return (time >= 12040 && time <= 23961);
    }

    @Inject(
            method = "updateLightTexture",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LightTexture;clampColor(Lorg/joml/Vector3f;)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 2)
    )
    private void recolorLight(float pPartialTicks,
                              CallbackInfo ci,
                              @Local ClientLevel clientlevel,
                              @Local(ordinal = 7) float nightVisScale,
                              @Local(ordinal = 0) Vector3f skyColor,
                              @Local(ordinal = 1) Vector3f lightColor,
                              @Local(ordinal = 0) int skyLight,
                              @Local(ordinal = 1) int blockLight
    ) throws CloneNotSupportedException {

        int time = (int) (clientlevel.getDayTime() % 24000);

        Vector3f warmTint = new Vector3f(0.4F, 0.13F, -0.2F);
        float warmness = blockLight / 15f * // increase w/ blockLight
                (1f - skyColor.x() * (1 - skyLight / 15f)) * // decrease in skyLight w/ dayness
                Math.min((15 - blockLight) / 9f, 1f); // decrease for the 3 highest block light levels
        warmTint.mul(warmness);

        warmTint.add(1f, 1f, 1f);

        lightColor.mul(warmTint);

        // Check if block light is 15 so that it doesn't darken GUI elements
        if (blockLight != 15) {
            Vector3f dramaticFactor = (Vector3f) lightColor.clone();
            dramaticFactor.mul(0.2f);
            dramaticFactor.add(0.8f, 0.8f, 0.8f);
            lightColor.mul(dramaticFactor);
            if (!Config.getBannedDimensions().contains(clientlevel.dimension().location())) {

                if (clientlevel.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {

                    // Skylight control - sunsets, skyrise, weather events
                    if (skyLight > 0) {
                        if (etl$isNight(time)) {
                            Vector3f nightDarkness = new Vector3f(1f, 1f, 1f);
                            float darkness = Math.max(Math.max(
                                            blockLight / 15f,
                                            EmbraceTheLight.getNightDayTransitions(time)
                                    ), nightVisScale
                            );
                            nightDarkness.mul(darkness);
                            nightDarkness.add(EmbraceTheLight.getMoonPhaseMultiplier(clientlevel.getMoonPhase()));
                            clampColor(nightDarkness);
                            lightColor.mul(nightDarkness);
                        }

                        float sunsetFactor = EmbraceTheLight.getSunsetFactor(time);
                        if (sunsetFactor > 0.0f) {
                            // Deep orange sunset color
                            // Base clear-sky sunset (warm pink-gold)
                            Vector3f clearSunset = new Vector3f(1.18f, 0.60f, 0.45f);

                            // Rainy sunset (cooler, muted)
                            Vector3f rainySunset = new Vector3f(0.95f, 0.95f, 1.05f);

                            // Blend based on weather
                            Vector3f sunsetColor = new Vector3f(clearSunset);
                            sunsetColor.lerp(rainySunset, clientlevel.getRainLevel(pPartialTicks));

                            // Scale by skyLight strength, fade factor and block light
                            float topFade = blockLight >= 13 ? (15 - blockLight) / 2f : 1f;
                            float intensity = (skyLight / 15f) * sunsetFactor * (1f - blockLight / 15f) * topFade;


                            // Interpolate from neutral -> orange
                            Vector3f sunsetTint = new Vector3f(1f, 1f, 1f);
                            sunsetTint.lerp(sunsetColor, intensity);

                            lightColor.mul(sunsetTint);
                        }
                    }
                }

                // New - caves and closed spaces are darker
                if (skyLight != 15) {
                    Vector3f caveDarkness = new Vector3f(1f, 1f, 1f);
                    float darkness = Math.max(etl$getActualLightLevel(blockLight, skyLight) / 15f, nightVisScale);
                    caveDarkness.mul(darkness);
                    float undergroundLightAmount = Config.UNDERGROUND_LIGHT_AMOUNT.get().floatValue();
                    caveDarkness.add(undergroundLightAmount, undergroundLightAmount, undergroundLightAmount);
                    clampColor(caveDarkness);
                    lightColor.mul(caveDarkness);
                }
            }
        }
    }
}
