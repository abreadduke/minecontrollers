package com.abadon.minecontrollers.api;

import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlockEntity;

@FunctionalInterface
public interface InterruptSupplier {
    void interruptFunction(MicrocontrollerBlockEntity microcontrollerBlockEntity);
}
