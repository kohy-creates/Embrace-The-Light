package xyz.kohara.etl;

import com.mojang.logging.LogUtils;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

@Mod(EmbraceTheLight.MOD_ID)
public class EmbraceTheLight {

    public static final String MOD_ID = "etl";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EmbraceTheLight() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, MOD_ID + "-client.toml");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Those are for day/night transitions
    public static float getNightDayTransitions(int timeOfDay) {
        // Sunset: fade from 1.0 -> 0.0
        if (timeOfDay >= 12040 && timeOfDay <= 13670) {
            float t = (timeOfDay - 12040) / (13670f - 12040f);
            return 1.0f - t;
        }

        // Sunrise: fade from 0.0 -> 1.0
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

    // Those are for sunsets and sunrises
    public static float getSunTransition(int timeOfDay) {
        timeOfDay = timeOfDay % 24000;

        // Sunset: 11500 -> 13400
        if (timeOfDay >= 12000 && timeOfDay <= 13400) {
            return (timeOfDay - 12000) / (13400f - 12000);
        }

        // Sunrise: 22400 -> 150 (wrap-around)
        if (timeOfDay >= 22400 || timeOfDay <= 150) {
            float t;
            if (timeOfDay >= 22400) {
                // 22400 -> 24000 (1600 ticks)
                t = (timeOfDay - 22400f) / 1750f;
            } else {
                // 0 -> 150 (continue fade, 150 ticks)
                t = (timeOfDay + 1600f) / 1750f;
            }
            return 1.0f - t;
        }

        // Night or day: no transition
        return 0.0f;
    }

    public static float getSunsetFactor(int time) {
        return 1.0f - Math.abs(2.0f * getSunTransition(time) - 1.0f);
    }

    public static Vector3f getMoonPhaseMultiplier(int phase) {
        // Full moon - default
        float col = Config.FULL_MOON.get().floatValue();
        switch (phase) {
            // Waning/Waxing Gibbous
            case 1, 7 -> col = Config.WANING_WAXING_MOON.get().floatValue();
            // Quarter
            case 2, 6 -> col = Config.QUARTER_MOON.get().floatValue();
            // Crescent
            case 3, 5 -> col = Config.CRESCENT_MOON.get().floatValue();
            // New
            case 4 -> col = Config.NEW_MOON.get().floatValue();
        }
        return new Vector3f(col, col, col);
    }

}
