package com.mpush.tools;

import com.mpush.tools.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UtilsTest {

    @Test
    public void isLocalHostInputNotNullOutputTrue() {
        // Arrange
        final String host = "lOCALHOSt";
        // Act
        final boolean retval = Utils.isLocalHost(host);
        // Assert result
        Assert.assertEquals(true, retval);
    }

    @Test
    public void isLocalHostInputNotNullOutputTrue2() {
        // Arrange
        final String host = "0.0.0.0";
        // Act
        final boolean retval = Utils.isLocalHost(host);
        // Assert result
        Assert.assertEquals(true, retval);
    }

    @Test
    public void isLocalHostInputNotNullOutputTrue3() {
        // Arrange
        final String host = "";
        // Act
        final boolean retval = Utils.isLocalHost(host);
        // Assert result
        Assert.assertEquals(true, retval);
    }

    @Test
    public void headerToStringInput0OutputNull() {
        // Arrange
        final HashMap<String, String> headers = new HashMap<String, String>();
        // Act
        final String retval = Utils.headerToString(headers);
        // Assert result
        Assert.assertNull(retval);
    }

    @Test
    public void headerFromStringInputNullOutputNull() {
        // Arrange
        final String headersString = null;
        // Act
        final Map<String, String> retval = Utils.headerFromString(headersString);
        // Assert result
        Assert.assertNull(retval);
    }

    @Test
    public void headerFromStringInputNotNullOutput02() {
        // Arrange
        final String headersString = ":";
        // Act
        final Map<String, String> retval = Utils.headerFromString(headersString);
        // Assert result
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        Assert.assertEquals(hashMap, retval);
    }

}
