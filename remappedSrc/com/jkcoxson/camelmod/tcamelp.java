package com.jkcoxson.camelmod;

import java.net.*;
import java.io.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oroarmor.json.brigadier.JsonToBrigadier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.realms.util.JsonUtils;
import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.server.network.ServerPlayerEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class tcamelp {
    // initialize socket and input output streams
    public static Socket           socket  = new Socket();
    static DataOutputStream out     = null;
    static BufferedReader in        = null;
    static Path dataFolder;
    static final MutableObject<MinecraftServer> serverReference = new MutableObject<MinecraftServer>();
    public static Boolean camalized = false;
    public static Boolean reconnectpls = true;
    public static Boolean messageShown = false;

    ////////////////////////////////////////
    public static final String version = "4.0.0";
    ////////////////////////////////////////



    // constructor to put ip address and port

    static void Connect(String address, int port)
    {
        try{
            socket.setTcpNoDelay(true);
        }catch (Exception e) {

        }
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> serverReference.setValue(minecraftServer));
        new Thread(new Runnable() {
            public void run() {
                while (reconnectpls){
                    try{
                        Thread.sleep(10000);
                    }catch (Exception e){
                        // ha ha no
                    }

                    // Load up the key and attempt to send it off
                    dataFolder = FabricLoader.getInstance().getConfigDir().resolve("CamelMod");

                    // Create the key file
                    if (!dataFolder.toFile().exists()) {
                        dataFolder.toFile().mkdir();
                        try{
                            File temp = new File(dataFolder.toAbsolutePath().toString()+"/key.txt");
                            temp.createNewFile();
                        }catch (IOException e){
                            System.out.println(e);
                        }catch (Exception e){
                            System.out.println(e);
                        }
                    }
                    // Read the key
                    File keyfile = dataFolder.resolve("key.txt").toFile();
                    String key = "";

                    try{
                        BufferedReader br = new BufferedReader(new FileReader(keyfile));
                        key = br.readLine();
                    }catch(Exception e){

                    }



                    // Big brain connection
                    System.out.println("Connecting to CamelBot");
                    try {
                        socket = new Socket("jkcoxson.com",port);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new DataOutputStream(socket.getOutputStream());
                        Yeet("{\"packet\":\"key\",\n\"key\":\""+key+"\",\n\"version\":\""+version+"\"}");
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    Boolean go = true;
                    while (go) {
                        try {
                            String response = in.readLine();
                            if (response==null){
                                go=false;
                                System.out.println("CamelBot closed the connection");
                                Disconnect();
                            }else{

                                // Start parse JSON pls
                                try{
                                    if(response.startsWith("s")){
                                        response = response.substring(1);
                                    }
                                    Gson gson = new Gson();
                                    // How the heck does this work? Thanks internet, Java is the worst
                                    JsonObject packet = gson.fromJson(response, JsonObject.class);
                                    String packetType = packet.get("packet").getAsString(); // Yeah, that makes sense NOT.
                                    if (packetType.equals("status")){
                                        System.out.println(packet.get("message"));
                                        if(packet.get("message").getAsString().equals("Connection verified")){
                                            camalized = true;
                                            serverReference.getValue().getCommandManager().execute(serverReference.getValue().getCommandSource(), "/tellraw @a {\"text\":\"CamelBot is connected!\",\"color\":\"blue\"}");
                                            messageShown = false;
                                        }
                                    }
                                    if (packetType.equals("command")){
                                        serverReference.getValue().getCommandManager().execute(serverReference.getValue().getCommandSource(), "/"+packet.get("command").getAsString());
                                    }
                                    if (packetType.equals("players")){
                                        List<ServerPlayerEntity> players = serverReference.getValue().getPlayerManager().getPlayerList();
                                        List<String> playernames = new ArrayList<String>();
                                        for (int i = 0; i<players.size(); ++i){
                                            playernames.add(players.get(i).getEntityName().toString().replace("\"",""));
                                        }
                                        String playerjson = new Gson().toJson(playernames);
                                        JsonObject toSend = new JsonObject();
                                        toSend.addProperty("packet","players");
                                        toSend.addProperty("players",playerjson);
                                        Yeet(toSend.toString());
                                    }
                                    if (packetType.equals("whitelist-add")){
                                        serverReference.getValue().getCommandManager().execute(serverReference.getValue().getCommandSource(), "/whitelist add"+packet.get("player").getAsString());
                                    }
                                    if (packetType.equals("whitelist-remove")){
                                        serverReference.getValue().getCommandManager().execute(serverReference.getValue().getCommandSource(), "/whitelist remove"+packet.get("player").getAsString());
                                    }
                                    if (packetType.equals("whitelist-list")){
                                        String[] whitelist = serverReference.getValue().getPlayerManager().getWhitelist().getNames();
                                        List<String> listwhitelist = new ArrayList<String>();
                                        for (int i = 0; i <whitelist.length; ++i){
                                            listwhitelist.add(whitelist[i].replace("\"",""));
                                        }
                                        String whitelistjson = new Gson().toJson(listwhitelist);
                                        JsonObject toSend = new JsonObject();
                                        toSend.addProperty("packet","whitelist");
                                        toSend.addProperty("whitelist",whitelistjson);
                                        Yeet(toSend.toString());
                                    }
                                    if (packetType.equals("coords")){
                                        String player = packet.get("player").getAsString();
                                        String coords = "";
                                        coords += serverReference.getValue().getPlayerManager().getPlayer(player).getX();
                                        coords += ",";
                                        coords += serverReference.getValue().getPlayerManager().getPlayer(player).getY();
                                        coords += ",";
                                        coords += serverReference.getValue().getPlayerManager().getPlayer(player).getZ();
                                        JsonObject toSend = new JsonObject();
                                        Boolean overworld = serverReference.getValue().getPlayerManager().getPlayer(player).getEntityWorld().getDimension().isBedWorking();
                                        Boolean nether = serverReference.getValue().getPlayerManager().getPlayer(player).getEntityWorld().getDimension().isPiglinSafe();
                                        Boolean end = serverReference.getValue().getPlayerManager().getPlayer(player).getEntityWorld().getDimension().hasEnderDragonFight();
                                        if(overworld){
                                            toSend.addProperty("dimension","overworld");
                                        }
                                        if(nether){
                                            toSend.addProperty("dimension","nether");
                                        }
                                        if(end){
                                            toSend.addProperty("dimension","end");
                                        }
                                        toSend.addProperty("packet","coords");
                                        toSend.addProperty("player",player);
                                        toSend.addProperty("coords",coords);
                                        Yeet(toSend.toString());
                                    }
                                    if (packetType.equals("register")){
                                        LiteralArgumentBuilder<ServerCommandSource> builder = (LiteralArgumentBuilder<ServerCommandSource>) JsonToBrigadier.parse(packet.get("command").toString(),ServerCommandSource.class);
                                        serverReference.getValue().getCommandManager().getDispatcher().register(builder);
                                        serverReference.getValue().getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                                            serverReference.getValue().getCommandManager().sendCommandTree(serverPlayerEntity);
                                        });
                                        serverReference.getValue().getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                                            serverReference.getValue().getCommandManager().sendCommandTree(serverPlayerEntity);
                                        });
                                    }
                                    if (packetType.equals("unregister")){
                                        try{
                                            CommandRemoval.removeCommand(serverReference.getValue(),packet.get("command").getAsString());
                                            serverReference.getValue().getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                                                serverReference.getValue().getCommandManager().sendCommandTree(serverPlayerEntity);
                                            });
                                        }catch (Exception e){
                                            System.out.println("Couldn't remove the command: "+e);
                                        }
                                    }
                                    if (packetType.equals("ready")){
                                        JsonObject toSend = new JsonObject();
                                        toSend.addProperty("packet","ready");
                                        try{
                                            toSend.addProperty("ready",serverReference.getValue().isRunning());
                                        }catch (Exception e){
                                            toSend.addProperty("ready",false);
                                        }
                                        Yeet(toSend.toString());
                                    }

                                }catch(Exception e){
                                    System.out.println(e);
                                }
                            }
                        } catch (NullPointerException j) {

                        } catch (Exception e){
                            go = false;
                            System.out.println(e);

                            Disconnect();
                        }

                    }

                }

                Thread.currentThread().interrupt();

            }
        }).start();

    }

    public static void Disconnect(){
        if (!reconnectpls){
            System.out.println("Closing all sockets to CamelBot");
        }
        if(!messageShown){

            serverReference.getValue().getCommandManager().execute(serverReference.getValue().getCommandSource(), "/tellraw @a {\"text\":\"Lost connection to CamelBot\",\"color\":\"red\"}");
            messageShown=true;

        }
        camalized=false;
        try
        {
            out.close();
        }
        catch(IOException i)
        {
            //System.out.println(i);
        }
        try
        {
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        try
        {
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }


    public static void Yeet(String input){
        if (socket.isConnected()){
            try {
                out.writeUTF(input+"endmessage");
                out.flush();
            }catch (Exception e){
                System.out.println(e);
            }
        }
    }
    public static void startHeartbeat(){
        Boolean readyPacketSent = false;
        new Thread(new Runnable() {
            public void run() {
                while(reconnectpls){

                    if(camalized){
                        Yeet("{\"packet\":\"heartbeat\"}");
                    }
                    try {
                        Thread.sleep(5000);
                    }catch (Exception e){
                        System.out.println(e);
                    }

                }

            }
        }).start();
    }

}
