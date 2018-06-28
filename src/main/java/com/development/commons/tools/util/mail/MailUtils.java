// ###Modified by SCAConverter on 2014-06-10 PST###
/* $Id$ */
package com.development.commons.tools.util.mail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.development.commons.tools.StringUtils;

/**
 * todo make this a singleton ? -pk
 */
public class MailUtils {

    private MailUtils() {
    }

    public static boolean isValidEmailAddress(final String emailAddr) {
        // Added check empty to prevent NPE from InternetAddress.parse.
        if (StringUtils.isBlank(emailAddr)) {
            return false;
        }
        // User InternetAddress.parse to validate e-mail address. This will be
        // compliant with the RFC
        try {
            InternetAddress.parse(emailAddr, true /* strict */);
        } catch (final AddressException e) {
//            logger.error("Error when parsing e-mail address for validation "
//                    + emailAddr);
            return false;
        }

        // even strict is turned on, it allows e-mail address without domain name.
        // in our application we enforce the format for xxx@domain.xxx
        // Set the email pattern string
        final Pattern p = Pattern.compile(".+@[a-zA-Z.\\-_0-9]+[.][a-zA-Z]+");

        // Match the given string with the pattern
        final Matcher m = p.matcher(emailAddr);

        // Check whether match is found
        return m.matches();
    }

    /**
     * EE-10850 For external participants. Check if the userId is an email addredd
     *
     * @param userId
     * @return boolean true or false
     */
    public static boolean isUserIdAnEmail(final String userId) {
        // check if userId is an email address for external users
        // Set the email pattern string
        final Pattern p = Pattern.compile(".+@.+\\.[a-zA-Z]+");
        // Match the given string with the pattern
        final Matcher m = p.matcher(userId);

        // check whether match is found
        return m.matches();
    }

}
