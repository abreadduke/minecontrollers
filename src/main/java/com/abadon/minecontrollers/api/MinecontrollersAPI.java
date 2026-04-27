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
                sound = SoundEvents.NOTE_BLOCK_BASS.value();
                break;
            }
            case 2:{
                sound = SoundEvents.NOTE_BLOCK_BELL.value();
                break;
            }
            case 3:{
                sound = SoundEvents.NOTE_BLOCK_CHIME.value();
                break;
            }
            case 4:{
                sound = SoundEvents.NOTE_BLOCK_FLUTE.value();
                break;
            }
            case 5:{
                sound = SoundEvents.NOTE_BLOCK_GUITAR.value();
                break;
            }
            case 6:{
                sound = SoundEvents.NOTE_BLOCK_HARP.value();
                break;
            }
            case 7:{
                sound = SoundEvents.NOTE_BLOCK_HAT.value();
                break;
            }
            case 8:{
                sound = SoundEvents.NOTE_BLOCK_PLING.value();
                break;
            }
            case 9:{
                sound = SoundEvents.NOTE_BLOCK_SNARE.value();
                break;
            }
            case 10:{
                sound = SoundEvents.NOTE_BLOCK_XYLOPHONE.value();
                break;
            }
            case 11:{
                sound = SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value();
                break;
            }
            case 12:{
                sound = SoundEvents.NOTE_BLOCK_COW_BELL.value();
                break;
            }
            case 13:{
                sound = SoundEvents.NOTE_BLOCK_DIDGERIDOO.value();
                break;
            }
            case 14:{
                sound = SoundEvents.NOTE_BLOCK_BIT.value();
                break;
            }
            case 15:{
                sound = SoundEvents.NOTE_BLOCK_BANJO.value();
                break;
            }
            case 16:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_ZOMBIE.value();
                break;
            }
            case 17:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_SKELETON.value();
                break;
            }
            case 18:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_CREEPER.value();
                break;
            }
            case 19:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_ENDER_DRAGON.value();
                break;
            }
            case 20:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_WITHER_SKELETON.value();
                break;
            }
            case 21:{
                sound = SoundEvents.NOTE_BLOCK_IMITATE_PIGLIN.value();
                break;
            }
            default:{
                sound = SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            }
        }
        return sound;
    }
}
