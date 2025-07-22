package com.abadon.minecontrollers.api;

import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlockEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.HashMap;

public class MinecontrollersAPI {
    private static HashMap<Integer, InterruptSupplier> interrupts = new HashMap<>();
    public static void registerInterrupt(InterruptSupplier interruptFunction, int number){
        interrupts.put(number, interruptFunction);
    }
    public static void invokeInterrupt(MicrocontrollerBlockEntity microcontrollerBlockEntity, int interruptNumber){
        interrupts.get(interruptNumber).interruptFunction(microcontrollerBlockEntity);
    }
    public static SoundEvent formatNumberToSound(int number){
        SoundEvent sound = null;
        switch (number){
            case 1:{
                sound = SoundEvents.NOTE_BLOCK_BASS.get();
                break;
            }
            case 2:{
                sound = SoundEvents.NOTE_BLOCK_BELL.get();
                break;
            }
            case 3:{
                sound = SoundEvents.NOTE_BLOCK_CHIME.get();
                break;
            }
            case 4:{
                sound = SoundEvents.NOTE_BLOCK_FLUTE.get();
                break;
            }
            case 5:{
                sound = SoundEvents.NOTE_BLOCK_GUITAR.get();
                break;
            }
            case 6:{
                sound = SoundEvents.NOTE_BLOCK_HARP.get();
                break;
            }
            case 7:{
                sound = SoundEvents.NOTE_BLOCK_HAT.get();
                break;
            }
            case 8:{
                sound = SoundEvents.NOTE_BLOCK_PLING.get();
                break;
            }
            case 9:{
                sound = SoundEvents.NOTE_BLOCK_SNARE.get();
                break;
            }
            case 10:{
                sound = SoundEvents.NOTE_BLOCK_XYLOPHONE.get();
                break;
            }
            case 11:{
                sound = SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.get();
                break;
            }
            case 12:{
                sound = SoundEvents.NOTE_BLOCK_COW_BELL.get();
                break;
            }
            case 13:{
                sound = SoundEvents.NOTE_BLOCK_DIDGERIDOO.get();
                break;
            }
            case 14:{
                sound = SoundEvents.NOTE_BLOCK_BIT.get();
                break;
            }
            case 15:{
                sound = SoundEvents.NOTE_BLOCK_BANJO.get();
                break;
            }
            case 16:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_ZOMBIE.get();
                break;
            }
            case 17:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_SKELETON.get();
                break;
            }
            case 18:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_CREEPER.get();
                break;
            }
            case 19:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_ENDER_DRAGON.get();
                break;
            }
            case 20:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_WITHER_SKELETON.get();
                break;
            }
            case 21:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_PIGLIN.get();
                break;
            }
            default:{
                sound = SoundEvents.NOTE_BLOCK_BASEDRUM.get();
            }
        }
        return sound;
    }
}
