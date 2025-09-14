package com.abadon.minecontrollers.blocks.disassembler;

import com.abadon.minecontrollers.utils.CommandAction;
import java.util.HashMap;

public class CodeDisassembler {
    protected StringBuilder asmCodeBuilder = new StringBuilder();
    protected HashMap<Integer, String> registersTable = new HashMap();
    protected HashMap<String, String> opcodesTable = new HashMap<>();
    protected HashMap<Integer, String> generatedLabels = new HashMap<>();
    protected HashMap<String, String> generatedSections = new HashMap<>();
    protected HashMap<Integer, Integer> labelsIndexTable = new HashMap<>();
    protected int actualDSIndex = 0;
    protected int actualESIndex = 0;
    protected int actualSSIndex = 0;
    private int labelsCounter = 0;
    private int sectionsCounter = 0;
    private int labelsUniqueBaseIndex = 0;
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
        return (opcode.equals("jmp") || opcode.equals("jz") || opcode.equals("jnz")|| opcode.equals("jg")|| opcode.equals("jge")|| opcode.equals("jl")|| opcode.equals("jle")|| opcode.equals("jc")|| opcode.equals("jnc")|| opcode.equals("loop"));
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
        if(line.length() < 12) return line + "\n";
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
        //creating links and generating labels
        if(((imod >>> 3) & 1) == 1){
            //if(firstOffset.isEmpty()) firstOffset = "ds:";
            if(firstOperandIsValue){
                if(!generatedLabels.containsKey(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex) ||
                    (labelsIndexTable.containsKey(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex) &&
                    getPolishedIndexOfLabel(firstOffset, parsedFirstOperand) != labelsIndexTable.get(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex))){

                    generatedLabels.put(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex, "l" + labelsCounter++);
                    labelsIndexTable.put(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex, getPolishedIndexOfLabel(firstOffset, parsedFirstOperand));
                }
                parsedFirstOperand = "[" + generatedLabels.get(Integer.parseInt(parsedFirstOperand)+ labelsUniqueBaseIndex) + "]";
            } else {
                parsedFirstOperand = "[" + parsedFirstOperand + "]";
            }
        } else if (!firstOffset.isEmpty()) {
            if(firstOperandIsValue){
                if(!generatedLabels.containsKey(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex) ||
                    (labelsIndexTable.containsKey(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex) &&
                    getPolishedIndexOfLabel(firstOffset, parsedFirstOperand) != labelsIndexTable.get(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex))){

                    generatedLabels.put(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex, "l" + labelsCounter++);
                    labelsIndexTable.put(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex, getPolishedIndexOfLabel(firstOffset, parsedFirstOperand));
                }
                parsedFirstOperand = generatedLabels.get(Integer.parseInt(parsedFirstOperand)+labelsUniqueBaseIndex);
            }
        }
        if(((imod >>> 2) & 1) == 1){
            //if(secondOffset.isEmpty()) secondOffset = "ds:";
            if(secondOperandIsValue){
                if(!generatedLabels.containsKey(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1) ||
                    (labelsIndexTable.containsKey(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1) &&
                    getPolishedIndexOfLabel(secondOffset, parsedSecondOperand) != labelsIndexTable.get(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1))){
                    generatedLabels.put(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1, "l" + labelsCounter++);
                    labelsIndexTable.put(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1, getPolishedIndexOfLabel(secondOffset, parsedSecondOperand));
                }
                parsedSecondOperand = "[" + generatedLabels.get(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1) + "]";
            } else {
                parsedSecondOperand = "[" + parsedSecondOperand + "]";
            }
        } else if (!secondOffset.isEmpty()) {
            if(secondOperandIsValue){
                if(!generatedLabels.containsKey(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1) ||
                    (labelsIndexTable.containsKey(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1) &&
                    getPolishedIndexOfLabel(secondOffset, parsedSecondOperand) != labelsIndexTable.get(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1))){
                    generatedLabels.put(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1, "l" + labelsCounter++);
                    labelsIndexTable.put(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1, getPolishedIndexOfLabel(secondOffset, parsedSecondOperand));
                }
                parsedSecondOperand = generatedLabels.get(Integer.parseInt(parsedSecondOperand)+labelsUniqueBaseIndex+1);
            }
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
                            break;
                        }
                        case "es":{
                            actualESIndex = Integer.parseInt(parsedSecondOperand);
                            break;
                        }
                        case "ss":{
                            actualSSIndex = Integer.parseInt(parsedSecondOperand);
                            break;
                        }
                    }
                }
                parsedSecondOperand = generatedSections.get(parsedSecondOperand);
            }
            //generating labels for jumps
            //else if (isOpcodeJumpCommand(opcodesTable.get(opcode)) && firstOperandIsValue) {
            //    if(!generatedLabels.containsKey(parsedFirstOperand)){
            //        generatedLabels.put(parsedFirstOperand, "l" + labelsCounter++);
            //    }
            //    parsedFirstOperand = generatedLabels.get(parsedFirstOperand);
            //}
            labelsUniqueBaseIndex+=2;
            decompiledLineBuilder.append(opcodesTable.get(opcode));
            if(isOpcodeHasZeroOperands(opcodesTable.get(opcode))){
                return decompiledLineBuilder.append("\n").toString();
            } else if (isOpcodeHasSoloOperand(opcodesTable.get(opcode))) {
                return decompiledLineBuilder.append(' ').append(firstOffset).append(parsedFirstOperand).append("\n").toString();
            }
            return decompiledLineBuilder.append(' ').append(firstOffset).append(parsedFirstOperand).append(", ").append(secondOffset).append(parsedSecondOperand).append("\n").toString();
        }
        return "";
    }
    public CodeDisassembler(){
        init();
    }
    protected String insertLabels(String code){
        StringBuilder newCode = new StringBuilder();
        int lineCounter = 0;
        for(String line : code.split("\n")){
            for(String sectionIndex : generatedSections.keySet()){
                int index = Integer.parseInt(sectionIndex) / 6;
                if(index == lineCounter){
                    newCode.append("section ").append(generatedSections.get(sectionIndex)).append("\n");
                }
            }
            for(int labelIndex : generatedLabels.keySet()){
                //int index = Integer.parseInt(labelIndex) / 6;
                int index = labelsIndexTable.get(labelIndex) / 6;
                if(index == lineCounter){
                    newCode.append(generatedLabels.get(labelIndex)).append(":\n");
                }
            }
            newCode.append(line).append("\n");
            lineCounter++;
        }
        return newCode.toString();
    }
    public String disassembly(String mashineCodes){
        for(String codeline : mashineCodes.split("\\n+")){
            asmCodeBuilder.append(lineTranslator(codeline));
        }
        return insertLabels(asmCodeBuilder.toString());
    }
}
