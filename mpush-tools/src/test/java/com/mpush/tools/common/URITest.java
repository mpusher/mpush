package com.mpush.tools.common;

import com.mpush.tools.common.URI;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

public class URITest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void toStringMapInput1OutputIllegalArgumentException() {
        // Arrange
        final String[] pairs = {""};
        // Act
        thrown.expect(IllegalArgumentException.class);
        URI.toStringMap(pairs);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void toStringMapInput0Output0() {
        // Arrange
        final String[] pairs = {};
        // Act
        final Map<String, String> retval = URI.toStringMap(pairs);
        // Assert result
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        Assert.assertEquals(hashMap, retval);
    }

    @Test
    public void decodeInputNullOutputNotNull() {
        // Arrange
        final String value = null;
        // Act
        final String retval = URI.decode(value);
        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void decodeInputNotNullOutputNotNull() {
        // Arrange
        final String value = "";
        // Act
        final String retval = URI.decode(value);
        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void encodeInputNullOutputNotNull() {
        // Arrange
        final String value = null;
        // Act
        final String retval = URI.encode(value);
        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void encodeInputNotNullOutputNotNull() {
        // Arrange
        final String value = "";
        // Act
        final String retval = URI.encode(value);
        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void toStringMapInput2Output1() {
        // Arrange
        final String[] pairs = {"??", null};
        // Act
        final Map<String, String> retval = URI.toStringMap(pairs);
        // Assert result
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("??", null);
        Assert.assertEquals(hashMap, retval);
    }

}
