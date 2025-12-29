package xyz.kohara.etl;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue EMISSIVE_TRIMS;

    public static final ForgeConfigSpec.DoubleValue UNDERGROUND_LIGHT_AMOUNT;

    public static final ForgeConfigSpec.DoubleValue FULL_MOON;
    public static final ForgeConfigSpec.DoubleValue WANING_WAXING_MOON;
    public static final ForgeConfigSpec.DoubleValue QUARTER_MOON;
    public static final ForgeConfigSpec.DoubleValue CRESCENT_MOON;
    public static final ForgeConfigSpec.DoubleValue NEW_MOON;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNED_DIMENSIONS;

    public static final ForgeConfigSpec.BooleanValue ALEXS_CAVES_ENABLE_COLORED_LIGHT;

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

    private static boolean isValidResourceLocation(Object o) {
        if (!(o instanceof String s)) return false;
        return ResourceLocation.isValidResourceLocation(s);
    }

    public static List<ResourceLocation> getBannedDimensions() {
        return BANNED_DIMENSIONS.get().stream()
                .map(ResourceLocation::parse)
                .toList();
    }
}
