package com.jkcoxson.camelmod.mixin;

import com.jkcoxson.camelmod.tcamelp;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(MinecraftServer.class)
public class mixedMessages {

    @Inject(at = @At("HEAD"), method = "sendSystemMessage")
    public void sendMessage(Text text, UUID uUID, CallbackInfo ci) {
        String toSend = text.getString();
        String toYeet = "{\"packet\":\"event\",\"event\":\""+toSend+"\"}";
        tcamelp.Yeet(toYeet);
    }


}