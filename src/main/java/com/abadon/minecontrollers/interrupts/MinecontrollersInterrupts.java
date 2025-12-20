package com.abadon.minecontrollers.interrupts;

import com.abadon.minecontrollers.ServerConfig;
import com.abadon.minecontrollers.api.MinecontrollersAPI;
import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlockEntity;
import com.google.common.collect.Lists;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.List;

public class MinecontrollersInterrupts {
    public static void getGameTimeInt(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        if (level == null) return;
        long gameTime = level.getGameTime();
        microcontrollerBlockEntity.registerA = (short) ((gameTime >>> 48) & 0xFFFF);
        microcontrollerBlockEntity.registerB = (short) ((gameTime >>> 32) & 0xFFFF);
        microcontrollerBlockEntity.registerC = (short) ((gameTime >>> 16) & 0xFFFF);
        microcontrollerBlockEntity.registerD = (short) (gameTime & 0xFFFF);
    }
    public static void getDayTimeInt(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        if (level == null) return;
        long gameTime = level.getDayTime();
        microcontrollerBlockEntity.registerA = (short) ((gameTime >>> 48) & 0xFFFF);
        microcontrollerBlockEntity.registerB = (short) ((gameTime >>> 32) & 0xFFFF);
        microcontrollerBlockEntity.registerC = (short) ((gameTime >>> 16) & 0xFFFF);
        microcontrollerBlockEntity.registerD = (short) (gameTime & 0xFFFF);
    }
    public static void getSinCos(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        double sin = Math.sin(Math.toRadians(microcontrollerBlockEntity.registerA));
        double cos = Math.cos(Math.toRadians(microcontrollerBlockEntity.registerA));
        microcontrollerBlockEntity.registerA = (short)(sin * 1000);
        microcontrollerBlockEntity.registerB = (short)(cos * 1000);
    }
    public static void getLogn(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        microcontrollerBlockEntity.registerA = (short)(Math.log(microcontrollerBlockEntity.registerA) * 1000);
    }
    public static void getPow(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        int a = microcontrollerBlockEntity.registerA;
        int b = microcontrollerBlockEntity.registerB;
        microcontrollerBlockEntity.registerA = (short)Math.pow(a, (double)b / 1000D);
    }
    public static void getDayStatus(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        if (level == null) return;
        microcontrollerBlockEntity.registerA = (short)(level.isDay() ? 1 : 0);
    }
    public static void getRainingStatus(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        if (level == null) return;
        microcontrollerBlockEntity.registerA = (short)(level.isRaining() ? 1 : 0);
    }
    public static void getThunderingStatus(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        if (level == null) return;
        microcontrollerBlockEntity.registerA = (short)(level.isThundering() ? 1 : 0);
    }
    public static void getRandomNumber(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        if (level == null) return;
        microcontrollerBlockEntity.registerA = (short)(level.getRandom().nextInt());
    }
    public static void getSelfCoordinates(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        microcontrollerBlockEntity.pushStack((blockPos.getX() >>> 16) & 0xFFFF);
        microcontrollerBlockEntity.pushStack(blockPos.getX() & 0xFFFF);
        microcontrollerBlockEntity.pushStack((blockPos.getY() >>> 16) & 0xFFFF);
        microcontrollerBlockEntity.pushStack(blockPos.getY() & 0xFFFF);
        microcontrollerBlockEntity.pushStack((blockPos.getZ() >>> 16) & 0xFFFF);
        microcontrollerBlockEntity.pushStack(blockPos.getZ() & 0xFFFF);
    }
    public static void getWatchCoordinates(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        BlockState blockState = level.getBlockState(blockPos);
        Direction direction = blockState.getValue(PlateBlockStateProperties.ATTACHMENT_DIRECTION).getOpposite();
        int observeDistance = ServerConfig.MAX_OBS_RANGE.get();
        for (int i = 1; i < observeDistance; i++) {
            BlockPos foundBlockPos = blockPos.relative(direction, i);
            Block foundBlock = level.getBlockState(foundBlockPos).getBlock();
            if (!foundBlock.getDescriptionId().equals("block.minecraft.air")) {
                microcontrollerBlockEntity.registerA = (short) 1;
                microcontrollerBlockEntity.registerB = (short) (foundBlockPos.getX() - blockPos.getX());
                microcontrollerBlockEntity.registerC = (short) (foundBlockPos.getY() - blockPos.getY());
                microcontrollerBlockEntity.registerD = (short) (foundBlockPos.getZ() - blockPos.getZ());
                return;
            }
        }
        microcontrollerBlockEntity.registerA = (short) 0;
    }
    public static void getNearbyPlayers(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int maxCubeRadius = ServerConfig.MAX_PLAYER_FIND_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        List<Player> nearbyPlayers = Lists.newArrayList();
        for (Player player : level.players()) {
            if (new AABB(firstPos, secondPos).contains(player.getX(), player.getY(), player.getZ())) {
                nearbyPlayers.add(player);
            }
        }
        microcontrollerBlockEntity.registerA = (short) nearbyPlayers.size();
    }
    public static void getPlayerHash(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int playerIndex = microcontrollerBlockEntity.registerB;
        int maxCubeRadius = ServerConfig.MAX_PLAYER_FIND_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        List<Player> nearbyPlayers = Lists.newArrayList();
        for (Player player : level.players()) {
            if (new AABB(firstPos, secondPos).contains(player.getX(), player.getY(), player.getZ())) {
                nearbyPlayers.add(player);
            }
        }
        if(playerIndex >= nearbyPlayers.size()){
            microcontrollerBlockEntity.registerA = 0;
            return;
        }
        microcontrollerBlockEntity.registerA = 1;
        int hash = nearbyPlayers.get(playerIndex).getName().hashCode();
        microcontrollerBlockEntity.registerB = (short) ((hash >>> 4) & 0xFFFF);
        microcontrollerBlockEntity.registerC = (short) (hash & 0xFFFF);
    }
    public static void getPlayerLocalCords(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int playerIndex = microcontrollerBlockEntity.registerB;
        int maxCubeRadius = ServerConfig.MAX_PLAYER_FIND_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        List<Player> nearbyPlayers = Lists.newArrayList();
        for (Player player : level.players()) {
            if (new AABB(firstPos, secondPos).contains(player.getX(), player.getY(), player.getZ())) {
                nearbyPlayers.add(player);
            }
        }
        if(playerIndex >= nearbyPlayers.size()){
            microcontrollerBlockEntity.registerA = 0;
            return;
        }
        microcontrollerBlockEntity.registerA = 1;
        LivingEntity player = nearbyPlayers.get(playerIndex);
        microcontrollerBlockEntity.registerB = (short) (player.getBlockX() - blockPos.getX());
        microcontrollerBlockEntity.registerC = (short) (player.getBlockY() - blockPos.getY());
        microcontrollerBlockEntity.registerD = (short) (player.getBlockZ() - blockPos.getZ());
    }
    public static void getNearbyEntitiesCount(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int maxCubeRadius = ServerConfig.MAX_ENTITY_FIND_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, new AABB(firstPos, secondPos));
        microcontrollerBlockEntity.registerA = (short) nearbyEntities.size();
    }
    public static void getEntityHash(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int entityIndex = microcontrollerBlockEntity.registerB;
        int maxCubeRadius = ServerConfig.MAX_ENTITY_FIND_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, new AABB(firstPos, secondPos));
        if(entityIndex >= nearbyEntities.size()){
            microcontrollerBlockEntity.registerA = 0;
            return;
        }
        microcontrollerBlockEntity.registerA = 1;
        int hash = nearbyEntities.get(entityIndex).getName().hashCode();
        microcontrollerBlockEntity.registerB = (short) ((hash >>> 4) & 0xFFFF);
        microcontrollerBlockEntity.registerC = (short) (hash & 0xFFFF);
    }
    public static void getEntityLocalCords(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if (level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int entityIndex = microcontrollerBlockEntity.registerB;
        int maxCubeRadius = ServerConfig.MAX_ENTITY_FIND_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, new AABB(firstPos, secondPos));
        if(entityIndex >= nearbyEntities.size()){
            microcontrollerBlockEntity.registerA = 0;
            return;
        }
        microcontrollerBlockEntity.registerA = 1;
        LivingEntity entity = nearbyEntities.get(entityIndex);
        microcontrollerBlockEntity.registerB = (short) (entity.getBlockX() - blockPos.getX());
        microcontrollerBlockEntity.registerC = (short) (entity.getBlockY() - blockPos.getY());
        microcontrollerBlockEntity.registerD = (short) (entity.getBlockZ() - blockPos.getZ());
    }
    public static void playSound(MicrocontrollerBlockEntity microcontrollerBlockEntity){
        Level level = microcontrollerBlockEntity.getLevel();
        BlockPos blockPos = microcontrollerBlockEntity.getBlockPos();
        if(level == null || level.getBlockEntity(blockPos) == null || !level.getBlockEntity(blockPos).equals(microcontrollerBlockEntity)) return;
        int cubeRadius = microcontrollerBlockEntity.registerA;
        int maxCubeRadius = ServerConfig.MAX_MC_SOUNDING_RANGE.get();
        cubeRadius = Math.abs(cubeRadius);
        cubeRadius = cubeRadius > maxCubeRadius ? maxCubeRadius : cubeRadius;
        BlockPos firstPos = blockPos.offset(-cubeRadius, -cubeRadius, -cubeRadius);
        BlockPos secondPos = blockPos.offset(cubeRadius, cubeRadius, cubeRadius);
        SoundEvent sound = MinecontrollersAPI.formatNumberToSound(microcontrollerBlockEntity.registerB);
        for(Player player : level.players()) {
            if (new AABB(firstPos, secondPos).contains(player.getX(), player.getY(), player.getZ())) {
                if (!level.isClientSide) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.NOTE,
                            blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D,
                            1, 0, 0, 0D,
                            microcontrollerBlockEntity.registerB + microcontrollerBlockEntity.registerC);
                } else {
                    level.addParticle(ParticleTypes.NOTE,
                            blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D,
                            0, 0, 0); // На клиенте логика движения частиц задается иначе
                }

                level.playSound(null, blockPos, sound, SoundSource.BLOCKS, 1, (float) microcontrollerBlockEntity.registerC / 1000F); //CX register must be 0-2000 but it works only between 500 and 1000
            }
        }
    }
    public static void register(){
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getGameTimeInt, 0);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getDayTimeInt, 1);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getDayStatus, 2);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getRainingStatus, 3);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getThunderingStatus, 4);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getSinCos, 5);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getLogn, 6);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getPow, 7);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getRandomNumber, 8);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getSelfCoordinates, 9);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getWatchCoordinates, 10);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getNearbyPlayers, 11);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getPlayerHash, 12);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getPlayerLocalCords, 13);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getNearbyEntitiesCount, 14);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getEntityHash, 15);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::getEntityLocalCords, 16);
        MinecontrollersAPI.registerInterrupt(MinecontrollersInterrupts::playSound, 17);
    }
}
