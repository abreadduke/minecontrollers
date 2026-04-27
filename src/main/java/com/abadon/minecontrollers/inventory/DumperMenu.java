//package com.abadon.minecontrollers.inventory;
//
//import com.abadon.minecontrollers.Minecontrollers;
//import com.abadon.minecontrollers.MinecontrollersBlocks;
//import com.abadon.minecontrollers.blocks.dumper.CodeDumper;
//import com.abadon.minecontrollers.network.packets.DumperPacket;
//import com.abadon.minecontrollers.utils.BookMenuUtils;
//import com.abadon.minecontrollers.utils.BookPagesWriter;
//import com.abadon.minecontrollers.utils.MicrocontrollerMemory;
//import com.mojang.logging.LogUtils;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.*;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.network.NetworkDirection;
//import net.minecraftforge.network.NetworkRegistry;
//import net.minecraftforge.network.simple.SimpleChannel;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Optional;
//
//public class DumperMenu extends AbstractContainerMenu {
//    protected Inventory inventory;
//    protected CraftingContainer container;
//    protected ContainerLevelAccess levelAccess;
//    protected Slot controllerSlot;
//    protected Slot bookSlot;
//    protected Slot outputSlot;
//    private static DumperMenu lastOpenedDumperMenu = null;
//    private static final SimpleChannel BOOK_GETTER_CHANNEL = NetworkRegistry.newSimpleChannel(
//            ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "dumper_menu_getter_channel"), () -> "",
//            versionFromServer -> true,
//            versionFromClient -> true
//    );
//    public static void registerNetworkChannel(){
//        BOOK_GETTER_CHANNEL.registerMessage(0, DumperPacket.class, (pack, buffer) -> buffer.writeInt(pack.from).writeInt(pack.to), (buffer) -> new DumperPacket(buffer.readInt(), buffer.readInt()), (pack, context) ->{
//            if(lastOpenedDumperMenu != null)
//                lastOpenedDumperMenu.doDump(pack.from, pack.to);
//        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
//    }
//    protected DumperMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
//        super(p_38851_, p_38852_);
//    }
//    @Override
//    public void removed(Player player) {
//        super.removed(player);
//        ItemStack controllerItem = slots.get(0).getItem();
//        ItemStack bookItem = slots.get(1).getItem();
//        ItemStack outputItem = slots.get(2).getItem();
//        if (player.isAlive()) {
//            if(controllerItem != ItemStack.EMPTY)
//                player.getInventory().placeItemBackInInventory(controllerItem);
//            if(bookItem != ItemStack.EMPTY)
//                player.getInventory().placeItemBackInInventory(bookItem);
//            if(outputItem != ItemStack.EMPTY)
//                player.getInventory().placeItemBackInInventory(outputItem);
//        } else {
//            if(controllerItem != ItemStack.EMPTY)
//                player.drop(controllerItem, false);
//            if(bookItem != ItemStack.EMPTY)
//                player.drop(bookItem, false);
//            if(outputItem != ItemStack.EMPTY)
//                player.drop(outputItem, false);
//        }
//    }
//    public void doDump(int start, int bytes){
//        if(controllerSlot.hasItem() && bookSlot.hasItem() && !outputSlot.hasItem()){
//            ItemStack dumpedBook = bookSlot.getItem().copy();
//            BookPagesWriter writer = new BookPagesWriter();
//            CompoundTag tag = controllerSlot.getItem().getTag();
//            try{
//                CompoundTag beTag = tag.getCompound("BlockEntityTag");
//                CompoundTag memoryTag = beTag.getCompound("memory");
//                MicrocontrollerMemory memory = new MicrocontrollerMemory();
//                for(String address : memoryTag.getAllKeys()){
//                    memory.setValue(Integer.valueOf(address), memoryTag.getByte(address));
//                }
//                writer.writeBook(dumpedBook, new CodeDumper().build(memory.getMemorySequence(start, bytes)));
//                container.setItem(1, ItemStack.EMPTY);
//                container.setItem(2, dumpedBook);
//            } catch (RuntimeException exception){
//                LogUtils.getLogger().info(exception.toString());
//            }
//        }
//    }
//    public void sendDumpPacket(DumperPacket pack){
//        BOOK_GETTER_CHANNEL.sendToServer(pack);
//    }
//    @Override
//    public ItemStack quickMoveStack(Player player, int id) {
//        ItemStack itemstack = ItemStack.EMPTY;
//        Slot slot = this.slots.get(id);
//        if (slot != null && slot.hasItem()) {
//            ItemStack itemstack1 = slot.getItem();
//            itemstack = itemstack1.copy();
//            if (id < this.container.getContainerSize()) {
//                if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
//                    return ItemStack.EMPTY;
//                }
//            } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
//                return ItemStack.EMPTY;
//            }
//
//            if (itemstack1.isEmpty()) {
//                slot.setByPlayer(ItemStack.EMPTY);
//            } else {
//                slot.setChanged();
//            }
//        }
//
//        return itemstack;
//    }
//
//    @Override
//    public boolean stillValid(Player player) {
//        return true;
//    }
//
//    public DumperMenu(int value, Inventory inventory){
//        this(value, inventory, ContainerLevelAccess.NULL);
//    }
//    public DumperMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess){
//        this(value, inventory, levelAccess, ModMenu.DUMPER_MENU_MENU_TYPE.get());
//    }
//    public DumperMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess, MenuType<?> menuType){
//        this(menuType, value);
//        this.inventory = inventory;
//        this.levelAccess = levelAccess;
//        container = new TransientCraftingContainer(this, 3, 1);
//        if(!inventory.player.level().isClientSide)
//            lastOpenedDumperMenu = this;
//        this.controllerSlot = new Slot(container, 0, 44, 23){
//            @Override
//            public boolean mayPlace(ItemStack itemStack) {
//                return itemStack.getItem().getDefaultInstance().getItem().equals(MinecontrollersBlocks.blockItems.get(MinecontrollersBlocks.MICROCONTROLLER_ID).get());
//            }
//        };
//        this.bookSlot = new Slot(container, 1, 44, 49){
//            @Override
//            public boolean mayPlace(ItemStack itemStack) {
//                return BookMenuUtils.canPlaceBookIntoContainer(itemStack);
//            }
//        };
//        this.outputSlot = new Slot(container, 2, 116, 35){
//            @Override
//            public boolean mayPlace(ItemStack itemStack) {
//                return false;
//            }
//        };
//        addSlot(controllerSlot);
//        addSlot(bookSlot);
//        addSlot(outputSlot);
//        for(int i = 0; i < 3; ++i) {
//            for(int j = 0; j < 9; ++j) {
//                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
//            }
//        }
//
//        for(int i = 0; i < 9; ++i) {
//            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
//        }
//    }
//}

