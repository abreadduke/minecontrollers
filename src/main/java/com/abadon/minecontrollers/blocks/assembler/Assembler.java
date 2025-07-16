package com.abadon.minecontrollers.blocks.assembler;

import com.abadon.minecontrollers.inventory.AssemblerMenu;
import com.abadon.minecontrollers.inventory.FormatterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class Assembler extends Block {
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(getMenuProvider(blockState, level, blockPos));
            return InteractionResult.CONSUME;
        }
    }
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((value, inventory, player) -> new AssemblerMenu(value, inventory, ContainerLevelAccess.create(level, blockPos)), Component.translatable("container.minecontrollers.assembler"));
    }
    public Assembler(Properties p_49795_) {
        super(p_49795_);
    }

}