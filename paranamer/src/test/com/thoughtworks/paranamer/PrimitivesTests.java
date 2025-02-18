package com.thoughtworks.paranamer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PrimitivesTests {

    @Test
    public void primitivesInArraysShouldBeRetrievable() throws NoSuchMethodException {
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{String.class}), equalTo("java.lang.String"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{String.class, String.class}), equalTo("java.lang.String,java.lang.String"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{String[].class, Object[].class}), equalTo("java.lang.String[],java.lang.Object[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{String[][].class, Object[][].class}), equalTo("java.lang.String[][],java.lang.Object[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{long[].class}), equalTo("long[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{long[][].class}), equalTo("long[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{int[].class}), equalTo("int[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{int[][].class}), equalTo("int[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{boolean[].class}), equalTo("boolean[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{boolean[][].class}), equalTo("boolean[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{short[].class}), equalTo("short[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{short[][].class}), equalTo("short[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{float[].class}), equalTo("float[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{float[][].class}), equalTo("float[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{double[].class}), equalTo("double[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{double[][].class}), equalTo("double[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{byte[].class}), equalTo("byte[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{byte[][].class}), equalTo("byte[][]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{char[].class}), equalTo("char[]"));
        assertThat(LegacyParanamer.getParameterTypeNamesCSV(new Class[]{char[][].class}), equalTo("char[][]"));
    }
}
