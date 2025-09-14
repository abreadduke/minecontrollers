package com.abadon.minecontrollers.blocks.dumper;

public class CodeDumper {
    protected StringBuilder dumpedCode = new StringBuilder();
    public String build(byte[] data){
        int newLines = 0;
        for(byte b : data){
            String dumpedByte = Integer.toString((int)b & 0xFF, 16);
            dumpedByte = dumpedByte.length() == 1 ? "0" + dumpedByte : dumpedByte;
            dumpedCode.append(dumpedByte);
            if((dumpedCode.length() - newLines) % 12 == 0){
                dumpedCode.append('\n');
                newLines++;
            }
        }
        return dumpedCode.toString();
    }
}
