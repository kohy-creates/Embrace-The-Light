package xyz.kohara.etl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    /**
     * Changes the width of the stars to simulate old stars.
     */
    @ModifyExpressionValue(
            method = "drawStars",
            at = @At(
                    value = "CONSTANT",
                    args = "floatValue=0.15F"
            )
    )
    private float nt_world_sky$setStarWidth(float width) {
        return 0.25f;
    }

    /**
     * Changes the height of the stars to simulate old stars.
     */
    @ModifyExpressionValue(
            method = "drawStars",
            at = @At(
                    value = "CONSTANT",
                    args = "floatValue=0.1F"
            )
    )
    private float nt_world_sky$setStarHeight(float height) {
        return 0.25f;
    }
}
