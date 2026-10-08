package dev.kubabin.openmap.compat.mixin;

import com.simibubi.create.content.trains.station.StationScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StationScreen.class)
public class CreateMixin {
    @Inject(method = "mapModsPresent", at = @org.spongepowered.asm.mixin.injection.At("RETURN"), cancellable = true)
    private static void mapModsPresent(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
