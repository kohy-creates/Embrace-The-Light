package xyz.kohara.etl;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

//    public static final ForgeConfigSpec.DoubleValue STAR_WIDTH;
//    public static final ForgeConfigSpec.DoubleValue STAR_HEIGHT;

    public static final ForgeConfigSpec.BooleanValue EMISSIVE_TRIMS;

    public static final ForgeConfigSpec.DoubleValue UNDERGROUND_LIGHT_AMOUNT;

    public static final ForgeConfigSpec.DoubleValue FULL_MOON;
    public static final ForgeConfigSpec.DoubleValue WANING_WAXING_MOON;
    public static final ForgeConfigSpec.DoubleValue QUARTER_MOON;
    public static final ForgeConfigSpec.DoubleValue CRESCENT_MOON;
    public static final ForgeConfigSpec.DoubleValue NEW_MOON;

    static {
        BUILDER.comment("\"Embrace The Light!\" Client Config");

        EMISSIVE_TRIMS = BUILDER
                .define("Are trims emissive", true);

        //BUILDER.pop();

//        BUILDER.comment("Stars").push("stars");
//
//        STAR_HEIGHT = BUILDER
//                .defineInRange("Height", 0.25f, 0f, 10f);
//        STAR_WIDTH = BUILDER
//                .defineInRange("Width", 0.25f, 0f, 10f);

//        BUILDER.pop();

        BUILDER.comment("Light amounts").push("light_amounts");

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
}
