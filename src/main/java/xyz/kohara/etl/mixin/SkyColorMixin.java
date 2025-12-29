package xyz.kohara.etl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kohara.etl.EmbraceTheLight;

@Mixin(ClientLevel.class)
public class SkyColorMixin {

    @WrapOperation(
            method = "getSkyDarken",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"
            )
    )
    private float increaseRainDarkness(ClientLevel instance, float v, Operation<Float> original) {
        float or = instance.getRainLevel(v);
        return or * 1.5f;
    }

    @WrapOperation(
            method = "getSkyDarken",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F"
            )
    )
    private float increaseThunderDarkness(ClientLevel instance, float v, Operation<Float> original) {
        float or = instance.getThunderLevel(v);
        return or * 1.5f;
    }

    // Adapted from https://modrinth.com/mod/subtle-skybox
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void subtleSkyboxChangeColor(Vec3 originalColor, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        ClientLevel clientLevel = (ClientLevel) (Object) this;

        if (clientLevel.dimension() != Level.OVERWORLD) return;

        Vec3 color = cir.getReturnValue();

        int time = (int) (clientLevel.dayTime() % 24000L);
        double delta = (time + 2000) / 24000.0;
        double lerp = Mth.clampedMap(delta, 0.5, 0.55, 0, 1);
        lerp *= Mth.clampedMap(delta, 0.95, 1, 1, 0);

        double red = color.x;
        double green = color.y;
        double blue = color.z;

        if (delta > 0.5) {
            Vector3f skyColorMix = new Vector3f(4f, 0f, 18f)
                    .mul(1f / 255f);
            skyColorMix.mul(EmbraceTheLight.getMoonPhaseMultiplier(clientLevel.getMoonPhase()));
            red += skyColorMix.x * lerp;
            green += skyColorMix.y * lerp;
            blue += skyColorMix.z * lerp;
        }

        float sunsetFactor = EmbraceTheLight.getSunsetFactor(time);
        if (sunsetFactor > 0) {

            Vector3f clearSunset = new Vector3f(1.18f, 0.60f, 0.45f);
            Vector3f rainySunset = new Vector3f(0.95f, 0.95f, 1.05f);
            // Blend based on weather
            Vector3f sunsetColor = new Vector3f(clearSunset);
            sunsetColor.lerp(rainySunset, clientLevel.getRainLevel(partialTicks));

            float intensity = sunsetFactor * 0.35f;


            // Interpolate from neutral -> orange
            Vector3f neutral = new Vector3f(1f, 1f, 1f);
            Vector3f sunsetTint = new Vector3f(neutral);
            sunsetTint.lerp(sunsetColor, intensity);

            // Apply only the delta
            red += sunsetTint.x - 1f;
            green += sunsetTint.y - 1f;
            blue += sunsetTint.z - 1f;

        }

        cir.setReturnValue(new Vec3(red, green, blue));
    }

}
