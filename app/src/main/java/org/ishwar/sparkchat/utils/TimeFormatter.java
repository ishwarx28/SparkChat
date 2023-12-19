package org.ishwar.sparkchat.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeFormatter {

    public static String format(long time){
        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(time);
        Calendar currentTime = Calendar.getInstance();

        if(messageTime.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR)){
            if(messageTime.get(Calendar.WEEK_OF_MONTH) == currentTime.get(Calendar.WEEK_OF_MONTH)){
                if(messageTime.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH)){
                    return new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(time);
                }else if(currentTime.get(Calendar.DAY_OF_MONTH) > 1 && currentTime.get(Calendar.DAY_OF_MONTH) - 1 == messageTime.get(Calendar.DAY_OF_MONTH)){
                    return "Yesterday";
                }
                return  new SimpleDateFormat("EEE", Locale.ENGLISH).format(time);
            }
            return new SimpleDateFormat("d MMM", Locale.ENGLISH).format(time);
        }else{
            return new SimpleDateFormat("d/M/yyyy", Locale.ENGLISH).format(time);
        }
    }

    public static String formatDiff(Context context, long time){
        long difference = System.currentTimeMillis() - time;
        return "";
    }

}
