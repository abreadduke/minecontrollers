package com.abadon.minecontrollers.blocks.disassembler;

import com.abadon.minecontrollers.utils.CommandAction;
import java.util.HashMap;
import java.util.Set;

public class CodeDisassembler {
    protected StringBuilder asmCodeBuilder = new StringBuilder();
    protected HashMap<Integer, String> registersTable = new HashMap();
    protected HashMap<String, String> opcodesTable = new HashMap<>();
    protected HashMap<Integer, String> generatedLabels = new HashMap<>();
    protected HashMap<String, String> generatedSections = new HashMap<>();
    protected HashMap<Character, Integer> sectionsAddressesTable = new HashMap<>();
    protected int actualDSIndex = 0;
    protected int actualESIndex = 0;
    protected int actualSSIndex = 0;
    private int labelsCounter = 0;
    private int sectionsCounter = 0;
    private int linesAddressCount = 0;
    private int linesCount = 0;
    protected void initOpcodes(){
        for(CommandAction opcode : CommandAction.values()){
            String code = Integer.toString(opcode.ordinal(), 16);
            if(code.length() == 1){
                code = "0" + code;
            }
            opcodesTable.put(code, opcode.name().toLowerCase());
        }
    }
    protected void initRegisters(){
        registersTable.put(0, "ax");
        registersTable.put(1, "ah");
        registersTable.put(2, "al");
        registersTable.put(3, "bx");
        registersTable.put(4, "bh");
        registersTable.put(5, "bl");
        registersTable.put(6, "cx");
        registersTable.put(7, "ch");
        registersTable.put(8, "cl");
        registersTable.put(9, "dx");
        registersTable.put(10, "dh");
        registersTable.put(11, "dl");
        registersTable.put(12, "di");
        registersTable.put(13, "si");
        registersTable.put(14, "bp");
        registersTable.put(15, "sp");
        registersTable.put(16, "ip");
        registersTable.put(17, "flags");
        registersTable.put(18, "ds");
        registersTable.put(19, "ss");
        registersTable.put(20, "cs");
        registersTable.put(21, "es");
        for(int i = 0; i < 64; i++) registersTable.put(22 + i, "r" + i);
        registersTable.put(86, "it");
    }
    protected void init(){
        initOpcodes();
        initRegisters();
    }
    protected String getOffsetPrefix(int offsetMod){
        switch (offsetMod){
            case 1:{
                return "ds:";
            }
            case 2:{
                return "ss:";
            }
            case 3:{
                return "es:";
            }
            default:{
                return "";
            }
        }
    }
    protected boolean isValueSegmentRegister(String value){
        return (value.equals("ds") || value.equals("ss") || value.equals("es") || value.equals("cs"));
    }
    protected boolean isOpcodeJumpCommand(String opcode){
        return (opcode.equals("jmp") || opcode.equals("jz") || opcode.equals("jnz")|| opcode.equals("jg")|| opcode.equals("jge")|| opcode.equals("jl")|| opcode.equals("jle")|| opcode.equals("jc")|| opcode.equals("jnc")|| opcode.equals("loop") || opcode.equals("call"));
    }
    protected boolean isOpcodeHasSoloOperand(String opcode){
        return (isOpcodeJumpCommand(opcode) || opcode.equals("inc") || opcode.equals("dec") || opcode.equals("int") || opcode.equals("push") || opcode.equals("pop"));
    }
    protected boolean isOpcodeHasZeroOperands(String opcode){
        return (opcode.equals("pusha") || opcode.equals("popa"));
    }
    protected int getPolishedIndexOfLabel(String offset, String label){
        switch (offset){
            case "ds:":{
                return Integer.parseInt(label) + actualDSIndex;
            }
            case "es:":{
                return Integer.parseInt(label) + actualESIndex;
            }
            case "ss:":{
                return Integer.parseInt(label) + actualSSIndex;
            }
        }
        return Integer.parseInt(label);
    }
    public String lineTranslator(String line){
        String commentAddress = Integer.toString(linesAddressCount, 16);
        commentAddress = "0".repeat(4 - commentAddress.length()) + commentAddress;
        linesAddressCount += line.length()/2;
        commentAddress = "; " + commentAddress.toUpperCase() + ":" + linesCount + '\n';
        if(line.length() < 12) return line + commentAddress;
        StringBuilder decompiledLineBuilder = new StringBuilder();
        String opcode = line.substring(0, 2).toLowerCase();
        String mod = line.substring(2, 4).toLowerCase();
        int imod = Integer.valueOf(mod, 16);
        String firstOffset = getOffsetPrefix(imod >>> 6);
        String secondOffset = getOffsetPrefix((imod >>> 4) & 3);
        int firstOperand = Integer.valueOf(line.substring(4,8).toLowerCase(), 16);
        int secondOperand = Integer.valueOf(line.substring(8, 12).toLowerCase(), 16);
        boolean firstOperandIsValue = false;
        boolean secondOperandIsValue = false;
        String parsedFirstOperand = "";
        String parsedSecondOperand = "";

        if(((imod >>> 1) & 1) == 1){
            parsedFirstOperand = String.valueOf(firstOperand);
            firstOperandIsValue = true;
        } else {
            if(registersTable.containsKey(firstOperand))
                parsedFirstOperand = registersTable.get(firstOperand);
        }
        if((imod & 1) == 1){
            secondOperandIsValue = true;
            parsedSecondOperand = String.valueOf(secondOperand);
        } else {
            if(registersTable.containsKey(secondOperand))
                parsedSecondOperand = registersTable.get(secondOperand);
        }

        //removing ds offset from jump commands
        if(isOpcodeJumpCommand(opcodesTable.get(opcode)) && firstOffset.equals("ds:")){
            firstOffset = "cs:";
        }
        //defining of label/procedure prefix
        String labelPrefix = "l";
        if(opcodesTable.get(opcode).equals("call")) labelPrefix = "p";
        //creating links and generating labels
        if(((imod >>> 3) & 1) == 1){
            if(firstOperandIsValue){
                if(!generatedLabels.containsKey(getPolishedIndexOfLabel(firstOffset, parsedFirstOperand))){
                    generatedLabels.put(getPolishedIndexOfLabel(firstOffset, parsedFirstOperand), labelPrefix + labelsCounter++);
                }
                parsedFirstOperand = "[" + generatedLabels.get(getPolishedIndexOfLabel(firstOffset, parsedFirstOperand)) + "]";
            } else {
                parsedFirstOperand = "[" + parsedFirstOperand + "]";
            }
        } else if (!firstOffset.isEmpty()) {
            if(firstOperandIsValue){
                if(!generatedLabels.containsKey(getPolishedIndexOfLabel(firstOffset, parsedFirstOperand))){
                    generatedLabels.put(getPolishedIndexOfLabel(firstOffset, parsedFirstOperand), labelPrefix + labelsCounter++);
                }
                parsedFirstOperand = generatedLabels.get(getPolishedIndexOfLabel(firstOffset, parsedFirstOperand));
            }
        }
        if(((imod >>> 2) & 1) == 1){
            if(secondOperandIsValue){
                if(!generatedLabels.containsKey(getPolishedIndexOfLabel(secondOffset, parsedSecondOperand))){
                    generatedLabels.put(getPolishedIndexOfLabel(secondOffset, parsedSecondOperand), labelPrefix + labelsCounter++);
                }
                parsedSecondOperand = "[" + generatedLabels.get(getPolishedIndexOfLabel(secondOffset, parsedSecondOperand)) + "]";
            } else {
                parsedSecondOperand = "[" + parsedSecondOperand + "]";
            }
        } else if (!secondOffset.isEmpty()) {
            if(secondOperandIsValue){
                if(!generatedLabels.containsKey(getPolishedIndexOfLabel(secondOffset, parsedSecondOperand))){
                    generatedLabels.put(getPolishedIndexOfLabel(secondOffset, parsedSecondOperand), labelPrefix + labelsCounter++);
                }
                parsedSecondOperand = generatedLabels.get(getPolishedIndexOfLabel(secondOffset, parsedSecondOperand));
            }
        }
        //removing cs offset from jump commands
        if(isOpcodeJumpCommand(opcodesTable.get(opcode)) && firstOffset.equals("cs:")){
            firstOffset = "";
        }
        //generating result line
        if(opcodesTable.containsKey(opcode)){
            //generating sections
            if(opcodesTable.get(opcode).equals("mov") && isValueSegmentRegister(parsedFirstOperand)){
                if(!generatedSections.containsKey(parsedSecondOperand)){
                    generatedSections.put(parsedSecondOperand, "s" + sectionsCounter++);
                    //applying actual data of segment registers
                    switch (parsedFirstOperand){
                        case "ds":{
                            actualDSIndex = Integer.parseInt(parsedSecondOperand);
                            sectionsAddressesTable.put('d', actualDSIndex);
                            break;
                        }
                        case "es":{
                            actualESIndex = Integer.parseInt(parsedSecondOperand);
                            sectionsAddressesTable.put('e', actualESIndex);
                            break;
                        }
                        case "ss":{
                            actualSSIndex = Integer.parseInt(parsedSecondOperand);
                            sectionsAddressesTable.put('s', actualSSIndex);
                            break;
                        }
                    }
                }
                parsedSecondOperand = generatedSections.get(parsedSecondOperand);
            }
            decompiledLineBuilder.append(opcodesTable.get(opcode));
            if(isOpcodeHasZeroOperands(opcodesTable.get(opcode))){

                return decompiledLineBuilder.append(commentAddress).toString();
            } else if (isOpcodeHasSoloOperand(opcodesTable.get(opcode))) {
                return decompiledLineBuilder.append(' ').append(firstOffset).append(parsedFirstOperand).append(commentAddress).toString();
            }
            return decompiledLineBuilder.append(' ').append(firstOffset).append(parsedFirstOperand).append(", ").append(secondOffset).append(parsedSecondOperand).append(commentAddress).toString();
        }
        return "";
    }
    public CodeDisassembler(){
        init();
    }
    protected String insertLabels(String code){
        StringBuilder newCode = new StringBuilder();
        int byteCounter = 0;
        for(String line : code.split("\n")){
            for(String sectionIndex : generatedSections.keySet()){
                int index = Integer.parseInt(sectionIndex);
                if(index == byteCounter){
                    newCode.append("section ").append(generatedSections.get(sectionIndex)).append("\n");
                }
            }
            for(int labelIndex : generatedLabels.keySet()){
                if(labelIndex == byteCounter){
                    newCode.append(generatedLabels.get(labelIndex)).append(":\n");
                }
            }
            newCode.append(line).append("\n");
            //checking for data bytes
            if(line.indexOf(';') == 2)
                byteCounter++;
            else byteCounter+=6;
        }
        return newCode.toString();
    }
    protected StringBuilder codeBuffer = new StringBuilder();
    protected int bufferBytesCount = 0;
    protected int commandByteCounter = 0;
    protected void appendCodeToBuffer(String line){
        if(line.length() % 2 != 0) return;
        linesCount++;
        for(int i = 0; i < line.length(); i += 2){
            codeBuffer.append(line.charAt(i)).append(line.charAt(i+1));
            bufferBytesCount++;
            Character[] sectionsKeySet =  sectionsAddressesTable.keySet().toArray(new Character[]{});
            boolean isDataBytesArray = false;
            for(int s = 0; s < sectionsKeySet.length; s++){
                if(sectionsKeySet[s] == 'd' && sectionsAddressesTable.get(sectionsKeySet[s]) < bufferBytesCount){
                    if(s == sectionsKeySet.length - 1){
                        asmCodeBuilder.append(lineTranslator(codeBuffer.toString()));
                        codeBuffer = new StringBuilder();
                        isDataBytesArray = true;
                    } else if(sectionsKeySet[s+1] != 'd' && sectionsAddressesTable.get(sectionsKeySet[s+1]) >= bufferBytesCount){
                        asmCodeBuilder.append(lineTranslator(codeBuffer.toString()));
                        codeBuffer = new StringBuilder();
                        isDataBytesArray = true;
                    }
                }
            }
            if(!isDataBytesArray){
                commandByteCounter++;
                if(commandByteCounter >= 6){
                    asmCodeBuilder.append(lineTranslator(codeBuffer.toString()));
                    codeBuffer = new StringBuilder();
                    commandByteCounter = 0;
                }
            }
        }
    }
    public String disassembly(String mashineCodes){
        for(String codeline : mashineCodes.split("\\n+")){
            appendCodeToBuffer(codeline);
        }
        return insertLabels(asmCodeBuilder.toString());
    }
}
