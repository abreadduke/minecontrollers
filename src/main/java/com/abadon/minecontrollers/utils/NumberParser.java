package com.abadon.minecontrollers.utils;

public class NumberParser {
    public static boolean checkForSignedNumber(String number){
        return number.matches("^-?\\d+$") || number.matches("^-?0[xX][0-9a-fA-F]+$") || number.matches("^-?\\d+b$");
    }
    public static boolean checkForUnignedNumber(String number){
        return number.matches("^\\d+$") || number.matches("^0[xX][0-9a-fA-F]+$") || number.matches("^\\d+b$");
    }
    public static boolean checkForHexPrefix(String number){
        return number.matches("^0[xX]");
    }
    public static int getSignedNumber(String number){
        if(number.matches("^-?\\d+$")){
            return Integer.valueOf(number);
        }else if(number.matches("^-?0[xX][0-9a-fA-F]+$")){
            return Integer.valueOf(number.substring(2), 16);
        }else if(number.matches("^-?\\d+b$")){
            return Integer.valueOf(number.substring(0, number.length() - 1), 2);
        } else return 0;
    }
    public static int getUnsignedNumber(String number){
        if(number.matches("^\\d+$")){
            return Integer.valueOf(number);
        }else if(number.matches("^0[xX][0-9a-fA-F]+$")){
            return Integer.valueOf(number.substring(2), 16);
        }else if(number.matches("^\\d+b$")){
            return Integer.valueOf(number.substring(0, number.length() - 1), 2);
        } else return 0;
    }
}
