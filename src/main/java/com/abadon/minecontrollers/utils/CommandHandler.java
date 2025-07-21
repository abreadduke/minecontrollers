package com.abadon.minecontrollers.utils;

public class CommandHandler {
    public CommandHandler(){}
    public CommandAction getAction(byte command){
        if(CommandAction.values().length <= (command & 0xFF)) return CommandAction.NOP;
        return CommandAction.values()[command];
    }
}
