package com.hawk.funday.support.utils;

import java.text.DecimalFormat;

public class UnitUtils {


    public static String byteUnit2Str(long bytesLen,boolean isHasUnit,boolean isUpKbyte){
        float kbUnit=bytesLen/1024f;
        String result="";
        if (kbUnit>1024f||isUpKbyte )
        {
            kbUnit=kbUnit/1024f;
            result=new DecimalFormat("#.##").format(kbUnit);
            if (isHasUnit)
                result+="M";

        }else {
            result=new DecimalFormat("#.##").format(kbUnit);
            if (isHasUnit)
                result+="K";
        }
        if (result.length()>0&&result.startsWith("."))
            result=result.substring(1,result.length()-1);
        return result;
    }
}
