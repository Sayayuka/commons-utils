/* $Id$ */
package com.development.commons.tools.util.math;

import java.math.BigDecimal;

import com.development.commons.tools.StringUtils;

public class MathUtil {

    private MathUtil() {
    }

    /**
     * .
     */
    public static final int DEFAULT_NUMBER_OF_DECIMAL_PLACES_FOR_ROUNDING = 2;

    /**
     * Default scale for divide.
     */
    public static final int DEF_DIV_SCALE = 10;

    /**
     * <pre>
     * THIS ROUNDING METHOD (PROBLEMATIC)
     * 63.085  63.09
     * 64.085  64.08
     * 81.085  81.08
     * 82.085  82.09
     * 63.585  63.59
     * 64.585  64.58
     * 81.585  81.58
     * 82.585  82.59
     * 82.586  82.59
     * 82.584  82.58
     *
     * STANDARD ROUND_HALF_UP
     * 63.085  63.09
     * 64.085  64.09
     * 81.085  81.09
     * 82.085  82.09
     * 63.585  63.59
     * 64.585  64.59
     * 81.585  81.59
     * 82.585  82.59
     * 82.586  82.59
     * 82.584  82.58
     *
     * STANDARD ROUND_HALF_EVEN
     * 63.085  63.08
     * 64.085  64.08
     * 81.085  81.08
     * 82.085  82.08
     * 63.585  63.58
     * 64.585  64.58
     * 81.585  81.58
     * 82.585  82.58
     * 82.586  82.59
     * 82.584  82.58
     * </pre>
     *
     * @param value
     *            value
     * @param decimals
     *            decimals
     * @return double
     * @deprecated please use roundV2 instead.
     */
    @Deprecated
    public static double round(final double value, final int decimals) {

        double retval = value;
        double exp = 1;
        for (int d = 0; d < decimals; d++) {
            exp *= 10;
        }
        retval *= exp;
        if (retval > 0) {
            retval += 0.5; // turns int truncation to rounding
        } else if (retval < 0) {
            retval -= 0.5; // turns int truncation to rounding for negative numbers
        }
        final long i = (long) retval;
        retval = i;
        retval /= exp;
        return retval;

    }

    /**
     * .
     *
     * @param signedIntString
     *            signedIntString
     * @return int
     */
    // parse the specified signed integer string - remove "+" sign if applicable
    // if error occurs, log message and return 0
    public static int parseSignedIntString(final String signedIntString) {
        String validParseIntString = signedIntString.trim();
        if (StringUtils.isEmpty(validParseIntString)) {
//            log.error("Invalid number passed to com.successfactors.sfutil.tools.math.MathUtil.parseSignedIntString()");
            return 0;
        }
        if ((validParseIntString.indexOf('+') == 0)) {
            if (validParseIntString.length() < 2) {
//                log.error("Invalid number passed to com.successfactors.sfutil.tools.math.MathUtil.parseSignedIntString()");
                return 0;
            }
            validParseIntString = validParseIntString.substring(1);
        }
        try {
            return Integer.parseInt(validParseIntString);
        } catch (final NumberFormatException nfe) {
//            log.error("Problem parsing Integer from string: " + validParseIntString, nfe);
            return 0;
        }
    }

    /**
     * rounding method in HALF_UP mode (introduced for PMT-3933)
     *
     * @param value
     *            the double value you want to round
     * @param decimals
     *            the number of decimals you want to keep
     * @return the rounded value
     */
    public static double roundV2(final double value, final int decimals) {
        // to prevent loss of precision we use big decimal here instead of multiplying double values
        final BigDecimal retval = BigDecimal.valueOf(value);
        return roundV2(retval, decimals).doubleValue();
    }

    /**
     * rounding method in HALF_UP mode (introduced for PMT-3933)
     *
     * @param value
     *            the BigDecimal value you want to round
     * @param decimals
     *            the number of decimals you want to keep
     * @return the rounded value
     */
    public static BigDecimal roundV2(final BigDecimal value, final int decimals) {
        final BigDecimal retval = value.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return retval;
    }

    /**
     * rounding method in HALF_UP mode (introduced for PMT-3933)
     *
     * @param value
     *            the double value you want to round
     * @param decimals
     *            the number of decimals you want to keep
     * @param keepOldRoundingFormat
     *            if keep the old rounding format
     * @return the rounded value
     */
    public static double roundV2(final double value, final int decimals, final boolean keepOldRoundingFormat) {
        if (keepOldRoundingFormat) {
            double retval = value;
            double exp = 1;
            for (int d = 0; d < decimals; d++) {
                exp *= 10;
            }
            retval *= exp;
            if (retval > 0) {
                retval += 0.5;
            } else if (retval < 0) {
                retval -= 0.5;
            }
            final long i = (long) retval;
            retval = i;
            retval /= exp;
            return retval;
        } else {
            return roundV2(value, decimals);
        }
    }

}
