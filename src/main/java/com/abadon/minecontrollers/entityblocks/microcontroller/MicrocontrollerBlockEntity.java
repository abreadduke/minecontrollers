package com.abadon.minecontrollers.entityblocks.microcontroller;

import com.abadon.minecontrollers.api.MinecontrollersAPI;
import com.abadon.minecontrollers.blockstates.ControllersSide;
import com.abadon.minecontrollers.MinecontrollersBlocks;
import com.abadon.minecontrollers.utils.AddressingVariants;
import com.abadon.minecontrollers.utils.CommandHandler;
import com.abadon.minecontrollers.utils.ControllerMath;
import com.abadon.minecontrollers.utils.MicrocontrollerMemory;
import com.mojang.logging.LogUtils;
import commoble.morered.bitwise_logic.ChanneledPowerStorageBlockEntity;
import commoble.morered.plate_blocks.PlateBlock;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import commoble.morered.util.BlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

public class MicrocontrollerBlockEntity extends ChanneledPowerStorageBlockEntity {

    public static final int powerPinsCount = 16*4;
    protected MicrocontrollerMemory memory = new MicrocontrollerMemory();
    public short registerA = 0;
    public short registerB = 0;
    public short registerC = 0;
    public short registerD = 0;
    public short registerIp = 0;
    public short registerSp = 0;
    public short registerBp = 0;
    public short registerDi = 0;
    public short registerSi = 0;
    public short registerI = 0;
    public static final byte instructionSize = 6;
    public static final byte commandletSize = 1;
    public short dataSegment = 0;
    public short codeSegment = 0;
    public short stackSegment = 0;
    public short extraSegment = 0;
    public short flags = 0; //CF;ZF;SF;IF
    private boolean startFlag = false;
    protected byte[] outputPins = new byte[powerPinsCount];
    private HashMap<Byte, String> registers = new HashMap<>();
    private HashMap<Byte, String> lowBitRegisters = new HashMap<>();
    private HashMap<Byte, String> highBitRegisters = new HashMap<>();
    private HashMap<Byte, String> segmentRegisters = new HashMap<>();
    private HashMap<Byte, String> redstonePins = new HashMap<>();
    public int getValueByAddress(Integer address){
        return memory.readValue(address);
    }
    public Collection<String> getRegistersNames(){
        return registers.values();
    }
    public Collection<String> getLowBitRegistersNames(){
        return lowBitRegisters.values();
    }
    public Collection<String> getHighBitRegistersNames(){
        return highBitRegisters.values();
    }
    public Collection<String> getSegmentRegistersNames(){
        return segmentRegisters.values();
    }
    private int registerInc = 0;
    protected void registerNewRegister(String register){
        registers.put((byte)registerInc++, register);
    }
    protected void registerLowBitRegister(String register){
        lowBitRegisters.put((byte)registerInc++, register);
    }
    protected void registerHighBitRegister(String register){
        highBitRegisters.put((byte)registerInc++, register);
    }
    protected void registerSegmentRegister(String register){
        segmentRegisters.put((byte)registerInc++, register);
    }
    protected void registerRedstonePin(String register){
        redstonePins.put((byte)registerInc++, register);
    }
    public MicrocontrollerBlockEntity(BlockEntityType<? extends MicrocontrollerBlockEntity> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
        this.power = new byte[powerPinsCount];
        registerNewRegister("registerA");
        registerHighBitRegister("registerA");
        registerLowBitRegister("registerA");
        registerNewRegister("registerB");
        registerHighBitRegister("registerB");
        registerLowBitRegister("registerB");
        registerNewRegister("registerC");
        registerHighBitRegister("registerC");
        registerLowBitRegister("registerC");
        registerNewRegister("registerD");
        registerHighBitRegister("registerD");
        registerLowBitRegister("registerD");
        registerNewRegister("registerDi");
        registerNewRegister("registerSi");
        registerNewRegister("registerBp");
        registerNewRegister("registerSp");
        registerNewRegister("registerIp");
        registerNewRegister("flags");
        registerSegmentRegister("dataSegment");
        registerSegmentRegister("stackSegment");
        registerSegmentRegister("codeSegment");
        registerSegmentRegister("extraSegment");
        for(int i = 0; i < powerPinsCount; i++){
            registerRedstonePin("r" + i);
        }
        registerNewRegister("registerI");
    }
    public MicrocontrollerBlockEntity(BlockPos pos, BlockState state) {
        this(MinecontrollersBlocks.MICROCONTROLLER_BE.get(), pos, state);
    }
    public static MicrocontrollerBlockEntity create(BlockPos pos, BlockState state) {
        return new MicrocontrollerBlockEntity(MinecontrollersBlocks.MICROCONTROLLER_BE.get(), pos, state);
    }
    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        //if (cap == MoreRedAPI.CHANNELED_POWER_CAPABILITY) {
        //    return side == PlateBlockStateProperties.getOutputDirection(this.getBlockState()) ? this.powerHolder : LazyOptional.empty();
        //} else {
        //    return super.getCapability(cap, side);
        //}
        return (LazyOptional<T>) this.powerHolder;
    }
    public void changeIOState(){
        for(int i = 0; i < powerPinsCount; i++){
            if(outputPins[i] > 0){
                power[i] = 0;
            }
        }
        if (!this.level.isClientSide) setChanged();
    }
    @Override
    public int getPowerOnChannel(Level level, BlockPos wirePos, BlockState wireState, Direction wireFace, int channel) {
        BlockState thisState = this.getBlockState();
        BlockPos thisPosition = this.getBlockPos();
        int rotationIndex = (Integer)thisState.getValue(PlateBlockStateProperties.ROTATION);
        Direction attachmentDir = (Direction)thisState.getValue(PlateBlock.ATTACHMENT_DIRECTION);
        Direction inputSideA = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.A.rotationsFromOutput);
        Direction inputSideB = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.B.rotationsFromOutput);
        Direction inputSideC = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.C.rotationsFromOutput);
        Direction inputSideD = BlockStateUtil.getInputDirection(attachmentDir, rotationIndex, ControllersSide.D.rotationsFromOutput);
        int offset = 0;
        int channels = 16;
        if(wireFace == null) return 0;
        if(thisPosition.relative(inputSideA).equals(wirePos)){
            offset = 1;
        }else if(thisPosition.relative(inputSideB).equals(wirePos)){
            offset = 2;
        }else if(thisPosition.relative(inputSideC).equals(wirePos)){
            offset = 3;
        }else if(thisPosition.relative(inputSideD).equals(wirePos)){
            offset = 0;
        }
        return getOutputPower(channel + offset * channels);
    }
    public byte getOutputPower(int channel){
        return this.outputPins[channel];
    }
    public void callInterrupt(int pin){
        int interruptionAddress = registerI + pin * 2;
        Address address = new Address();
        address.IsAddress16bit = true;
        address.addressVariant = AddressingVariants.VALUE;
        address.offset = 0;
        address.useValueAsAddress = true;
        address.address = interruptionAddress;
        AddressIO addressIO = new AddressIO(this);
        int interruptionPoint = addressIO.getValueFromAddress(address);
        if(interruptionPoint != 0){
            short oldES = extraSegment;
            extraSegment = (short)interruptionPoint;
            Address callAddress = new Address();
            callAddress.IsAddress16bit = true;
            callAddress.addressVariant = AddressingVariants.REGISTERS;
            callAddress.offset = 3;
            callAddress.useValueAsAddress = true;
            callAddress.address = instructionSize;
            call(callAddress);
            extraSegment = oldES;
            pushAllRegisters();

        }
    }
    @Override
    public boolean setPower(byte[] newPowers) {
        boolean updated = false;

        for(int i = 0; i < powerPinsCount; ++i) {
            byte newPower = newPowers[i];
            byte oldPower = this.power[i];
            if (newPower != oldPower) {
                this.power[i] = newPower;
                if((flags & 1) == 1){
                    callInterrupt(i);
                }
                updated = true;
            }
        }

        if (updated && !this.level.isClientSide) {
            this.setChanged();
            return true;
        } else {
            return false;
        }
    }
    protected class Address{
        public AddressingVariants addressVariant = AddressingVariants.REGISTERS;
        public boolean useValueAsAddress = false;
        public boolean IsAddress16bit = false;
        public int offset = 0;
        public int address = 0;
    }
    protected class AddressReader {
        private final int instructionSize;
        MicrocontrollerBlockEntity microprocessorBE;
        int addressInfoOffset;
        public Address firstAddress= new Address();
        public Address secondAddress = new Address();
        public AddressReader(MicrocontrollerBlockEntity microprocessorBE, int instructionSize, int addressInfoOffset){
            this.microprocessorBE = microprocessorBE;
            this.instructionSize = instructionSize;
            this.addressInfoOffset = addressInfoOffset;
        }
        public void calculateAddressTypes(){
            byte[] memorySeq = microprocessorBE.memory.getMemorySequence((short)(microprocessorBE.registerIp + microprocessorBE.codeSegment), (short) instructionSize);
            //first register type info
            if(((memorySeq[addressInfoOffset] >>> 1) & 1) == AddressingVariants.REGISTERS.getAddressType()){
                firstAddress.addressVariant = AddressingVariants.REGISTERS;
            } else {
                firstAddress.addressVariant = AddressingVariants.VALUE;
                firstAddress.IsAddress16bit = true;
            }
            firstAddress.offset = (memorySeq[addressInfoOffset] >>> 6) & 3;
            firstAddress.useValueAsAddress = ((memorySeq[addressInfoOffset] >>> 3) & 1) == 1;
            //second register type info
            if(((memorySeq[addressInfoOffset]) & 1) == AddressingVariants.REGISTERS.getAddressType()){
                secondAddress.addressVariant = AddressingVariants.REGISTERS;
            } else {
                secondAddress.addressVariant = AddressingVariants.VALUE;
                secondAddress.IsAddress16bit = true;
            }
            secondAddress.offset = (memorySeq[addressInfoOffset] >>> 4) & 3;
            secondAddress.useValueAsAddress = ((memorySeq[addressInfoOffset] >>> 2) & 1) == 1;
            firstAddress.address = (Byte.toUnsignedInt(memorySeq[addressInfoOffset + 1]) << 8) +
                    Byte.toUnsignedInt(memorySeq[addressInfoOffset + 2]);
            secondAddress.address = (Byte.toUnsignedInt(memorySeq[addressInfoOffset + 3]) << 8) +
                    Byte.toUnsignedInt(memorySeq[addressInfoOffset + 4]);
            //setting registers resolution
            if(firstAddress.addressVariant == AddressingVariants.REGISTERS){
                firstAddress.IsAddress16bit = !(lowBitRegisters.containsKey((byte)firstAddress.address) || highBitRegisters.containsKey((byte)firstAddress.address));
            }
            if(secondAddress.addressVariant == AddressingVariants.REGISTERS){
                secondAddress.IsAddress16bit = !(lowBitRegisters.containsKey((byte)secondAddress.address) || highBitRegisters.containsKey((byte)secondAddress.address));
            }
            //LogUtils.getLogger().info("first address type - " + String.valueOf(firstAddress.addressVariant));
            //LogUtils.getLogger().info("useValueAsAddress - " + String.valueOf(firstAddress.useValueAsAddress));
            //LogUtils.getLogger().info("16bit - " + String.valueOf(firstAddress.IsAddress16bit));
            //LogUtils.getLogger().info("first address - " + String.valueOf(firstAddress.address));
            //LogUtils.getLogger().info("second address type - " + String.valueOf(secondAddress.addressVariant));
            //LogUtils.getLogger().info("useValueAsAddress - " + String.valueOf(secondAddress.useValueAsAddress));
            //LogUtils.getLogger().info("16bit - " + String.valueOf(secondAddress.IsAddress16bit));
            //LogUtils.getLogger().info("second address - " + String.valueOf(secondAddress.address));
            //LogUtils.getLogger().info("value type - " + String.valueOf((power[addressInfoOffset] & AddressingVariants.PINS.getAddressType())));
        }
    }
    protected class AddressIO{
        MicrocontrollerBlockEntity processor;
        public final int lowWordMask = 255;
        protected HashMap<Byte, Byte> sectionTable = new HashMap<>();
        protected HashMap<Byte, Integer> pinsTable = new HashMap<>();
        public AddressIO(MicrocontrollerBlockEntity processor){
            this.processor = processor;
            int i = 1;
            for(Byte k : this.processor.segmentRegisters.keySet()){
                if(this.processor.segmentRegisters.get(k) != "codeSegment")
                    sectionTable.put((byte)i++, k);
            }
            int pinIndex = 0;
            for(Byte pinRegister : this.processor.redstonePins.keySet()){
                pinsTable.put(pinRegister, pinIndex++);
            }
        }
        public int getAbsoluteAdress(Address address){
            try {
                if (sectionTable.containsKey((byte)address.offset) && processor.segmentRegisters.containsKey(sectionTable.get((byte)address.offset))) {
                    return address.address + (int) processor.getClass().getField(processor.segmentRegisters.get(sectionTable.get((byte)address.offset))).getShort(processor);
                }
                return address.address;
            } catch (Exception ex){
                return address.address;
            }
        }
        public int getValueFromAddress(Address address){
            if(address == null) return 0;
            if(address.addressVariant == AddressingVariants.REGISTERS){
                try{
                    if(!address.useValueAsAddress) {
                        if (processor.registers.containsKey((byte) address.address)) {
                            return (int) processor.getClass().getField(processor.registers.get((byte) address.address)).getShort(processor) + (sectionTable.containsKey((byte)address.offset) ? (int) processor.getClass().getField(processor.segmentRegisters.get(sectionTable.get((byte)address.offset))).getShort(processor) : 0);
                        } else if (processor.lowBitRegisters.containsKey((byte) address.address)) {
                            return  (int) (processor.getClass().getField(processor.lowBitRegisters.get((byte)address.address)).getShort(processor) & 0xFF) + (sectionTable.containsKey((byte)address.offset) ? (int) processor.getClass().getField(processor.segmentRegisters.get(sectionTable.get((byte)address.offset))).getShort(processor) : 0);
                        } else if (processor.highBitRegisters.containsKey((byte) address.address)) {
                            return  (int) (processor.getClass().getField(processor.highBitRegisters.get((byte)address.address)).getShort(processor) >>> 8) + (sectionTable.containsKey((byte)address.offset) ? (int) processor.getClass().getField(processor.segmentRegisters.get(sectionTable.get((byte)address.offset))).getShort(processor) : 0);
                        } else if (processor.segmentRegisters.containsKey((byte) address.address)) {
                            return (int) processor.getClass().getField(processor.segmentRegisters.get((byte) address.address)).getShort(processor) + (sectionTable.containsKey((byte)address.offset) ? (int) processor.getClass().getField(processor.segmentRegisters.get(sectionTable.get((byte)address.offset))).getShort(processor) : 0);
                        } else if (processor.redstonePins.containsKey((byte) address.address) && pinsTable.containsKey((byte)address.address)) {
                            return processor.power[pinsTable.get((byte)address.address)] + (sectionTable.containsKey((byte)address.offset) ? (int) processor.getClass().getField(processor.segmentRegisters.get(sectionTable.get((byte)address.offset))).getShort(processor) : 0);
                        }
                    }
                    else {
                        int oldAddress = address.address;
                        address.useValueAsAddress = false;
                        int offset = address.offset;
                        address.offset = 0;
                        address.address = (short)getValueFromAddress(address);
                        address.offset = offset;
                        address.addressVariant = AddressingVariants.VALUE;
                        address.useValueAsAddress = true;
                        address.address = getAbsoluteAdress(address);
                        address.offset = 0;
                        int value = getValueFromAddress(address);
                        address.offset = offset;
                        address.addressVariant = AddressingVariants.REGISTERS;
                        address.address = oldAddress;
                        return value;
                    }
                }
                catch (Exception exception){
                    LogUtils.getLogger().info("address getting failed: " + exception.toString());
                    return 0;
                }
            } else if(address.addressVariant == AddressingVariants.VALUE){
                if(!address.useValueAsAddress){
                    return getAbsoluteAdress(address);
                }
                else{
                    if(!address.IsAddress16bit){
                        int oldAddress = address.address;
                        address.address = getAbsoluteAdress(address);
                        int offset = address.offset;
                        address.offset = 0;
                        Byte value = memory.readValue(address.address);
                        address.address = oldAddress;
                        address.offset = offset;
                        return value;
                    }
                    else{
                        int oldAddress = address.address;
                        address.address = getAbsoluteAdress(address);
                        int offset = address.offset;
                        address.offset = 0;
                        Short value = (short)((Byte.toUnsignedInt(memory.readValue(address.address)) << 8) + Byte.toUnsignedInt(memory.readValue(address.address + 1)));
                        address.address = oldAddress;
                        address.offset = offset;
                        return value;
                    }
                }
            }
            return 0;
        }
        public void setValueToAddress(Address address, int value){
            if(address == null) return;
            if(address.addressVariant == AddressingVariants.REGISTERS){
                try{
                    if(!address.useValueAsAddress) {
                        if (processor.registers.containsKey((byte) address.address)) {
                            processor.getClass().getField(processor.registers.get((byte) getAbsoluteAdress(address))).setShort(processor, (short) value);
                        } else if (processor.lowBitRegisters.containsKey((byte) address.address)) {
                            processor.getClass().getField(processor.lowBitRegisters.get((byte) getAbsoluteAdress(address))).setByte(processor, (byte)(value & 0xFF));
                        } else if (processor.highBitRegisters.containsKey((byte) address.address)) {
                            processor.getClass().getField(processor.highBitRegisters.get((byte) getAbsoluteAdress(address))).setShort(processor, (short)(value << 8));
                        } else if (processor.segmentRegisters.containsKey((byte) address.address)) {
                            processor.getClass().getField(processor.segmentRegisters.get((byte) getAbsoluteAdress(address))).setShort(processor, (short) value);
                        } else if (processor.redstonePins.containsKey((byte) address.address) && pinsTable.containsKey((byte)address.address)) {
                            processor.outputPins[pinsTable.get((byte)getAbsoluteAdress(address))] = (byte)value;
                        }
                    } else {
                        int oldAddress = address.address;
                        address.useValueAsAddress = false;
                        int offset = address.offset;
                        address.offset = 0;
                        address.address = (short)getValueFromAddress(address);
                        address.offset = offset;
                        address.addressVariant = AddressingVariants.VALUE;
                        address.useValueAsAddress = true;
                        address.address = getAbsoluteAdress(address);
                        address.offset = 0;
                        setValueToAddress(address, value);
                        address.offset = offset;
                        address.addressVariant = AddressingVariants.REGISTERS;
                        address.address = oldAddress;
                    }
                }
                catch (Exception exception){
                    LogUtils.getLogger().info("address setting failed: " + exception.toString());
                }
            } else if (address.addressVariant == AddressingVariants.VALUE && address.useValueAsAddress) {
                int oldAddress = address.address;
                address.useValueAsAddress = false;
                address.address = getAbsoluteAdress(address);
                int offset = address.offset;
                address.offset = 0;
                short memoryAddress = (short)getValueFromAddress(address);
                address.offset = offset;
                address.address = oldAddress;
                address.useValueAsAddress = true;
                //LogUtils.getLogger().info(String.valueOf((short)memoryAddress));
                if(!address.IsAddress16bit)
                    memory.setValue(Integer.valueOf(memoryAddress), Byte.valueOf((byte)value));
                else{
                    memory.setValue(Integer.valueOf(memoryAddress), Byte.valueOf((byte)(value >>> 8)));
                    memory.setValue(Integer.valueOf(memoryAddress + 1), Byte.valueOf((byte)(value & (lowWordMask))));
                }
            }
        }
    }
    public void applySettings(){
        final byte forwardSideOffset = 0;
        final byte rightSideOffset = 1;
        final byte backwardSideOffset = 2;
        final byte leftSideOffset = 3;
        final byte maxCableSignal = 31;
        final int channelBits = 4;
        int address = 0;
        int value = 0;
        final byte chanelSideCount = powerPinsCount / 4;

        address = ((short) power[backwardSideOffset * chanelSideCount] << channelBits * 3) +
                ((short) power[backwardSideOffset * chanelSideCount + 1] << channelBits * 2) +
                ((short) power[backwardSideOffset * chanelSideCount + 2] << channelBits) +
                (short) power[backwardSideOffset * chanelSideCount + 3];
        value = (power[backwardSideOffset * chanelSideCount + 4] << channelBits) + power[backwardSideOffset * chanelSideCount + 5];
        //Reset flag
        if(power[backwardSideOffset * chanelSideCount + 9] == 0) {
            outputPins = new byte[powerPinsCount];
            registerA = 0;
            registerB = 0;
            registerC = 0;
            registerD = 0;
            registerSp = 0;
            registerBp = 0;
            registerDi = 0;
            registerSi = 0;
            registerIp = 0;
            codeSegment = 0;
            dataSegment = 0;
            stackSegment = 0;
            extraSegment = 0;
            registerI = 0;
            startFlag = false;
            flags = 0;
            if (!this.level.isClientSide) setChanged();
            return;
        } else {
            if(!startFlag) {
                codeSegment = (short)address;
            }
            startFlag = true;
        }
        //Read flag
        if(ControllerMath.digitalize(power[backwardSideOffset * chanelSideCount + 7]) == maxCableSignal){
            outputPins[backwardSideOffset * chanelSideCount + 4] = (byte)(((memory.readValue(address) & 0xFF) >>> channelBits) + 2);
            outputPins[backwardSideOffset * chanelSideCount + 5] = (byte)((memory.readValue(address) & 0xF) + 2);
        } else {
            outputPins[backwardSideOffset * chanelSideCount + 4] = 0;
            outputPins[backwardSideOffset * chanelSideCount + 5] = 0;
        }
        //Address Write
        if(ControllerMath.digitalize(power[backwardSideOffset * chanelSideCount + 6]) == maxCableSignal){
            memory.setValue(Integer.valueOf(address), Byte.valueOf((byte) value));
            //LogUtils.getLogger().info("vriting value - " + String.valueOf(value) + "\thigh - " + String.valueOf(power[backwardSideOffset * chanelSideCount + 4] << channelBits) + "\tlow - " + String.valueOf(power[backwardSideOffset * chanelSideCount + 5]));
        }
        changeIOState();
        //Exec flag
        if(ControllerMath.digitalize(power[backwardSideOffset * chanelSideCount + 8]) == maxCableSignal){
            executeInstruction((short)((codeSegment & 0xFFFF) + (registerIp & 0xFFFF)));
            registerIp = (short)((registerIp & 0xFFFF) + instructionSize);
        }
    }
    private void applyFlags(int result, Address destinationAddress){
        short newFlags = 0;
        if(result == 0)
            newFlags |= 4; //0100b - ZF
        if(result != (short)result){
            newFlags |= 8; //1000b - CF
        }
        if(((result & (1 << 15)) == (1 << 15) && destinationAddress.IsAddress16bit) || ((result & (1 << 7)) == (1 << 7) && !destinationAddress.IsAddress16bit)){
            newFlags |= 2; //0010b - SF
        }
        newFlags |= flags & 1;
        flags = newFlags;
    }
    public void jumpToAddress(Address address, int stepFix){
        registerIp = (short)(address.address - stepFix);
        switch (address.offset){
            case 2:{
                codeSegment = stackSegment;
                break;
            }
            case 3:{
                codeSegment = extraSegment;
                break;
            }
        }
    }
    public void pushStack(int value){
        AddressIO addressIO = new AddressIO(this);
        registerSp -= 2;
        Address stackAddress = new Address();
        stackAddress.address = stackSegment + registerSp;
        stackAddress.useValueAsAddress = true;
        stackAddress.addressVariant = AddressingVariants.VALUE;
        stackAddress.IsAddress16bit = true;
        addressIO.setValueToAddress(stackAddress, value);
    }
    public int popStack(){
        AddressIO addressIO = new AddressIO(this);
        Address stackAddress = new Address();
        stackAddress.address = stackSegment + registerSp;
        stackAddress.useValueAsAddress = true;
        stackAddress.addressVariant = AddressingVariants.VALUE;
        stackAddress.IsAddress16bit = true;
        int value = addressIO.getValueFromAddress(stackAddress);
        addressIO.setValueToAddress(stackAddress, 0);
        registerSp += 2;
        return value;
    }
    public void call(Address address){
        pushStack(registerIp);
        if(address.offset != 0) pushStack(codeSegment);
        jumpToAddress(address, instructionSize);
    }
    public void executeInstruction(short address){
        Address firstAddress = new Address();
        Address secondAddress = new Address();
        AddressIO addressIO = new AddressIO(this);
        final byte chanelSideCount = powerPinsCount / 4;
        byte[] commandSeq = memory.getMemorySequence(address, chanelSideCount);
        String byteinfo = "";
        for(byte b : commandSeq){
            byteinfo = byteinfo.concat(String.valueOf((int)b));
        }
        //LogUtils.getLogger().info(byteinfo);
        CommandHandler commandHandler = new CommandHandler();
        AddressReader addreader = new AddressReader(this, instructionSize, commandletSize);

        addreader.calculateAddressTypes();
        firstAddress = addreader.firstAddress;
        secondAddress = addreader.secondAddress;
        try {
            switch (commandHandler.getAction(commandSeq[0])) {
                case MOV: {
                    //LogUtils.getLogger().info("MOV command execution");
                    int secondValue = addressIO.getValueFromAddress(secondAddress);
                    addressIO.setValueToAddress(firstAddress, secondValue);
                    break;
                }
                case ADD: {
                    //LogUtils.getLogger().info("ADD command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) + addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case SUB: {
                    //LogUtils.getLogger().info("SUB command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) - addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case MUL: {
                    //LogUtils.getLogger().info("MUL command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) * addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case DIV: {
                    //LogUtils.getLogger().info("DIV command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) / addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case INC: {
                    //LogUtils.getLogger().info("INC command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) + 1;
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case DEC: {
                    //LogUtils.getLogger().info("DEC command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) - 1;
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case AND: {
                    //LogUtils.getLogger().info("AND command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) & addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case OR: {
                    //LogUtils.getLogger().info("OR command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) | addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case XOR: {
                    //LogUtils.getLogger().info("XOR command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) ^ addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case NOT: {
                    //LogUtils.getLogger().info("NOT command execution");
                    int newValue = ~addressIO.getValueFromAddress(firstAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case CMP: {
                    //LogUtils.getLogger().info("CMP command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) - addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    break;
                }
                case JMP: {
                    //LogUtils.getLogger().info("JMP command execution " + String.valueOf((short) addressIO.getValueFromAddress(firstAddress)));
                    jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JZ: {
                    //LogUtils.getLogger().info("JZ/JE command execution");
                    if ((flags & 4) == 4) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JNZ: {
                    //LogUtils.getLogger().info("JNZ command execution");
                    if ((flags & 4) != 4) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JG: {
                    //LogUtils.getLogger().info("JG command execution");
                    if ((flags & 6) == 0) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JL: {
                    //LogUtils.getLogger().info("JL command execution");
                    if ((flags & 2) == 2) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JGE: {
                    //LogUtils.getLogger().info("JGE command execution");
                    if ((flags & 2) == 0 || (flags & 4) == 4) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JLE: {
                    //LogUtils.getLogger().info("JLE command execution");
                    if ((flags & 2) == 2 || (flags & 4) == 4) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JC: {
                    //LogUtils.getLogger().info("JC command execution");
                    if ((flags & 8) == 8) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case JNC: {
                    //LogUtils.getLogger().info("JNC command execution");
                    if ((flags & 8) != 8) jumpToAddress(firstAddress, instructionSize);
                    break;
                }
                case SHL: {
                    //LogUtils.getLogger().info("SHL command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) << addressIO.getValueFromAddress(secondAddress);
                    if (firstAddress.IsAddress16bit)
                        newValue &= 65535; //1111 1111 1111 1111
                    else newValue &= 255; //1111 1111
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case SHR: {
                    //LogUtils.getLogger().info("SHR command execution");
                    int firstAddressValue = addressIO.getValueFromAddress(firstAddress);
                    if (firstAddress.IsAddress16bit)
                        firstAddressValue &= 65535;
                    else firstAddressValue &= 255;
                    int newValue = firstAddressValue >>> addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case SAR: {
                    //LogUtils.getLogger().info("SAR command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress) >> addressIO.getValueFromAddress(secondAddress);
                    applyFlags(newValue, firstAddress);
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case ROL: {
                    //LogUtils.getLogger().info("ROL command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress);
                    int roll = 0;
                    int rollValue = 0;
                    if (firstAddress.IsAddress16bit) {
                        newValue &= 65535; //1111 1111 1111 1111
                        rollValue = addressIO.getValueFromAddress(secondAddress) % 16;
                        roll = newValue >>> (16 - rollValue);
                        flags |= (roll & 1) << 3;
                        newValue <<= rollValue;
                        newValue += roll;
                        newValue &= 65535; //1111 1111 1111 1111
                    } else {
                        newValue &= 255; //1111 1111
                        rollValue = addressIO.getValueFromAddress(secondAddress) % 8;
                        roll = newValue >>> (8 - rollValue);
                        flags |= (roll & 1) << 3;
                        newValue <<= rollValue;
                        newValue += roll;
                        newValue &= 255; //1111 1111
                    }
                    addressIO.setValueToAddress(firstAddress, newValue);
                    //applyFlags(newValue, firstAddress);
                    break;
                }
                case ROR: {
                    //LogUtils.getLogger().info("ROR command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress);
                    int roll = 0;
                    int rollValue = 0;
                    if (firstAddress.IsAddress16bit) {
                        newValue &= 65535; //1111 1111 1111 1111
                        rollValue = addressIO.getValueFromAddress(secondAddress) % 16;
                        roll = (newValue & (int) (Math.pow(2, rollValue) - 1)) << (16 - rollValue);
                        if (rollValue > 0)
                            flags |= ((newValue >>> (rollValue - 1)) & 1) << 3;
                        newValue >>>= rollValue;
                        newValue += roll;
                        newValue &= 65535; //1111 1111 1111 1111
                    } else {
                        newValue &= 255; //1111 1111
                        rollValue = addressIO.getValueFromAddress(secondAddress) % 8;
                        roll = (newValue & (int) (Math.pow(2, rollValue) - 1)) << (8 - rollValue);
                        if (rollValue > 0)
                            flags |= ((newValue >>> (rollValue - 1)) & 1) << 3;
                        newValue >>>= rollValue;
                        newValue += roll;
                        newValue &= 255; //1111 1111
                    }
                    addressIO.setValueToAddress(firstAddress, newValue);
                    //applyFlags(newValue, firstAddress);
                    break;
                }
                case RCL: {
                    //LogUtils.getLogger().info("RCL command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress);
                    int cflag = (flags & 8) >>> 3;
                    if (firstAddress.IsAddress16bit) {
                        newValue &= 65535; //1111 1111 1111 1111
                        for (int i = 0; i < addressIO.getValueFromAddress(secondAddress); i++) {
                            int oldCFlag = cflag;
                            cflag = newValue >>> 15;
                            newValue <<= 1;
                            newValue += oldCFlag;
                        }
                    } else {
                        newValue &= 255; //1111 1111
                        for (int i = 0; i < addressIO.getValueFromAddress(secondAddress); i++) {
                            int oldCFlag = cflag;
                            cflag = newValue >>> 7;
                            newValue <<= 1;
                            newValue += oldCFlag;
                        }
                    }
                    flags |= cflag << 3;
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case RCR: {
                    //LogUtils.getLogger().info("RCR command execution");
                    int newValue = addressIO.getValueFromAddress(firstAddress);
                    int cflag = (flags & 8) >>> 3;
                    if (firstAddress.IsAddress16bit) {
                        newValue &= 65535; //1111 1111 1111 1111
                    } else {
                        newValue &= 255; //1111 1111
                    }
                    for (int i = 0; i < addressIO.getValueFromAddress(secondAddress); i++) {
                        int oldCFlag = cflag;
                        cflag = newValue & 1;
                        newValue >>>= 1;
                        newValue += oldCFlag;
                    }
                    flags |= cflag << 3;
                    addressIO.setValueToAddress(firstAddress, newValue);
                    break;
                }
                case PUSH: {
                    //LogUtils.getLogger().info("PUSH command execution");
                    pushStack(addressIO.getValueFromAddress(firstAddress));
                    break;
                }
                case POP: {
                    //LogUtils.getLogger().info("POP command execution");
                    addressIO.setValueToAddress(firstAddress, popStack());
                    break;
                }
                case CALL: {
                    //LogUtils.getLogger().info("CALL command execution");
                    call(firstAddress);
                    break;
                }
                case RET: {
                    //LogUtils.getLogger().info("RET command execution");
                    int clearCount = addressIO.getValueFromAddress(firstAddress);
                    for (int i = 0; i < clearCount; i++) {
                        popStack();
                    }
                    if (addressIO.getValueFromAddress(secondAddress) == 0xFF || addressIO.getValueFromAddress(secondAddress) == 1)
                        codeSegment = (short) popStack();
                    registerIp = (short) popStack();
                    if (addressIO.getValueFromAddress(secondAddress) == 0xFF)
                        registerIp = (short) ((registerIp & 0xFFFF) - instructionSize);
                    break;
                }
                case INT: {
                    //LogUtils.getLogger().info("INT command execution");
                    MinecontrollersAPI.invokeInterrupt(this, addressIO.getValueFromAddress(firstAddress));
                    break;
                }
                case LEA: {
                    //LogUtils.getLogger().info("LEA command execution");
                    boolean sumIndex = false;
                    boolean sumDisp = false;
                    int addressInfo = addressIO.getValueFromAddress(firstAddress);
                    int disp = addressIO.getValueFromAddress(secondAddress);
                    int sourceRegisterAddress = addressInfo >>> 12;
                    sumIndex = ((addressInfo >>> 11) & 1) == 1;
                    sumDisp = ((addressInfo >>> 10) & 1) == 1;
                    int sizeMode = (addressInfo >>> 8) & 3;
                    int baseRegisterAddress = ((addressInfo >>> 4) & 0xF) - 1;
                    int indexRegisterAddress = (addressInfo & 0xF) - 1;
                    Address baseRegister = new Address();
                    Address indexRegister = new Address();
                    baseRegister.IsAddress16bit = true;
                    indexRegister.IsAddress16bit = true;
                    baseRegister.address = baseRegisterAddress;
                    indexRegister.address = indexRegisterAddress;
                    int result = 0;
                    result += addressIO.getValueFromAddress(baseRegister);
                    if (sumIndex)
                        result += addressIO.getValueFromAddress(indexRegister) * (int) Math.pow(2, sizeMode);
                    else result -= addressIO.getValueFromAddress(indexRegister) * (int) Math.pow(2, sizeMode);
                    result = sumDisp ? result + disp : result - disp;
                    Address sourceRegister = new Address();
                    sourceRegister.address = sourceRegisterAddress;
                    sourceRegister.IsAddress16bit = true;
                    addressIO.setValueToAddress(sourceRegister, result);
                    break;
                }
                case PUSHA: {
                    //LogUtils.getLogger().info("PUHSA command execution");
                    pushAllRegisters();
                    break;
                }
                case POPA: {
                    //LogUtils.getLogger().info("POPA command execution");
                    popAllRegisters();
                    break;
                }
                case LOOP: {
                    //LogUtils.getLogger().info("LOOP command execution");
                    if (--registerC != 0) {
                        jumpToAddress(firstAddress, instructionSize);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (ArithmeticException exception){

        }
    }
    public void popAllRegisters(){
        extraSegment = (short)popStack();
        dataSegment = (short)popStack();
        flags = (short)popStack();
        registerSi = (short)popStack();
        registerDi = (short)popStack();
        registerD = (short)popStack();
        registerC = (short)popStack();
        registerB = (short)popStack();
        registerA = (short)popStack();
    }
    public void pushAllRegisters(){
        pushStack(registerA);
        pushStack(registerB);
        pushStack(registerC);
        pushStack(registerD);
        pushStack(registerDi);
        pushStack(registerSi);
        pushStack(flags);
        pushStack(dataSegment);
        pushStack(extraSegment);
    }
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putByteArray("power", (byte[])this.power.clone());
        compound.putByteArray("outputPins", (byte[])this.outputPins.clone());
        compound.putShort("AX", registerA);
        compound.putShort("BX", registerB);
        compound.putShort("CX", registerC);
        compound.putShort("DX", registerD);
        compound.putShort("IP", registerIp);
        compound.putShort("CS", codeSegment);
        compound.putShort("DS", dataSegment);
        compound.putShort("SS", stackSegment);
        compound.putShort("SP", registerSp);
        compound.putShort("BP", registerBp);
        compound.putShort("DI", registerDi);
        compound.putShort("SI", registerSi);
        compound.putShort("FLAGS", flags);
        compound.putShort("ES", extraSegment);
        compound.putBoolean("isActive", startFlag);
        compound.putShort("IT", registerI);
        CompoundTag memoryCompound = new CompoundTag();
        for (int address : memory.getUsedAddresses()) {
            memoryCompound.putByte(String.valueOf((int) address), memory.readValue(address));
        }
        compound.put("memory", memoryCompound);
    }
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        byte[] newPower = compound.getByteArray("power");
        byte[] newOutputPower = compound.getByteArray("outputPins");
        if (newPower.length == powerPinsCount) {
            this.power = (byte[])newPower.clone();
        }
        if(newOutputPower.length == powerPinsCount){
            this.outputPins = newOutputPower.clone();
        }
        this.registerA = compound.getShort("AX");
        this.registerB = compound.getShort("BX");
        this.registerC = compound.getShort("CX");
        this.registerD = compound.getShort("DX");
        this.registerIp = compound.getShort("IP");
        this.codeSegment = compound.getShort("CS");
        this.dataSegment = compound.getShort("DS");
        this.stackSegment = compound.getShort("SS");
        this.registerSp = compound.getShort("SP");
        this.registerBp = compound.getShort("BP");
        this.registerDi = compound.getShort("DI");
        this.registerSi = compound.getShort("SI");
        this.extraSegment = compound.getShort("ES");
        this.flags = compound.getShort("FLAGS");
        this.startFlag = compound.getBoolean("isActive");
        for(String address : compound.getCompound("memory").getAllKeys()){
            memory.setValue(Integer.valueOf(address), compound.getCompound("memory").getByte(address));
        }
        this.registerI = compound.getShort("IT");
    }
    @Override
    public byte[] getStrongestNeighborPower() {
        return new byte[powerPinsCount];
    }
}
