package xyz.kohara.etl;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = EmbraceTheLight.MOD_ID)
public class Config {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	private static final ForgeConfigSpec.BooleanValue EMISSIVE_TRIMS;

	private static final ForgeConfigSpec.DoubleValue UNDERGROUND_LIGHT_AMOUNT;

	private static final ForgeConfigSpec.DoubleValue FULL_MOON;
	private static final ForgeConfigSpec.DoubleValue WANING_WAXING_MOON;
	private static final ForgeConfigSpec.DoubleValue QUARTER_MOON;
	private static final ForgeConfigSpec.DoubleValue CRESCENT_MOON;
	private static final ForgeConfigSpec.DoubleValue NEW_MOON;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNED_DIMENSIONS;

	private static final ForgeConfigSpec.BooleanValue ALEXS_CAVES_ENABLE_COLORED_LIGHT;

	static {
		BUILDER.comment("\"Embrace The Light!\" Client Config");

		EMISSIVE_TRIMS = BUILDER
				.define("Are trims emissive", true);

		ALEXS_CAVES_ENABLE_COLORED_LIGHT = BUILDER
				.comment("Whether to enable Alex's Caves colored light in cave bomes")
				.define("Alex's Caves Compat", true);

		BUILDER.comment("Light amounts").push("light_amounts");

		BANNED_DIMENSIONS = BUILDER
				.comment("List of dimensions where ETL will not work in")
				.comment("Band aid to fix e.g. Aether compatibility")
				.defineListAllowEmpty(
						"Banned Dimensions",
						List.of("aether:the_aether", "the_bumblezone:the_bumblezone"),
						Config::isValidResourceLocation
				);

		UNDERGROUND_LIGHT_AMOUNT = BUILDER
				.defineInRange("Underground", 0.5f, 0f, 1f);

		BUILDER.push("night");

		FULL_MOON = BUILDER
				.defineInRange("Full moon", 1f, 0f, 1f);

		WANING_WAXING_MOON = BUILDER
				.defineInRange("Wanin/Waxing moon", 0.8f, 0f, 1f);

		QUARTER_MOON = BUILDER
				.defineInRange("Quarter moon", 0.6f, 0f, 1f);

		CRESCENT_MOON = BUILDER
				.defineInRange("Crescent moon", 0.4f, 0f, 1f);

		NEW_MOON = BUILDER
				.defineInRange("New moon", 0.2f, 0f, 1f);

		BUILDER.pop(2);

		SPEC = BUILDER.build();
	}

	public static boolean emissiveTrims;
	public static boolean alexsCavesColoredLight;

	public static float undergroundLightAmount;

	public static float fullMoon;
	public static float waningWaxingMoon;
	public static float quarterMoon;
	public static float crescentMoon;
	public static float newMoon;

	public static Set<ResourceLocation> bannedDimensions = new HashSet<>();

	public static void updateConfig() {
		System.out.println("Updating Embrace The Light client config...");

		emissiveTrims = EMISSIVE_TRIMS.get();
		alexsCavesColoredLight = ALEXS_CAVES_ENABLE_COLORED_LIGHT.get();

		undergroundLightAmount = UNDERGROUND_LIGHT_AMOUNT.get().floatValue();

		fullMoon = FULL_MOON.get().floatValue();
		waningWaxingMoon = WANING_WAXING_MOON.get().floatValue();
		quarterMoon = QUARTER_MOON.get().floatValue();
		crescentMoon = CRESCENT_MOON.get().floatValue();
		newMoon = NEW_MOON.get().floatValue();

		bannedDimensions = BANNED_DIMENSIONS.get()
				.stream()
				.map(ResourceLocation::parse)
				.collect(Collectors.toUnmodifiableSet());
	}

	private static boolean isValidResourceLocation(Object o) {
		if (!(o instanceof String s)) return false;
		return ResourceLocation.isValidResourceLocation(s);
	}

	@SubscribeEvent
	static void onLoad(ModConfigEvent.Loading event) {
		updateConfig();
	}

	@SubscribeEvent
	static void onReload(ModConfigEvent.Reloading event) {
		updateConfig();
	}
}
