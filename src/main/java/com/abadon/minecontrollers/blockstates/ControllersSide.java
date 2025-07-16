package com.abadon.minecontrollers.blockstates;

import commoble.morered.plate_blocks.InputSide;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import commoble.morered.util.BlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.abadon.minecontrollers.blockstates.ControllerBlockStateProperties;

public enum ControllersSide {
    A(ControllerBlockStateProperties.INPUT_A, 1),
    B(ControllerBlockStateProperties.INPUT_B, 2),
    C(ControllerBlockStateProperties.INPUT_C, 3),
    D(ControllerBlockStateProperties.INPUT_D, 4);

    public final BooleanProperty property;
    public final int rotationsFromOutput;

    private ControllersSide(BooleanProperty property, int rotationsFromOutput) {
        this.property = property;
        this.rotationsFromOutput = rotationsFromOutput;
    }

    public boolean isBlockReceivingPower(Level world, BlockState state, BlockPos pos) {
        if (state.hasProperty(this.property) && state.hasProperty(PlateBlockStateProperties.ATTACHMENT_DIRECTION) && state.hasProperty(PlateBlockStateProperties.ROTATION)) {
            Direction attachmentDirection = (Direction)state.getValue(PlateBlockStateProperties.ATTACHMENT_DIRECTION);
            int baseRotation = (Integer)state.getValue(PlateBlockStateProperties.ROTATION);
            Direction inputDirection = BlockStateUtil.getInputDirection(attachmentDirection, baseRotation, this.rotationsFromOutput);
            BlockPos inputPos = pos.relative(inputDirection);
            int power = world.getSignal(inputPos, inputDirection);
            if (power > 0) {
                return true;
            } else {
                BlockState inputState = world.getBlockState(inputPos);
                return inputState.getBlock() == Blocks.REDSTONE_WIRE && (Integer)inputState.getValue(RedStoneWireBlock.POWER) > 0;
            }
        } else {
            return false;
        }
    }
}
