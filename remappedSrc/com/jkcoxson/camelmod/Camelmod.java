package com.jkcoxson.camelmod;

import net.fabricmc.api.ModInitializer;


public class Camelmod implements ModInitializer {



    @Override
    public void onInitialize() {

        System.out.println("CamelMod initiated, all hail camels o7");
        CommandReg command = new CommandReg();
        command.RegisterCommands();
        tcamelp.Connect("jkcoxson.com",42069);
        tcamelp.startHeartbeat();


    }





}