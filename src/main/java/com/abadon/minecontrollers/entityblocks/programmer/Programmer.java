package com.abadon.minecontrollers.entityblocks.programmer;

import com.abadon.minecontrollers.MinecontrollersBlocks;
import commoble.morered.bitwise_logic.BitwiseLogicPlateBlock;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import commoble.morered.util.BlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class Programmer extends BitwiseLogicPlateBlock {
    private boolean neighbourHasSignal = false;
    public static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return MinecontrollersBlocks.PROGRAMMER_BE.get().create(pos, state);
    }

    @Override
    protected void updatePower(Level level, BlockPos blockPos, BlockState blockState) {

    }

    @Override
    public boolean isSignalSource(BlockState p_60571_) {
        return true;
    }
    public Direction getLookDirection(BlockState blockState){
        return PlateBlockStateProperties.getOutputDirection(blockState);
    }
    public Direction getLocalDirection(BlockState blockState, int rotations){
        return BlockStateUtil.getOutputDirection(getLookDirection(blockState), rotations);
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState p_60457_) {
        return true;
    }
    @Override
    public int getSignal(BlockState thisState, BlockGetter blockGetter, BlockPos thisPos, Direction direction) {
        if(blockGetter.getBlockEntity(thisPos) instanceof ProgrammerBlockEntity blockEntity)
            return BlockStateUtil.getOutputDirection(getLookDirection(thisState), 1) == direction && blockEntity.getWorkStatus() ? 15 : 0;
        else return 0;
    }
    @Override
    public void neighborChanged(BlockState thisState, Level level, BlockPos thisPos, Block block, BlockPos neighborPos, boolean p_52530_) {
        super.neighborChanged(thisState, level, thisPos, block, neighborPos, p_52530_);
        if (thisState.canSurvive(level, thisPos) && level.getBlockEntity(thisPos) instanceof ProgrammerBlockEntity programmerBlockEntity) {
            boolean signal = false;
            if(!level.isClientSide() && thisPos.relative(getLocalDirection(thisState, 1)).equals(neighborPos) &&
                    (signal = level.getBlockState(neighborPos).getSignal(level, neighborPos, getLocalDirection(thisState, 1).getOpposite()) > 0) &&
                     neighbourHasSignal != signal){
                programmerBlockEntity.invokeSeeker(level, thisState, thisPos);
            }
            neighbourHasSignal = signal;
        } else {
            BlockEntity blockentity = thisState.hasBlockEntity() ? level.getBlockEntity(thisPos) : null;
            dropResources(thisState, level, thisPos, blockentity);
            level.removeBlock(thisPos, false);

            for(Direction direction : Direction.values()) {
                level.updateNeighborsAt(thisPos.relative(direction), this);
            }
        }

    }
    @Override
    public boolean canConnectToAdjacentCable(@Nonnull BlockGetter world, @Nonnull BlockPos thisPos, @Nonnull BlockState thisState, @Nonnull BlockPos wirePos, @Nonnull BlockState wireState, @Nonnull Direction wireFace, @Nonnull Direction directionToWire) {
        Direction primaryOutputDirection = PlateBlockStateProperties.getOutputDirection(thisState);
        return directionToWire == primaryOutputDirection;
    }

    public Programmer(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(getStateDefinition().any().setValue(HAS_BOOK, false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_BOOK);

    }

    public void onRemove(BlockState p_51538_, Level p_51539_, BlockPos p_51540_, BlockState p_51541_, boolean p_51542_) {
        if (!p_51538_.is(p_51541_.getBlock())) {
            BlockEntity blockentity = p_51539_.getBlockEntity(p_51540_);
            if (blockentity instanceof Container) {
                Containers.dropContents(p_51539_, p_51540_, (Container)blockentity);
                p_51539_.updateNeighbourForOutputSignal(p_51540_, this);
            }

            super.onRemove(p_51538_, p_51539_, p_51540_, p_51541_, p_51542_);
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
        return p_60578_.getShape(p_60579_, p_60580_);
    }
    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return Shapes.block();
    }
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if(level.getBlockEntity(blockPos) instanceof ProgrammerBlockEntity programmerBlockEntity)
                player.openMenu(programmerBlockEntity);
            return InteractionResult.CONSUME;
        }
    }
}
