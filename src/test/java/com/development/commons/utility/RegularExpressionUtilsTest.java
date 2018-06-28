package com.development.commons.utility;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RegularExpressionUtilsTest {

    @Test
    public void testWildcardToRegexp() {
        Assert.assertFalse(matches(null, "*job*"));
        Assert.assertTrue(matches("Job Information".toLowerCase(), "*job*"));
    }

    private static boolean matches(String input, String pattern) {
        if (input == null) {
            return false;
        }
        String regex = RegularExpressionUtils.wildcardToRegexp(pattern);
        return input.matches(regex);
    }

}
