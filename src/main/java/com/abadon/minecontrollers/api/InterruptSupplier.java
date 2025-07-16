package com.abadon.minecontrollers.api;

import com.abadon.minecontrollers.entityblocks.microprocessor.MicrocontrollerBlockEntity;

@FunctionalInterface
public interface InterruptSupplier {
    void interruptFunction(MicrocontrollerBlockEntity microcontrollerBlockEntity);
}
