package com.jkcoxson.camelmod.mixin;

import com.google.gson.JsonObject;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.jkcoxson.camelmod.tcamelp;

@Mixin(ServerPlayNetworkHandler.class)
public class mixedchat {

    @Shadow public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    @Inject(
            method = ("handleMessage"),
            at = {@At("HEAD")},
            cancellable = true
    )
    public void asdf(TextStream.Message packet, CallbackInfo ci) {
        String message = packet.getRaw();
        if (!message.startsWith("/")){
            if (tcamelp.camalized){
                try{
                    message = message.replaceAll("\"","''");
                    JsonObject toJson = new JsonObject();
                    toJson.addProperty("packet","chat");
                    toJson.addProperty("sender",this.player.getEntityName());
                    toJson.addProperty("message",message);
                    if(player.getScoreboardTeam()!=null){
                        String teamColor = ((TeamAccessor)player.getScoreboardTeam()).getColor().getName();
                        toJson.addProperty("color",teamColor);
                    }

                    tcamelp.Yeet(toJson.toString());
                    ci.cancel();
                }catch (Exception e){
                    System.out.println(e);
                }

            }else {

            }
        }else {

        }

    }


}
