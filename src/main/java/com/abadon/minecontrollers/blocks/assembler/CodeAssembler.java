package com.abadon.minecontrollers.blocks.assembler;

import com.abadon.minecontrollers.utils.CommandAction;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CodeAssembler {
    protected abstract class Macros{
        public abstract Macros getNewMacro();
        protected StringBuilder value = new StringBuilder();
        public abstract boolean isMacro(String line);
        public abstract String getMacroKey(String line);
        public abstract boolean isClosed(String line);
        public abstract String removeMacroPattern(String code);
        public Preprocessor preprocessor;
        public Macros(Preprocessor preprocessor){
            this.preprocessor = preprocessor;
        }
        public boolean applyMacroValue(String line){
            if(!isClosed(line)){
                value.append(line);
                return true;
            } else return false;
        }
        public String getMacroValue(){
            return value.toString();
        }
    }
    protected class oneLineMacro extends Macros{

        public oneLineMacro(Preprocessor preprocessor) {
            super(preprocessor);
        }

        @Override
        public Macros getNewMacro() {
            return new oneLineMacro(preprocessor);
        }

        @Override
        public boolean isMacro(String line) {
            return line.matches("^\\s*%define.*");
        }
        @Override
        public String getMacroKey(String line) {
            return line.split("\\s+")[1];
        }
        @Override
        public boolean applyMacroValue(String line){
            if(line.split("\\s+").length > 2)
                this.value.append(line.replaceAll("^(\\S+\\s+){2}", ""));
            return !isClosed(line);
        }
        @Override
        public boolean isClosed(String line) {
            return true;
        }

        @Override
        public String removeMacroPattern(String code) {
            StringBuilder newCode = new StringBuilder();
            for(String line : code.split("\n")){
                if(!isMacro(line)) newCode.append(line).append("\n");
            }
            return newCode.toString();
        }
    }
    protected class MultiLineMacro extends Macros{
        private String key = "";

        public MultiLineMacro(Preprocessor preprocessor) {
            super(preprocessor);
        }

        @Override
        public boolean applyMacroValue(String line) {
            if(!(isMacro(line) && getMacroKey(line).equals(key))){
                boolean returnValue = super.applyMacroValue(line);
                value.append("\n");
                return returnValue;
            } else return false;
        }

        @Override
        public Macros getNewMacro() {
            return new MultiLineMacro(preprocessor);
        }

        @Override
        public boolean isMacro(String line) {
            return line.matches("^\\s*%macro.*") && line.split("\\s+").length == 2;
        }

        @Override
        public String getMacroKey(String line) {
            key = line.split("\\s+")[1];
            return key;
        }

        @Override
        public boolean isClosed(String line) {
            return line.matches("^\\s*%endmacro.*");
        }

        @Override
        public String removeMacroPattern(String code) {
            boolean removing = false;
            StringBuilder newCode = new StringBuilder();
            for(String line : code.split("\n")){
                if(isMacro(line)) removing = true;
                if(isClosed(line)) {
                    removing = false;
                    continue;
                }
                if(!removing) newCode.append(line).append("\n");
            }
            return newCode.toString();
        }
    }
    protected class ConditionMacros extends Macros {
        boolean canPaste = false;
        public String key = "";
        private boolean isDefCondition = false;

        public ConditionMacros(Preprocessor preprocessor) {
            super(preprocessor);
        }
        public boolean isElseStatement(String line){
            return line.matches("^\\s*%else\\s*");
        }
        @Override
        public Macros getNewMacro() {
            return new ConditionMacros(preprocessor);
        }

        @Override
        public String getMacroValue() {
            return "";
        }

        @Override
        public boolean applyMacroValue(String line) {
            key += line + "\n";
            if(isElseStatement(line)) {canPaste = !canPaste; return false;};
            if(!isMacro(line)  && canPaste){
                boolean returnValue = super.applyMacroValue(line);
                value.append("\n");
                return returnValue;
            } else return false;
        }

        @Override
        public boolean isMacro(String line) {
            return line.matches("^\\s*(%if|%ifdef)\\s+.*");
        }

        @Override
        public String getMacroKey(String line) {
            String params[] = line.split("\\s+");
            if(params[0].equals("%ifdef")){
                canPaste = preprocessor.macroTable.containsKey(params[1]);
            } else {
                canPaste = Integer.parseInt(params[1]) > 0;
            }
            return "";
        }

        @Override
        public boolean isClosed(String line) {
            return line.matches("^\\s*%endif.*");
        }

        @Override
        public String removeMacroPattern(String code) {
            return code.replaceAll(key, value.toString());
        }
    }
    protected class RetWithoutParamsMacros extends Macros{

        public RetWithoutParamsMacros(Preprocessor preprocessor) {
            super(preprocessor);
        }

        @Override
        public Macros getNewMacro() {
            return new RetWithoutParamsMacros(preprocessor);
        }

        @Override
        public boolean isMacro(String line) {
            return line.matches("^ret\\s*$");
        }

        @Override
        public String getMacroKey(String line) {
            return "";
        }

        @Override
        public boolean isClosed(String line) {
            return true;
        }

        @Override
        public String removeMacroPattern(String code) {
            return code.replaceAll("ret\\s*\n", "ret 0\n");
        }
    }
    protected class ProcedureMacro extends Macros{

        public String procedureName = "";
        public String procedurePrologue = "";
        public String procedureEpilogue = "";

        public ProcedureMacro(Preprocessor preprocessor) {
            super(preprocessor);
        }

        @Override
        public Macros getNewMacro() {
            return new ProcedureMacro(preprocessor);
        }

        @Override
        public boolean isMacro(String line) {
            return line.matches("\\S+\\s+proc");
        }

        @Override
        public String getMacroKey(String line) {
            return "";
        }

        @Override
        public boolean applyMacroValue(String line) {
            if(isMacro(line)) {
                procedurePrologue = line;
                procedureName = procedurePrologue.split("\\s+")[0];
                return true;
            } else if (super.applyMacroValue(line)) {
                value.append("\n");
                return true;
            } return false;
        }

        @Override
        public boolean isClosed(String line) {
            if(line.matches("\\S+\\s+endp")){
                procedureEpilogue = line;
                return true;
            } else return false;
        }

        @Override
        public String removeMacroPattern(String code) {
            //return code.replace(procedurePrologue,
            //        procedureName + ":\n"
            //                    + "push bp\n"
            //                    + "mov bp, sp").replace(procedureEpilogue, "mov sp, bp\npop bp");
            String lines[] = code.split("\n");
            boolean canFindNextSection = false;
            for(int i = 0; i < lines.length; i++){
                if(!canFindNextSection){
                    if(lines[i].equals(procedureEpilogue)){
                        canFindNextSection = true;
                    }
                } else{
                    if(lines[i].matches(sectionTemplate)){
                        return code.replace(procedurePrologue, "").replace(procedureEpilogue, "").replace(value, "").replace(lines[i], "\n" + procedureName + ":\n" + value + "\n" + lines[i]);
                    }
                }

            }
            return code.replace(procedurePrologue, "").replace(procedureEpilogue, "").replace(value, "") + "\n" + procedureName + ":\n" + value;
        }
    }
    protected class Preprocessor{
        protected String source;
        public LinkedList<Macros> macroses = new LinkedList();
        public HashMap<String, String> macroTable = new HashMap();
        public Preprocessor(String sourceCode){
            this.source = sourceCode;
            macroses.add(new oneLineMacro(this));
            macroses.add(new MultiLineMacro(this));
            macroses.add(new ConditionMacros(this));
            macroses.add(new RetWithoutParamsMacros(this));
            macroses.add(new ProcedureMacro(this));
        }
        private String[] getLineKeys(String line){
            return line.split("\\s+");
        }
        protected void initMacroTable(){
            String macroKey = "";
            Macros macroInstance = null;
            for(Macros macros : macroses){
                for(String line : source.split("\n")){
                    if(macroInstance == null && macros.isMacro(line)){
                        macroInstance = macros.getNewMacro();
                        macroKey = macroInstance.getMacroKey(line);
                    }
                    if(macroInstance != null){
                        macroInstance.applyMacroValue(line);
                        if (macroInstance.isClosed(line)) {
                            macroTable.put(macroKey, macroInstance.getMacroValue());
                            macroKey = "";
                            source = macroInstance.removeMacroPattern(source);
                            macroInstance = null;
                        }
                    }
                }
            }
        }
        public String getCode(){
            return source;
        }
        protected boolean applyMacroses(){
            boolean applied = false;
            for(String macroKey : macroTable.keySet()){
                if(macroKey.equals("")) continue;
                //source = source.replaceAll(macroKey, macroTable.get(macroKey));
                Pattern macrosUsages = Pattern.compile(macroKey + "(\\(\\s*(.*\\S)\\s*\\))?");
                Matcher sourceLines = macrosUsages.matcher(source);
                while (sourceLines.find()){
                    applied = true;
                    if(sourceLines.groupCount() > 1 && sourceLines.group(2) != null){
                        String macroString = macroTable.get(macroKey);
                        String macroParams[] = sourceLines.group(2).split(",\\s*");
                        int paramsCount = macroParams.length;
                        for(int i = 0; i < paramsCount; i++){
                            macroString = macroString.replaceAll("\\$" + i, macroParams[i]);
                        }
                        macroString = macroString.replaceAll("\\$#", String.valueOf(paramsCount));
                        macroString = macroString.replaceAll("\\$\\*", sourceLines.group(2).replaceAll(",\\s*", " "));
                        source = source.replace(sourceLines.group(0), macroString);
                    } else{
                        source = source.replace(macroKey, macroTable.get(macroKey).replace("$#", "0").replace("$*", ""));
                    }
                }
            }
            return applied;
        }
        public void start(){
            source = source.replaceAll(";.*?\n", "").replaceAll("\\\\.*\n", "");
            for (int i = 0; i < 128; i++){
                initMacroTable();
                if(!applyMacroses()) break;
            }
        }
    }
    protected abstract class SpecialCommand{
        protected int opcode = 0;
        public boolean getCommandOpcode(int opcode){
            return this.opcode == opcode;
        }
        public abstract String analyze(String opcode, String firstArg, String secondArg);
        public abstract SpecialCommand getDefaultInstance();
    }
    protected class LeaCommand extends SpecialCommand{
        public LeaCommand(){
            this.opcode = CommandAction.LEA.ordinal();
        }
        private int size = 0;
        private boolean sumIndex = false;
        private boolean sumDisp = false;
        private int base = 0;
        private int disp = 0;
        private int index = 0;
        private void tokenize(String addressEval, boolean baseFound){
            int reservedIndexTokens = -1000;
            final int indexTokenSequenseSize = 2;
            int baseTokenIndex = -1;
            //String tokens[] = addressEval.split("\\s+");
            ArrayList<String> tokens = new ArrayList<>();
            String buffer = "";
            for(char c : addressEval.toCharArray()){
                if(c == '+' || c == '*' || c == '-'){
                    tokens.add(buffer);
                    buffer = "";
                    tokens.add(String.valueOf(c));
                } else if(c == ' ') continue;
                else{
                    buffer += c;
                }
            }
            tokens.add(buffer);
            //
            for(int i = 0; i < tokens.size(); i++){
                if(tokens.get(i).equals("*") && tokens.size() > i + 1 && i > 0){
                    if(tokens.get(i-1).matches("0x[a-fA-F0-9]+") || tokens.get(i-1).matches("[0-9]+")  || tokens.get(i-1).matches("[01]+b")){
                        ArgumentParser sizeParser = new ArgumentParser(tokens.get(i-1));
                        sizeParser.analyze();
                        size = sizeParser.getParsedNumber();
                        ArgumentParser indexRegisterParser = new ArgumentParser(tokens.get(i+1));
                        indexRegisterParser.analyze();
                        index = indexRegisterParser.getParsedNumber();
                        if(i + 1 == baseTokenIndex){
                            baseFound = false;
                        }
                    } else{
                        ArgumentParser sizeParser = new ArgumentParser(tokens.get(i+1));
                        sizeParser.analyze();
                        size = sizeParser.getParsedNumber();
                        ArgumentParser indexRegisterParser = new ArgumentParser(tokens.get(i-1));
                        indexRegisterParser.analyze();
                        index = indexRegisterParser.getParsedNumber() + 1;
                        if(i - 1 == baseTokenIndex){
                            baseFound = false;
                        }
                    }
                    if(i > 1){
                        if(tokens.get(i-2).equals("+")){
                            sumIndex = true;
                        } else if (tokens.get(i-2).equals("-")) {
                            sumIndex = false;
                        } else return;
                    }else{
                        sumIndex = true;
                    }
                    reservedIndexTokens = i - 1;
                }
                if((tokens.get(i).matches("0x[a-fA-F0-9]+") || tokens.get(i).matches("[0-9]+")  || tokens.get(i).matches("[01]+b"))
                        && (i < reservedIndexTokens || i > reservedIndexTokens+indexTokenSequenseSize)){
                    ArgumentParser ap = new ArgumentParser(tokens.get(i));
                    ap.analyze();
                    disp = ap.getParsedNumber();
                    if(i > 0){
                        if(tokens.get(i-1).equals("+")){
                            sumDisp = true;
                        } else if (tokens.get(i-1).equals("-")) {
                            sumDisp = false;
                        } else return;
                    } else {
                        sumDisp = true;
                    }
                }
                if(tokens.get(i).matches("[a-zA-Z]+") && (i < reservedIndexTokens || i > reservedIndexTokens + indexTokenSequenseSize)){
                    if(!baseFound){
                        ArgumentParser ap = new ArgumentParser(tokens.get(i));
                        ap.analyze();
                        base = ap.getParsedNumber() + 1;
                        baseTokenIndex = i;
                        baseFound = true;
                    }
                    else{
                        ArgumentParser ap = new ArgumentParser(tokens.get(i));
                        ap.analyze();
                        index = ap.getParsedNumber() + 1;
                        reservedIndexTokens = i - 1;
                        if(i > 0){
                            if(tokens.get(i-1).equals("+")){
                                sumIndex = true;
                            } else if (tokens.get(i-1).equals("-")) {
                                sumIndex = false;
                            } else return;
                        }else{
                            sumIndex = true;
                        }
                    }
                }
            }
        }
        private void loadAddress(String effectiveAddress){
            if(!effectiveAddress.matches("(\\w*)\\[(.*?)\\]")) return;
            Pattern effectiveAddressPattern = Pattern.compile("(\\w*)\\s*\\[(.*?)\\]");
            Matcher matcher = effectiveAddressPattern.matcher(effectiveAddress);
            matcher.find();
            if(matcher.groupCount() < 2 && matcher.group(2) == "") return;
            if(matcher.group(1) != ""){
                ArgumentParser ap = new ArgumentParser(matcher.group(1));
                ap.analyze();
                base = ap.getParsedNumber() + 1;
            }
            tokenize(matcher.group(2), base != 0);
        }
        @Override
        public String analyze(String opcode, String firstArg, String secondArg) {
            ArgumentParser sourceRegisterParser = new ArgumentParser(firstArg);
            sourceRegisterParser.analyze();
            int sourceRegister = sourceRegisterParser.getParsedNumber();
            loadAddress(secondArg);
            StringBuilder commandArgsBuilder = new StringBuilder();
            int infoData = (sourceRegister << 4) + ((sumIndex ? 1 : 0) << 3) + ((sumDisp ? 1 : 0) << 2) + (int)Math.round(Math.log(size) / Math.log(2)) % 4;
            int baseIndexData = (base << 4) +  index;
            String stringInfoData = Integer.toString(infoData, 16);
            String stringBaseIndexData = Integer.toString(baseIndexData, 16);
            String stringDispData = Integer.toString(disp, 16);
            stringInfoData = "0".repeat(2 - stringInfoData.length()) + stringInfoData;
            stringBaseIndexData = "0".repeat(2 - stringBaseIndexData.length()) + stringBaseIndexData;
            stringDispData = "0".repeat(4 - stringDispData.length()) + stringDispData;
            //commandArgsBuilder.append(sourceRegister).append(sumIndex ? "1" : "0").append(sumDisp ? "1" : "0").append(Math.round(Math.log(size)) % 4).append(base).append(index).append(disp);
            commandArgsBuilder.append(stringInfoData).append(stringBaseIndexData).append(stringDispData);
            return opcode + "03" + commandArgsBuilder.toString();
        }

        @Override
        public SpecialCommand getDefaultInstance() {
            return new LeaCommand();
        }
    }
    protected class ArgumentParser{
        protected boolean checkKeyForNumber(String key){
            return key.matches("^-?\\d+$") ||key.matches("^-?0[xX][0-9a-fA-F]+$") || key.matches("^-?\\d+b$");
        }
        protected boolean checkKeyForRegister(String key){
            return registersTable.containsKey(key);
        }
        protected boolean hasOffset(String key){
            return key.matches("\\w{2}:.*");
        }
        protected boolean isKeyAddress(String key){
            return key.matches("^\\[.*\\]$");
        }
        protected String getAddress(String key) throws StringIndexOutOfBoundsException{
            return key.substring(1, key.length()-1);
        }
        protected int getNumberFromKey(String key){
            if(key.matches("^-?\\d+$")){
                return Integer.valueOf(key);
            }else if(key.matches("^-?0[xX][0-9a-fA-F]+$")){
                return Integer.valueOf(key.substring(2), 16);
            }else if(key.matches("^-?\\d+b$")){
                return Integer.valueOf(key.substring(0, key.length() - 1), 2);
            } else return 0;
        }
        protected boolean valid = false;
        protected boolean isValid(){
            return valid;
        }
        protected int metaInfo = 0;
        protected HashMap<String, Integer> registersTable = new HashMap();
        protected void initRegisters(){
            registersTable.put("ax", 0);
            registersTable.put("ah", 1);
            registersTable.put("al", 2);
            registersTable.put("bx", 3);
            registersTable.put("bh", 4);
            registersTable.put("bl", 5);
            registersTable.put("cx", 6);
            registersTable.put("ch", 7);
            registersTable.put("cl", 8);
            registersTable.put("dx", 9);
            registersTable.put("dh", 10);
            registersTable.put("dl", 11);
            registersTable.put("di", 12);
            registersTable.put("si", 13);
            registersTable.put("bp", 14);
            registersTable.put("sp", 15);
            registersTable.put("ip", 16);
            registersTable.put("flags", 17);
            registersTable.put("ds", 18);
            registersTable.put("ss", 19);
            registersTable.put("cs", 20);
            registersTable.put("es", 21);
            for(int i = 0; i < 64; i++) registersTable.put("r" + i, 22 + i);
            registersTable.put("it", 86);
        }
        protected int getMetaInfo(){
            return metaInfo;
        }
        String argument;
        int argumentNum = 0;
        ArgumentParser(String argument){
            this.argument = argument;
            initRegisters();
        }
        public void analyze(){
            String argument = this.argument;
            boolean hasOffsetFlag = false;
            if(hasOffset(argument)){
                hasOffsetFlag = true;
                String offset = argument.substring(0, 2);
                argument = argument.substring(3);
                switch (offset.toUpperCase()){
                    case "DS":{
                        metaInfo = 10;
                        break;
                    }
                    case "SS":{
                        metaInfo = 1;
                        break;
                    }
                    case "ES":{
                        metaInfo = 11;
                        break;
                    }
                }
            }
            if(isKeyAddress(argument)){
                metaInfo += 100;
                argument = getAddress(argument);
            }
            if(checkKeyForNumber(argument)){
                valid = true;
                metaInfo += 1000;
                argumentNum = getNumberFromKey(argument);

            } else if(checkKeyForRegister(argument)) {
                valid = true;
                argumentNum = registersTable.get(argument);
            } else if(labels.containsKey(argument)){
                valid = true;
                argumentNum = labels.get(argument);
                if(!hasOffsetFlag)
                    metaInfo += 1010;
                else metaInfo += 1000;
            } else if (sectionLabels.containsKey(argument)) {
                valid = true;
                argumentNum = sectionLabels.get(argument);
                metaInfo += 1000;
            }
        }
        public int getParsedNumber(){
            return argumentNum;
        }
    }
    protected HashMap<String, String> hashTable;
    protected HashMap<String, String> opcodesTable;
    protected int getCombinedMetaInfo(int firstMeta, int secondMeta){
        String rawFirstStringMeta = String.valueOf(firstMeta);
        String rawSecondStringMeta = String.valueOf(secondMeta);
        String firstStringMeta = new StringBuilder().append(rawFirstStringMeta).reverse().append(new String("0").repeat(4 - rawFirstStringMeta.length())).toString();
        String secondStringMeta = new StringBuilder().append(rawSecondStringMeta).reverse().append(new String("0").repeat(4 - rawSecondStringMeta.length())).toString();
        StringBuilder resultMetaBuilder = new StringBuilder();
        //bit resolution
        //resultMetaBuilder.append(firstStringMeta.substring(0,1) + secondStringMeta.substring(0,1));
        //address meta
        //resultMetaBuilder.append(firstStringMeta.substring(1,2) + secondStringMeta.substring(1,2));
        //operand types
        //resultMetaBuilder.append(firstStringMeta.substring(2,4) + secondStringMeta.substring(2,4));

        //offset
        resultMetaBuilder.append(firstStringMeta.substring(0,2) + secondStringMeta.substring(0,2));
        //address meta
        resultMetaBuilder.append(firstStringMeta.substring(2,3) + secondStringMeta.substring(2,3));
        //operand types
        resultMetaBuilder.append(firstStringMeta.substring(3,4) + secondStringMeta.substring(3,4));
        //compiling meta
        return Integer.valueOf(resultMetaBuilder.toString(), 2);
    }
    protected void initComandlets(){
        for(CommandAction commandlet : CommandAction.values()){
            String code = Integer.toString(commandlet.ordinal(), 16);
            if(code.length() == 1){
                code = "0" + code;
            }
            opcodesTable.put(commandlet.name(), code);
        }
    }
    protected HashMap<String, Integer> labels = new HashMap<>();
    protected HashMap<String, Integer> sectionLabels = new HashMap<>();
    public CodeAssembler(){
        hashTable = new HashMap();
        opcodesTable = new HashMap();
        specialCommands.put(CommandAction.LEA.name(), new LeaCommand());
        initComandlets();
    }
    private String compileRawData(String dataType, String data){
        String result = null;
        ArgumentParser argumentParser = new ArgumentParser(data);
        switch (dataType){
            case "db":{
                argumentParser.analyze();
                result = Integer.toUnsignedString((char)argumentParser.getParsedNumber() & 0xFFFF, 16);
                result = "0".repeat( 2 - result.length()) + result  + "\n";
                break;
            }
            case "dw":{
                argumentParser.analyze();
                result = Integer.toUnsignedString((short)argumentParser.getParsedNumber() & 0xFFFF, 16);
                result = "0".repeat( 2 * 2 - result.length()) + result;
                result = result.substring(0, result.length() / 2) + "\n"
                        + result.substring(result.length() / 2, result.length()) + "\n";
                break;
            }
        }
        return result;
    }
    private String sectionTemplate = "section\\s+(?<section>\\S+)";
    protected String initLabels(String code){
        Pattern sectionPattern = Pattern.compile(sectionTemplate);
        Pattern labelPattern = Pattern.compile("((?<label>\\S+)\\s*:)?(\\s*(?<type>(db|dw))\\s+(?<data>\\S+))?(\\s+dup\\s*\\(\\s*(?<dup>\\d+)\\s*\\))?\\s*");
        int index = 0;
        final int commandSize = 6;
        int prevSection = 0;
        for(String line : code.split("\n+")){
            Matcher labelSearcher = sectionPattern.matcher(line);
            if(labelSearcher.matches()){
                sectionLabels.put(labelSearcher.group("section"), index);
                prevSection = index;
                code = code.replace(labelSearcher.group(0), "\n");
            } else if ((labelSearcher = labelPattern.matcher(line)).matches()) {
                if(labelSearcher.group("label") != null){
                    String label = labelSearcher.group("label");
                    labels.put(label, index - prevSection);
                }
                if(labelSearcher.group("data") != null){
                    String type = labelSearcher.group("type");
                    String data = labelSearcher.group("data");
                    String compiledData = compileRawData(type, data);
                    if(labelSearcher.group("dup") != null){
                        compiledData = compiledData.repeat(Integer.parseInt(labelSearcher.group("dup")));
                    }
                    index += compiledData.split("\n").length;
                    code = code.replace(labelSearcher.group(0), compiledData);
                } else code = code.replace(labelSearcher.group(0), "");
            } else{
                index += commandSize;
            }
        }
        return code;
    }
    public String defaultCommand(String command){
        command = command.replace(',', ' ');
        String keys[] = command.split("\\s+", 3);
        if(keys.length == 1){
            try{
                Integer.parseInt(keys[0], 16);
                return keys[0];
            } catch (NumberFormatException ex){}
        }
        if(keys[0].toUpperCase().equals("ORG")){
            if(keys.length > 1) {
                return "OFFSET: " + keys[1];
            }
            else return "\n";
        }
        if(!opcodesTable.containsKey(keys[0].toUpperCase())) return command + " <- unknown command";
        LogUtils.getLogger().info("commandlet - " + keys[0] + "\tcomandlet code - " + opcodesTable.get(keys[0].toUpperCase()));
        String  firstValue = "0000";
        String  secondValue = "0000";
        String opcodeName = keys[0].toUpperCase();
        String opcode = opcodesTable.get(opcodeName);
        try{
            if(specialCommands.containsKey(opcodeName)){
                SpecialCommand specialCommand = specialCommands.get(opcodeName).getDefaultInstance();
                String compilationResult = "";
                if(keys.length > 2)
                    compilationResult = specialCommand.analyze(opcode, keys[1], keys[2]);
                else if (keys.length > 1)
                    compilationResult = specialCommand.analyze(opcode, keys[1], "");
                return compilationResult;
            }
            int firstMeta = 0;
            int secondMeta = 0;
            if(keys.length > 1){
                ArgumentParser argumentParser = new ArgumentParser(keys[1]);
                argumentParser.analyze();
                if(argumentParser.isValid()){
                    firstValue = Integer.toUnsignedString(argumentParser.getParsedNumber() & 0xFFFF, 16);
                    if(firstValue.length() < 4)
                        firstValue = new String("0").repeat(4 - firstValue.length()) + firstValue;
                    firstMeta = argumentParser.getMetaInfo();
                } else return command + " <- incorrect first argument";
            }
            if(keys.length > 2) {
                ArgumentParser argumentParser = new ArgumentParser(keys[2]);
                argumentParser.analyze();
                if(argumentParser.isValid()){
                    secondValue = Integer.toUnsignedString(argumentParser.getParsedNumber() & 0xFFFF, 16);
                    if(secondValue.length() < 4)
                        secondValue = new String("0").repeat(4 - secondValue.length()) + secondValue;
                    secondMeta = argumentParser.getMetaInfo();
                } else return command + " <- incorrect second argument";
            }
            String meta = Integer.toString(getCombinedMetaInfo(firstMeta, secondMeta), 16);
            meta = "0".repeat(2 - meta.length()) + meta;
            String compiledCommand = opcode + meta + firstValue + secondValue;
            LogUtils.getLogger().info(compiledCommand);
            return compiledCommand;
        } catch (Exception exception){
            return command + " <- fatal error";
        }

    }
    protected HashMap<String, SpecialCommand> specialCommands = new HashMap();
    public String assembly(String assemblerCode){
        assemblerCode += "\n";
        Preprocessor preprocessor = new Preprocessor(assemblerCode);
        preprocessor.start();
        assemblerCode = assemblerCode.replaceAll("\n+", "\n").replaceAll("^\n", "");
        assemblerCode = preprocessor.getCode();
        assemblerCode = initLabels(assemblerCode);
        assemblerCode = assemblerCode.replaceAll("\n+", "\n").replaceAll("^\n", "");
        StringBuilder mashineCodesBuilder = new StringBuilder();
        for(String codeline : assemblerCode.split("\\n+")){
            mashineCodesBuilder.append(defaultCommand(codeline)).append("\n");
        }
        return mashineCodesBuilder.toString().replaceAll("\n+", "\n").replaceAll("^\n", "").toUpperCase();
    }
}
