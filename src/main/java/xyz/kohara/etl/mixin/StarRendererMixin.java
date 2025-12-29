package xyz.kohara.etl.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public class StarRendererMixin {

    @ModifyConstant(method = "drawStars", constant = @Constant(intValue = 1500))
    private int starAmount(int original) {
        return 5000;
    }

    @ModifyConstant(method = "drawStars", constant = @Constant(floatValue = 0.15F))
    private float baseStarSize(float original) {
        return 0.08F;
    }

    @ModifyConstant(method = "drawStars", constant = @Constant(floatValue = 0.1F))
    private float addStarSize(float original) {
        return 0.25F;
    }

//    @Redirect(method = "drawStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 3))
//    private float trigDistribution(RandomSource random) {
//        return random.nextFloat() * random.nextFloat();
//    }
}
