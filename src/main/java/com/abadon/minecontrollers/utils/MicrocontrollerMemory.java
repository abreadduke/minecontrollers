package com.abadon.minecontrollers.utils;

import java.util.HashMap;
import java.util.HashSet;

public class MicrocontrollerMemory {
    protected HashMap<Integer, Byte> memory = new HashMap<>();
    protected HashSet<Integer> usedAddresses = new HashSet<>();
    public HashSet<Integer> getUsedAddresses(){
        return usedAddresses;
    }
    protected boolean checkForZeroValue(Byte value){
        return value == 0;
    }
    public byte[] getMemorySequence(int from, int to){
        byte[] sequence = new byte[to];
        from = from & 0xFFFF;
        to = to & 0xFFFF;
        for(int i = from; i < from + to; i++){
            sequence[i - from] = readValue(i);
        }
        return sequence;
    }
    public boolean setValue(Integer address, Byte value){
        address = address & 0xFFFF;
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
    public boolean setValue(Integer address, byte value){
        return setValue(address, Byte.valueOf(value));
    }
    public byte readValue(Integer address){
        //LogUtils.getLogger().info("checking key - " + String.valueOf(address));
        address = address & 0xFFFF;
        if(memory.containsKey(address)){
            //LogUtils.getLogger().info("Success");
            return memory.get(address);
        }else{
            return 0;
        }
    }
}
