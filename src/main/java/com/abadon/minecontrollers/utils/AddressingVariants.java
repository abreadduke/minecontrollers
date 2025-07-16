package com.abadon.minecontrollers.utils;

public enum AddressingVariants {
    REGISTERS((byte)0),
    VALUE((byte)1);

    private byte addressValue = 0;
    private AddressingVariants(byte addressValue){
        this.addressValue = addressValue;
    }
    public byte getAddressType(){
        return this.addressValue;
    }
}
