package com.estudos.whatsapp.utils;


import android.util.Base64;

public class Base64Utils {

    public static  String encode(String value){
        return Base64.encodeToString(value.getBytes(),Base64.DEFAULT).replaceAll("(\\n|\\r)","");
    }

    public static String decode(String value){
        return new String(Base64.decode(value,Base64.DEFAULT));
    }
}
