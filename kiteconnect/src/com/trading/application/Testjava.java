package com.trading.application;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Testjava {

    public static void main(String[] args){
        try{
            Calendar date1 = Calendar.getInstance();
            while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                date1.add(Calendar.DATE, 1);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String expiry = sdf.format(date1.getTime());

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
