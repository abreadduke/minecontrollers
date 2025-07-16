package com.abadon.minecontrollers.utils;

import java.util.Arrays;

public class CommandHandler {
    public CommandHandler(){}
    public CommandAction getAction(byte command){
        return CommandAction.values()[command];
    }
}
