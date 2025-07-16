package com.abadon.minecontrollers.entityblocks.microprocessor;

import com.abadon.minecontrollers.blockstates.ControllersSide;
import com.abadon.minecontrollers.entityblocks.MinecontrollersBlocks;
import commoble.morered.api.ChanneledPowerSupplier;
import commoble.morered.api.MoreRedAPI;
import commoble.morered.bitwise_logic.BitwiseLogicPlateBlock;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import commoble.morered.util.BlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class MicroprocessorBlock extends BitwiseLogicPlateBlock {

    public MicroprocessorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        //LogUtils.getLogger().info("(microprocessor_test) " + MinecontrollersBlocks.BITWISE_LOGIC_BE_TYPE.get().getClass().getName());
        return MinecontrollersBlocks.MICROCONTROLLER_BE.get().create(pos, state);
    }
    @Override
    protected void updatePower(Level world, BlockPos thisPos, BlockState thisState) {
        BlockEntity te = world.getBlockEntity(thisPos);
        if (te instanceof MicrocontrollerBlockEntity logicTE) {
            //byte[] power = new byte[16*4];
            Direction attachmentDir = (Direction)thisState.getValue(PlateBlockStateProperties.ATTACHMENT_DIRECTION);
            int rotationIndex = (Integer)thisState.getValue(PlateBlockStateProperties.ROTATION);
            Direction inputSideA = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.A.rotationsFromOutput);
            Direction inputSideB = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.B.rotationsFromOutput);
            Direction inputSideC = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.C.rotationsFromOutput);
            Direction inputSideD = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.D.rotationsFromOutput);
            BlockEntity inputTileA = world.getBlockEntity(thisPos.relative(inputSideA));
            BlockEntity inputTileB = world.getBlockEntity(thisPos.relative(inputSideB));
            BlockEntity inputTileC = world.getBlockEntity(thisPos.relative(inputSideC));
            BlockEntity inputTileD = world.getBlockEntity(thisPos.relative(inputSideD));
            ChanneledPowerSupplier inputA = inputTileA == null ? BitwiseLogicPlateBlock.NO_POWER_SUPPLIER : (ChanneledPowerSupplier)inputTileA.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputSideA.getOpposite()).orElse(NO_POWER_SUPPLIER);
            ChanneledPowerSupplier inputB = inputTileB == null ? BitwiseLogicPlateBlock.NO_POWER_SUPPLIER : (ChanneledPowerSupplier)inputTileB.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputSideB.getOpposite()).orElse(NO_POWER_SUPPLIER);
            ChanneledPowerSupplier inputC = inputTileC == null ? BitwiseLogicPlateBlock.NO_POWER_SUPPLIER : (ChanneledPowerSupplier)inputTileC.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputSideC.getOpposite()).orElse(NO_POWER_SUPPLIER);
            ChanneledPowerSupplier inputD = inputTileD == null ? BitwiseLogicPlateBlock.NO_POWER_SUPPLIER : (ChanneledPowerSupplier)inputTileD.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputSideD.getOpposite()).orElse(NO_POWER_SUPPLIER);

            //int offset = 0;
            ArrayList<Byte> cabbleA = new ArrayList<>();
            ArrayList<Byte> cabbleB = new ArrayList<>();
            ArrayList<Byte> cabbleC = new ArrayList<>();
            ArrayList<Byte> cabbleD = new ArrayList<>();
            for(int i = 0; i < 16; ++i) {
                //boolean inputBitA = inputA.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i) > 0;
                //boolean inputBitB = inputB.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i) > 0;
                //boolean inputBitC = inputC.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i) > 0;
                //boolean inputBitD = inputD.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i) > 0;
                //LogUtils.getLogger().info(String.valueOf(i));
                cabbleA.add((byte)inputA.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                cabbleB.add((byte)inputB.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                cabbleC.add((byte)inputC.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                cabbleD.add((byte)inputD.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                //power[i + offset * 3] = (byte)inputD.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i);
                //power[i + offset * 3 + 1] = (byte)inputA.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i);
                //power[i + offset * 3 + 2] = (byte)inputB.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i);
                //power[i + offset * 3 + 3] = (byte)inputC.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i);
                //offset++;
            }
            ArrayList<Byte> power = new ArrayList<Byte>();
            power.addAll(cabbleD);
            power.addAll(cabbleA);
            power.addAll(cabbleB);
            power.addAll(cabbleC);
            Byte[] bytes = new Byte[power.size()];
            power.toArray(bytes);
            logicTE.setPower(ArrayUtils.toPrimitive(bytes).clone());
            logicTE.applySettings();
            //logicTE.setPower(ArrayUtils.toPrimitive(bytes).clone());
        }

    }
    @Override
    public boolean canConnectToAdjacentCable(@Nonnull BlockGetter var1, @Nonnull BlockPos var2, @Nonnull BlockState var3, @Nonnull BlockPos var4, @Nonnull BlockState var5, @Nonnull Direction var6, @Nonnull Direction var7){
        return true;
    }
}
