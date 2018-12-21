package cn.edu.pku.sei.structureAlignment.util;

/**
 * Created by oliver on 2018/6/18.
 */
/*
* DigitsToWord is used to covert 10 to ten,  11 -> eleven
* */
public class DigitsToWord {
    private static final String[] BITS = {"one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "ten"};
    private static final String[] TEENS = {"eleven", "twelve", "thirteen",
            "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final String[] TIES = {"twenty", "thirty", "forty", "fifty",
            "sixty", "seventy", "eighty", "ninety"};

    public static void main(String[] args){
        for(int i = 0 ; i < 1000 ; i ++){
            System.out.println(toEnglish(i));
        }
    }


    public static String toEnglish(int num) {
        if(num > 999 || num < 0)
            return "";

        if(num == 0) {
            return "zero";
        }
        StringBuffer buffer = new StringBuffer();
        if(num >= 100) {
            buffer.append(pickHunder(num));
            if(num % 100 != 0) {
                buffer.append(" and ");
            }
            num -= (num / 100) * 100;
        }
        boolean largerThan20 = false;
        if(num >= 20) {
            largerThan20 = true;
            buffer.append(pickTies(num));
            num -= (num / 10) * 10;
        }
        if(!largerThan20 && num > 10) {
            buffer.append(pickTeens(num));
            num = 0;
        }
        if(num > 0) {
            String bit = pickBits(num);
            if(largerThan20) {
                buffer.append("-");
            }
            buffer.append(bit);
        }
        return buffer.toString();
    }


    private static String pickHunder(int num) {
        int hunder = num / 100;
        return BITS[hunder - 1] + " hundred";
    }

    private static String pickTies(int num) {
        int ties = num / 10;
        return TIES[ties - 2];
    }

    private static String pickTeens(int num) {
        return TEENS[num - 11];
    }

    private static String pickBits(int num) {
        return BITS[num - 1];
    }
}
