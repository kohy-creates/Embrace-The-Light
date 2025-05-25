package xyz.kohara.lightmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// From https://www.curseforge.com/minecraft/mc-mods/better-night-vision
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyReturnValue(
            method = "getNightVisionScale",
            at = @At("RETURN")
    )
    private static float cleanerNightVision(float original) {
        assert Minecraft.getInstance().player != null;
        MobEffectInstance statusEffectInstance = Minecraft.getInstance().player.getEffect(MobEffects.NIGHT_VISION);
        assert statusEffectInstance != null;
        return !statusEffectInstance.endsWithin(200) ? 1.0F : (float) statusEffectInstance.getDuration() / 200F;
    }
}
