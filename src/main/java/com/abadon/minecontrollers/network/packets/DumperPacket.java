package com.abadon.minecontrollers.network.packets;

public class DumperPacket {
    public int from = 0;
    public int to = 0;
    public DumperPacket(int from, int to){
        this.from = from;
        this.to = to;
    }
}