package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.MinecontrollersBlocks;
import com.abadon.minecontrollers.blocks.dumper.CodeDumper;
import com.abadon.minecontrollers.network.packets.DumperPayload;
import com.abadon.minecontrollers.utils.BookMenuUtils;
import com.abadon.minecontrollers.utils.BookPagesWriter;
import com.abadon.minecontrollers.utils.MicrocontrollerMemory;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class DumperMenu extends AbstractContainerMenu {
    protected Inventory inventory;
    protected CraftingContainer container;
    protected ContainerLevelAccess levelAccess;
    protected Slot controllerSlot;
    protected Slot bookSlot;
    protected Slot outputSlot;
    public static DumperMenu lastOpenedDumperMenu = null;

    protected DumperMenu(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && lastOpenedDumperMenu == this) {
            lastOpenedDumperMenu = null;
        }
        for (int i = 0; i < 3; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (player.isAlive()) {
                    player.getInventory().placeItemBackInInventory(stack);
                } else {
                    player.drop(stack, false);
                }
            }
        }
    }

    public void doDump(int start, int bytes) {
        if (controllerSlot.hasItem() && bookSlot.hasItem() && !outputSlot.hasItem()) {
            ItemStack controllerStack = controllerSlot.getItem();
            ItemStack dumpedBook = bookSlot.getItem().copy();
            BookPagesWriter writer = new BookPagesWriter();

            try {
                CustomData customData = controllerStack.get(DataComponents.BLOCK_ENTITY_DATA);
                if (customData == null) return;

                CompoundTag beTag = customData.copyTag();
                CompoundTag memoryTag = beTag.getCompound("memory");

                MicrocontrollerMemory memory = new MicrocontrollerMemory();
                for (String address : memoryTag.getAllKeys()) {
                    memory.setValue(Integer.valueOf(address), memoryTag.getByte(address));
                }

                writer.writeBook(dumpedBook, new CodeDumper().build(memory.getMemorySequence(start, bytes)));

                container.setItem(1, ItemStack.EMPTY);
                container.setItem(2, dumpedBook);
            } catch (Exception exception) {
                LogUtils.getLogger().error("Error during dump: ", exception);
            }
        }
    }
    public void sendDumpPacket(int from, int to) {
        PacketDistributor.sendToServer(new DumperPayload(from, to));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int id) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(id);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (id < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public DumperMenu(int id, Inventory inv) {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public DumperMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ModMenu.DUMPER_MENU_MENU_TYPE.get(), id);
        this.inventory = inv;
        this.levelAccess = access;
        this.container = new TransientCraftingContainer(this, 3, 1);

        if (!inv.player.level().isClientSide) {
            lastOpenedDumperMenu = this;
        }

        this.controllerSlot = addSlot(new Slot(container, 0, 44, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(MinecontrollersBlocks.MICROCONTROLLER_BLOCK.get().asItem());
            }
        });

        this.bookSlot = addSlot(new Slot(container, 1, 44, 49) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BookMenuUtils.canPlaceBookIntoContainer(stack);
            }
        });

        this.outputSlot = addSlot(new Slot(container, 2, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }
}
