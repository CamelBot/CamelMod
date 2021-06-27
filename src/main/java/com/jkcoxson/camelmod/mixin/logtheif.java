package com.jkcoxson.camelmod.mixin;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.jkcoxson.camelmod.tcamelp;



@Mixin(MinecraftServer.class)
public class logtheif {




    @Inject(
            method = ("stop"),
            at = {@At("HEAD")},
            cancellable = true
    )
    public void qwer(boolean bl, CallbackInfo ci){
        tcamelp.reconnectpls=false;
        tcamelp.Disconnect();
    }
}
