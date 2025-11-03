package br.com.le.addon.qualitymanagement.utils;

public class ValidaNumero {
    public static boolean isNumeric(String strNum) {
        if (strNum == null)
            return false;
        try {
            double d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
