package com.example.mymeditationapp;

public class InputValidator {
    public static boolean isPositiveInteger(String input){
        try {
            return Integer.parseInt(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDuration(String input){
        try {
            return Integer.parseInt(input) <= 60 && Integer.parseInt(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean inPlusOutLessThanOrEqualToDuration(String in, String out, String dur){
        try {
            return Integer.parseInt(in) + Integer.parseInt(out) <= (Integer.parseInt(dur)*60);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
