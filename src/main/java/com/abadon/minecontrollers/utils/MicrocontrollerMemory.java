package com.abadon.minecontrollers.utils;

import java.util.HashMap;
import java.util.HashSet;

public class MicrocontrollerMemory {
    protected HashMap<Short, Byte> memory = new HashMap<>();
    protected HashSet<Short> usedAddresses = new HashSet<>();
    public HashSet<Short> getUsedAddresses(){
        return usedAddresses;
    }
    protected boolean checkForZeroValue(Byte value){
        return value == 0;
    }
    public byte[] getMemorySequence(short from, short to){
        byte[] sequence = new byte[to];
        for(short i = from; i < from + to; i++){
            sequence[i - from] = readValue(i);
        }
        return sequence;
    }
    public boolean setValue(Short address, Byte value){
        if(!memory.containsKey(address)){
            if(checkForZeroValue(value)) return false;
            memory.put(address, value);
            usedAddresses.add(address);
            return true;
        }
        else if(!memory.get(address).equals(value)){
            memory.remove(address);
            usedAddresses.remove(address);
            if(checkForZeroValue(value)) return true;
            memory.put(address, value);
            usedAddresses.add(address);
            return true;
        }
        else{
            return false;
        }
    }
    public boolean setValue(Short address, byte value){
        return setValue(address, Byte.valueOf(value));
    }
    public byte readValue(Short address){
        //LogUtils.getLogger().info("checking key - " + String.valueOf(address));
        if(memory.containsKey(address)){
            //LogUtils.getLogger().info("Success");
            return memory.get(address);
        }else{
            return 0;
        }
    }
}
