package com.development.commons.utility;

public class RegularExpressionUtils {

    /**
     * Convert wildcard character to regular expression.
     * For example: "*Cost*" -> "^.*Cost.*$"
     *
     * @param  pattern String which contains wildcard character
     * @return Regular expression string
     */
    public static String wildcardToRegexp(String pattern) {
        final char ESCAPES[] = {'$', '^', '[', ']', '(', ')', '{', '|', '+', '\\', '.', '<', '>'};
        String result = "^";
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            boolean isEscaped = false;
            for (int j = 0; j < ESCAPES.length; j++) {
                if (ch == ESCAPES[j]) {
                    result += "\\" + ch;
                    isEscaped = true;
                    break;
                }
            }

            if (!isEscaped) {
                if (ch == '*') {
                    result += ".*";
                } else if (ch == '?') {
                    result += ".?";
                } else {
                    result += ch;
                }
            }
        }
        result += "$";
        return result;
    }

}
