package xyz.kohara.etl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kohara.etl.Config;
import xyz.kohara.etl.EmbraceTheLight;

@Mixin(LightTexture.class)
public abstract class RecolorLightmapMixin {

	@Unique
	private static int etl$getActualLightLevel(int block, int sky) {
		return Math.max(sky, block);
	}

	@Unique
	private boolean etl$isNight(int time) {
		return time >= 12040 && time < 24000;
	}

	@Unique
	private static final Vector3f etl$warmTint = new Vector3f(0.4F, 0.13F, -0.2F);

	// Deep orange sunset color
	// Base clear-sky sunset (warm pink-gold)
	@Unique
	private static final Vector3f etl$clearSunset = new Vector3f(1.18f, 0.60f, 0.45f);

	// Rainy sunset (cooler, muted)
	@Unique
	private static final Vector3f etl$rainySunset = new Vector3f(0.95f, 0.95f, 1.05f);

	@Inject(
			method = "updateLightTexture",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/LightTexture;clampColor(Lorg/joml/Vector3f;)V",
					shift = At.Shift.BEFORE,
					ordinal = 2
			)
	)
	private void recolorLight(float pPartialTicks,
							  CallbackInfo ci,
							  @Local ClientLevel clientlevel,
							  @Local(ordinal = 7) float nightVisScale,
							  @Local(ordinal = 0) Vector3f skyColor,
							  @Local(ordinal = 1) Vector3f lightColor,
							  @Local(ordinal = 0) int skyLight,
							  @Local(ordinal = 1) int blockLight
	) {

		int time = (int) (clientlevel.getDayTime() % 24000);

		float warmness = blockLight / 15f * // increase w/ blockLight
				(1f - skyColor.x() * (1 - skyLight / 15f)) * // decrease in skyLight w/ dayness
				Math.min((15 - blockLight) / 9f, 1f); // decrease for the 3 highest block light levels

		lightColor.mul(
				1F + etl$warmTint.x() * warmness,
				1F + etl$warmTint.y() * warmness,
				1F + etl$warmTint.z() * warmness
		);

		// Check if block light is 15 so that it doesn't darken GUI elements
		if (blockLight != 15) {
			lightColor.mul(
					lightColor.x() * 0.2F + 0.8F,
					lightColor.y() * 0.2F + 0.8F,
					lightColor.z() * 0.2F + 0.8F
			);
			if (!Config.bannedDimensions.contains(clientlevel.dimension().location())) {

				if (clientlevel.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {

					// Skylight control - sunsets, skyrise, weather events
					if (skyLight > 0) {
						if (etl$isNight(time)) {
							float darkness = Math.max(Math.max(
											blockLight / 15f,
											EmbraceTheLight.getNightDayTransitions(time)
									), nightVisScale
							);
							var moonlight = EmbraceTheLight.getMoonPhaseMultiplier(clientlevel.getMoonPhase());
							var nightDarkness = Mth.clamp(darkness + moonlight, 0F, 1F);
							lightColor.mul(nightDarkness);
						}

						float sunsetFactor = EmbraceTheLight.getSunsetFactor(time);
						if (sunsetFactor > 0.0f) {

							// Blend based on weather
							var rainLevel = clientlevel.getRainLevel(pPartialTicks);
							float sunsetR = Math.fma(etl$rainySunset.x() - etl$clearSunset.x(), rainLevel, etl$clearSunset.x());
							float sunsetG = Math.fma(etl$rainySunset.y() - etl$clearSunset.y(), rainLevel, etl$clearSunset.y());
							float sunsetB = Math.fma(etl$rainySunset.z() - etl$clearSunset.z(), rainLevel, etl$clearSunset.z());

							// Scale by skyLight strength, fade factor and block light
							float topFade = blockLight >= 13 ? (15 - blockLight) / 2f : 1f;
							float intensity = (skyLight / 15f) * sunsetFactor * (1f - blockLight / 15f) * topFade;

							// Interpolate from neutral -> orange
							lightColor.mul(
									Math.fma(sunsetR - 1f, intensity, 1f),
									Math.fma(sunsetG - 1f, intensity, 1f),
									Math.fma(sunsetB - 1f, intensity, 1f)
							);
						}
					}
				}

				// Caves and closed spaces are darker
				if (skyLight != 15) {
					float darkness = Math.max(etl$getActualLightLevel(blockLight, skyLight) / 15f, nightVisScale);
					lightColor.mul(Mth.clamp(darkness + Config.undergroundLightAmount, 0F, 1F));
				}
			}
		}
	}
}
