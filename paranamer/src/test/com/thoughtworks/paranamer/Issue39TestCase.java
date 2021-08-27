package com.thoughtworks.paranamer;

import org.junit.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class Issue39TestCase {

    @Test
    public void jdk8u301_getParameterNames() throws Exception {
        Method method = LocalDate.class
                .getMethod("of", int.class, int.class, int.class);

        String[] names = JavadocParanamer.getParameterNames(method, "of", new Class[]{int.class, int.class, int.class}, "<!--   -->\n" +
                "</a>\n" +
                "<ul class=\"blockList\">\n" +
                "<li class=\"blockList\">\n" +
                "<h4>of</h4>\n" +
                "<pre>public static&nbsp;<a href=\"../../java/time/LocalDate.html\" title=\"class in java.time\">LocalDate</a>&nbsp;of(int&nbsp;year,\n" +
                "                           int&nbsp;month,\n" +
                "                           int&nbsp;dayOfMonth)</pre>\n" +
                "<div class=\"block\">Obtains an instance of <code>LocalDate</code> from a year, month and day.\n" +
                " <p>\n" +
                " This returns a <code>LocalDate</code> with the specified year, month and day-of-month.\n" +
                " The day must be valid for the year and month, otherwise an exception will be thrown.</div>\n" +
                "<dl>\n" +
                "<dt><span class=\"paramLabel\">Parameters:</span></dt>\n" +
                "<dd><code>year</code> - the year to represent, from MIN_YEAR to MAX_YEAR</dd>\n" +
                "<dd><code>month</code> - the month-of-year to represent, from 1 (January) to 12 (December)</dd>\n" +
                "<dd><code>dayOfMonth</code> - the day-of-month to represent, from 1 to 31</dd>\n" +
                "<dt><span class=\"returnLabel\">Returns:</span></dt>\n" +
                "<dd>the local date, not null</dd>\n" +
                "<dt><span class=\"throwsLabel\">Throws:</span></dt>\n" +
                "<dd><code><a href=\"../../java/time/DateTimeException.html\" title=\"class in java.time\">DateTimeException</a></code> - if the value of any field is out of range,\n" +
                "  or if the day-of-month is invalid for the month-year</dd>\n" +
                "</dl>\n" +
                "</li>\n" +
                "</ul>\n" +
                "<a name=\"ofYearDay-int-int-\">\n" +
                "<!--   -->");

        assertEquals(3, names.length);
        assertEquals("year", names[0]);
        assertEquals("month", names[1]);
        assertEquals("dayOfMonth", names[2]);
    }


}
