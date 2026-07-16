package xyz.kohara.etl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
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

    @WrapOperation(method = "drawStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 3))
    private float trigDistribution(RandomSource instance, Operation<Float> original) {
        var value = original.call(instance);
        return value * value;
    }
}
