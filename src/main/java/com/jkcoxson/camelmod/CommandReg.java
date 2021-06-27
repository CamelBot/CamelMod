package com.jkcoxson.camelmod;
// I hate java, I'd just like to let the world know that.
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oroarmor.json.brigadier.JsonToBrigadier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BrigadierArgumentTypes;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.apache.commons.lang3.mutable.MutableObject;
import org.lwjgl.system.CallbackI;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.jkcoxson.camelmod.tcamelp;

public class CommandReg {
    static final MutableObject<MinecraftServer> serverReference = new MutableObject<>();
    static final CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
    public int RegisterCommands(){
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> serverReference.setValue(minecraftServer));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("uncamel").executes(context -> {
                tcamelp.Disconnect();
                return 1;

            }));
            dispatcher.register(CommandManager.literal("register").executes(context -> {
                String toParse = "{\n" +
                        "  \"name\": \"test\",\n" +
                        "  \"argument\" : {\n" +
                        "    \"type\": \"brigadier:literal\"\n" +
                        "  },\n" +
                        "  \"children\": [\n" +
                        "    {\n" +
                        "      \"name\": \"value\",\n" +
                        "      \"argument\": {\n" +
                        "        \"type\": \"brigadier:integer\",\n" +
                        "        \"min\": 0,\n" +
                        "        \"max\": 1\n" +
                        "      },\n" +
                        "      \"executes\": \"com.jkcoxson.camelmod.CommandReg::camelCommand\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
                LiteralArgumentBuilder<ServerCommandSource> builder = (LiteralArgumentBuilder<ServerCommandSource>)JsonToBrigadier.parse(toParse,ServerCommandSource.class);
                dispatcher.register(builder);
                serverReference.getValue().getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                    serverReference.getValue().getCommandManager().sendCommandTree(serverPlayerEntity);
                });
                return 1;
            }));
            dispatcher.register(CommandManager.literal("unregister").executes(context -> {
                CommandRemoval.removeCommand(serverReference.getValue(),"test");
                serverReference.getValue().getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                    serverReference.getValue().getCommandManager().sendCommandTree(serverPlayerEntity);
                });
                return 1;
            }));
            String toParse = "{\n" +
                    "    \"name\": \"camelkey\",\n" +
                    "    \"argument\" : {\n" +
                    "      \"type\": \"brigadier:literal\"\n" +
                    "    },\n" +
                    "    \"requires\":\"com.jkcoxson.camelmod.CommandReg::hasAdmin\",\n" +
                    "    \"children\": [\n" +
                    "      {\n" +
                    "        \"name\": \"value\",\n" +
                    "        \"argument\": {\n" +
                    "          \"type\": \"brigadier:string\"\n" +
                    "        },\n" +
                    "        \"executes\": \"com.jkcoxson.camelmod.CommandReg::saveKey\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "}";
            LiteralArgumentBuilder<ServerCommandSource> builder = (LiteralArgumentBuilder<ServerCommandSource>)JsonToBrigadier.parse(toParse,ServerCommandSource.class);
            dispatcher.register(builder);

        });
        return 0;
    }
    public static int camelCommand(CommandContext ctx){
        System.out.println(ctx.getInput());
        return 1;
    }
    public static boolean hasAdmin(ServerCommandSource source){
        try{
            source.getPlayer();
            return false;
        } catch (Exception e){
            return true;
        }

    }
    public static int saveKey(CommandContext ctx){
        Path dataFolder = FabricLoader.getInstance().getConfigDir().resolve("CamelMod");

        // Read the key
        File keyfile = dataFolder.resolve("key.txt").toFile();

        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(keyfile));
            bw.write(ctx.getInput().split(" ")[1]);
            bw.close();
            tcamelp.Disconnect();
        }catch(Exception e){
            System.out.println(e);
        }
        return 1;
    }
}
