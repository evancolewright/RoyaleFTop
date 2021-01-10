package io.github.evancolewright.royaleftop.utils;

import java.text.NumberFormat;

public class MoneyUtils
{
    public static String format(double money)
    {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(money);
    }
}
