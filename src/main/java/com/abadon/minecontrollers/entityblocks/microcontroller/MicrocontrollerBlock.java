package com.abadon.minecontrollers.entityblocks.microcontroller;

import com.abadon.minecontrollers.blockstates.ControllersSide;
import com.abadon.minecontrollers.MinecontrollersBlocks;
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
import java.util.Optional;

public class MicrocontrollerBlock extends BitwiseLogicPlateBlock {

    public MicrocontrollerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return MinecontrollersBlocks.MICROCONTROLLER_BE.get().create(pos, state);
    }
    @Override
    protected void updatePower(Level world, BlockPos thisPos, BlockState thisState) {
        BlockEntity te = world.getBlockEntity(thisPos);
        if (te instanceof MicrocontrollerBlockEntity logicTE) {
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
            ChanneledPowerSupplier inputA = BitwiseLogicPlateBlock.NO_POWER_SUPPLIER;
            ChanneledPowerSupplier inputB = BitwiseLogicPlateBlock.NO_POWER_SUPPLIER;
            ChanneledPowerSupplier inputC = BitwiseLogicPlateBlock.NO_POWER_SUPPLIER;
            ChanneledPowerSupplier inputD = BitwiseLogicPlateBlock.NO_POWER_SUPPLIER;
            if (inputTileA != null){
                inputA = Optional.ofNullable(world.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputTileA.getBlockPos(), inputSideA.getOpposite()))
                        .orElse(BitwiseLogicPlateBlock.NO_POWER_SUPPLIER);
            }
            if (inputTileB != null){
                inputB = Optional.ofNullable(world.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputTileB.getBlockPos(), inputSideB.getOpposite()))
                        .orElse(BitwiseLogicPlateBlock.NO_POWER_SUPPLIER);
            }
            if (inputTileC != null){
                inputC = Optional.ofNullable(world.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputTileC.getBlockPos(), inputSideC.getOpposite()))
                        .orElse(BitwiseLogicPlateBlock.NO_POWER_SUPPLIER);
            }
            if (inputTileD != null){
                inputD = Optional.ofNullable(world.getCapability(MoreRedAPI.CHANNELED_POWER_CAPABILITY, inputTileD.getBlockPos(), inputSideD.getOpposite()))
                        .orElse(BitwiseLogicPlateBlock.NO_POWER_SUPPLIER);
            }

            ArrayList<Byte> cabbleA = new ArrayList<>();
            ArrayList<Byte> cabbleB = new ArrayList<>();
            ArrayList<Byte> cabbleC = new ArrayList<>();
            ArrayList<Byte> cabbleD = new ArrayList<>();
            for(int i = 0; i < 16; ++i) {
                cabbleA.add((byte)inputA.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                cabbleB.add((byte)inputB.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                cabbleC.add((byte)inputC.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
                cabbleD.add((byte)inputD.getPowerOnChannel(world, thisPos, thisState, attachmentDir, i));
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
        }

    }
    @Override
    public boolean canConnectToAdjacentCable(@Nonnull BlockGetter var1, @Nonnull BlockPos var2, @Nonnull BlockState var3, @Nonnull BlockPos var4, @Nonnull BlockState var5, @Nonnull Direction var6, @Nonnull Direction var7){
        return true;
    }
}
