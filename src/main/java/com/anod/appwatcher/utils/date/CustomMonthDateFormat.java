package com.anod.appwatcher.utils.date;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author algavris
 * @date 12/09/2016.
 */

class CustomMonthDateFormat extends DateFormat {

    private static final int STATE_DAY = 0;
    private static final int STATE_MONTH = 1;
    private static final int STATE_YEAR = 2;
    private final String[] mMonthNames;

    CustomMonthDateFormat(String[] monthNames)
    {
        mMonthNames = monthNames;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        StringBuffer sb = new StringBuffer();
        sb.append("d MMM. y г.");
        return sb;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        int start = pos.getIndex();
        int textLength = source.length();

        int state = STATE_DAY;
        StringBuilder sb = new StringBuilder();
        int day = 0,month = -1,year = 0;
        // "d MMM. y г."
        for(int index = start; index < textLength; index++)
        {
            char ch = source.charAt(index);
            if (state == STATE_DAY)
            {
                if (ch == ' ')
                {
                    day = toInt(sb.toString());
                    if (day == -1)
                    {
                        pos.setIndex(0);
                        pos.setErrorIndex(index);
                        break;
                    }
                    sb = new StringBuilder();
                    state = STATE_MONTH;
                    continue;
                }
            } else if (state == STATE_MONTH)
            {
                if (ch == '.') {
                    continue;
                }
                if (ch == ' ') {
                    String monthName = sb.toString();
                    month = Arrays.asList(mMonthNames).indexOf(monthName);
                    if (month == -1)
                    {
                        pos.setIndex(0);
                        pos.setErrorIndex(index);
                        break;
                    }
                    sb = new StringBuilder();
                    state = STATE_YEAR;
                    continue;
                }
            } else {// if (state == STATE_YEAR)
                if (ch == ' ') {
                    year = toInt(sb.toString());
                    if (year == -1)
                    {
                        pos.setIndex(0);
                        pos.setErrorIndex(index);
                        break;
                    }
                    break;
                }
            }
            pos.setIndex(index);
            sb.append(ch);
        }

        if (day > 0 && month >=0)
        {
            if (year == 0 && sb.length() == 4) {
                year = toInt(sb.toString());
            }
            if (year > 2000)
            {
                Calendar c = Calendar.getInstance();
                c.set(year, month, day);
                return c.getTime();
            }
        }

        pos.setIndex(0);
        return null;
    }

    private static int toInt(String text) {
        if ("".equals(text)) {
            return -1;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }
}
