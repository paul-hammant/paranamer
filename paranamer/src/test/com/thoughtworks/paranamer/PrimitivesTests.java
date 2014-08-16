package com.thoughtworks.paranamer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PrimitivesTests {

    @Test
    public void primitivesInArraysShouldBeRetrievable() throws NoSuchMethodException {
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{String.class}), equalTo("java.lang.String"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{String.class, String.class}), equalTo("java.lang.String,java.lang.String"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{String[].class, Object[].class}), equalTo("java.lang.String[],java.lang.Object[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{String[][].class, Object[][].class}), equalTo("java.lang.String[][],java.lang.Object[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{long[].class}), equalTo("long[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{long[][].class}), equalTo("long[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{int[].class}), equalTo("int[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{int[][].class}), equalTo("int[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{boolean[].class}), equalTo("boolean[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{boolean[][].class}), equalTo("boolean[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{short[].class}), equalTo("short[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{short[][].class}), equalTo("short[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{float[].class}), equalTo("float[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{float[][].class}), equalTo("float[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{double[].class}), equalTo("double[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{double[][].class}), equalTo("double[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{byte[].class}), equalTo("byte[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{byte[][].class}), equalTo("byte[][]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{char[].class}), equalTo("char[]"));
        assertThat(DefaultParanamer.getParameterTypeNamesCSV(new Class[]{char[][].class}), equalTo("char[][]"));
    }
}
