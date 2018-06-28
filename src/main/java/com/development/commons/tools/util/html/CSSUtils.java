/*
 * $Id$
 *
 * Copyright (C) 2005 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools.util.html;

import java.awt.Color;
import java.util.HashMap;
import java.text.ParseException;

/**
 * CSSUtils
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class CSSUtils
{
  private static HashMap _cssColorNameMap = new HashMap();
  private static void defineColor(String name, int rgb) {
    _cssColorNameMap.put(name, new Color(rgb));
  }
  static {
    // from http://msdn.microsoft.com/library/default.asp?url=/workshop/author/dhtml/reference/colors/colors.asp
    // (seems to only be viewable in IE for some reason)

    defineColor("aliceblue", 0xF0F8FF);
    defineColor("antiquewhite", 0xFAEBD7);
    defineColor("aqua", 0x00FFFF);
    defineColor("aquamarine", 0x7FFFD4);
    defineColor("azure", 0xF0FFFF);
    defineColor("beige", 0xF5F5DC);
    defineColor("bisque", 0xFFE4C4);
    defineColor("black", 0x000000);
    defineColor("blanchedalmond", 0xFFEBCD);
    defineColor("blue", 0x0000FF);
    defineColor("blueviolet", 0x8A2BE2);
    defineColor("brown", 0xA52A2A);
    defineColor("burlywood", 0xDEB887);
    defineColor("cadetblue", 0x5F9EA0);
    defineColor("chartreuse", 0x7FFF00);
    defineColor("chocolate", 0xD2691E);
    defineColor("coral", 0xFF7F50);
    defineColor("cornflowerblue", 0x6495ED);
    defineColor("cornsilk", 0xFFF8DC);
    defineColor("crimson", 0xDC143C);
    defineColor("cyan", 0x00FFFF);
    defineColor("darkblue", 0x00008B);
    defineColor("darkcyan", 0x008B8B);
    defineColor("darkgoldenrod", 0xB8860B);
    defineColor("darkgray", 0xA9A9A9);
    defineColor("darkgreen", 0x006400);
    defineColor("darkkhaki", 0xBDB76B);
    defineColor("darkmagenta", 0x8B008B);
    defineColor("darkolivegreen", 0x556B2F);
    defineColor("darkorange", 0xFF8C00);
    defineColor("darkorchid", 0x9932CC);
    defineColor("darkred", 0x8B0000);
    defineColor("darksalmon", 0xE9967A);
    defineColor("darkseagreen", 0x8FBC8B);
    defineColor("darkslateblue", 0x483D8B);
    defineColor("darkslategray", 0x2F4F4F);
    defineColor("darkturquoise", 0x00CED1);
    defineColor("darkviolet", 0x9400D3);
    defineColor("deeppink", 0xFF1493);
    defineColor("deepskyblue", 0x00BFFF);
    defineColor("dimgray", 0x696969);
    defineColor("dodgerblue", 0x1E90FF);
    defineColor("firebrick", 0xB22222);
    defineColor("floralwhite", 0xFFFAF0);
    defineColor("forestgreen", 0x228B22);
    defineColor("fuchsia", 0xFF00FF);
    defineColor("gainsboro", 0xDCDCDC);
    defineColor("ghostwhite", 0xF8F8FF);
    defineColor("gold", 0xFFD700);
    defineColor("goldenrod", 0xDAA520);
    defineColor("gray", 0x808080);
    defineColor("green", 0x008000);
    defineColor("greenyellow", 0xADFF2F);
    defineColor("honeydew", 0xF0FFF0);
    defineColor("hotpink", 0xFF69B4);
    defineColor("indianred", 0xCD5C5C);
    defineColor("indigo", 0x4B0082);
    defineColor("ivory", 0xFFFFF0);
    defineColor("khaki", 0xF0E68C);
    defineColor("lavender", 0xE6E6FA);
    defineColor("lavenderblush", 0xFFF0F5);
    defineColor("lawngreen", 0x7CFC00);
    defineColor("lemonchiffon", 0xFFFACD);
    defineColor("lightblue", 0xADD8E6);
    defineColor("lightcoral", 0xF08080);
    defineColor("lightcyan", 0xE0FFFF);
    defineColor("lightgoldenrodyellow", 0xFAFAD2);
    defineColor("lightgreen", 0x90EE90);
    defineColor("lightgrey", 0xD3D3D3);
    defineColor("lightpink", 0xFFB6C1);
    defineColor("lightsalmon", 0xFFA07A);
    defineColor("lightseagreen", 0x20B2AA);
    defineColor("lightskyblue", 0x87CEFA);
    defineColor("lightslategray", 0x778899);
    defineColor("lightsteelblue", 0xB0C4DE);
    defineColor("lightyellow", 0xFFFFE0);
    defineColor("lime", 0x00FF00);
    defineColor("limegreen", 0x32CD32);
    defineColor("linen", 0xFAF0E6);
    defineColor("magenta", 0xFF00FF);
    defineColor("maroon", 0x800000);
    defineColor("mediumaquamarine", 0x66CDAA);
    defineColor("mediumblue", 0x0000CD);
    defineColor("mediumorchid", 0xBA55D3);
    defineColor("mediumpurple", 0x9370DB);
    defineColor("mediumseagreen", 0x3CB371);
    defineColor("mediumslateblue", 0x7B68EE);
    defineColor("mediumspringgreen", 0x00FA9A);
    defineColor("mediumturquoise", 0x48D1CC);
    defineColor("mediumvioletred", 0xC71585);
    defineColor("midnightblue", 0x191970);
    defineColor("mintcream", 0xF5FFFA);
    defineColor("mistyrose", 0xFFE4E1);
    defineColor("moccasin", 0xFFE4B5);
    defineColor("navajowhite", 0xFFDEAD);
    defineColor("navy", 0x000080);
    defineColor("oldlace", 0xFDF5E6);
    defineColor("olive", 0x808000);
    defineColor("olivedrab", 0x6B8E23);
    defineColor("orange", 0xFFA500);
    defineColor("orangered", 0xFF4500);
    defineColor("orchid", 0xDA70D6);
    defineColor("palegoldenrod", 0xEEE8AA);
    defineColor("palegreen", 0x98FB98);
    defineColor("paleturquoise", 0xAFEEEE);
    defineColor("palevioletred", 0xDB7093);
    defineColor("papayawhip", 0xFFEFD5);
    defineColor("peachpuff", 0xFFDAB9);
    defineColor("peru", 0xCD853F);
    defineColor("pink", 0xFFC0CB);
    defineColor("plum", 0xDDA0DD);
    defineColor("powderblue", 0xB0E0E6);
    defineColor("purple", 0x800080);
    defineColor("red", 0xFF0000);
    defineColor("rosybrown", 0xBC8F8F);
    defineColor("royalblue", 0x4169E1);
    defineColor("saddlebrown", 0x8B4513);
    defineColor("salmon", 0xFA8072);
    defineColor("sandybrown", 0xF4A460);
    defineColor("seagreen", 0x2E8B57);
    defineColor("seashell", 0xFFF5EE);
    defineColor("sienna", 0xA0522D);
    defineColor("silver", 0xC0C0C0);
    defineColor("skyblue", 0x87CEEB);
    defineColor("slateblue", 0x6A5ACD);
    defineColor("slategray", 0x708090);
    defineColor("snow", 0xFFFAFA);
    defineColor("springgreen", 0x00FF7F);
    defineColor("steelblue", 0x4682B4);
    defineColor("tan", 0xD2B48C);
    defineColor("teal", 0x008080);
    defineColor("thistle", 0xD8BFD8);
    defineColor("tomato", 0xFF6347);
    defineColor("turquoise", 0x40E0D0);
    defineColor("violet", 0xEE82EE);
    defineColor("wheat", 0xF5DEB3);
    defineColor("white", 0xFFFFFF);
    defineColor("whitesmoke", 0xF5F5F5);
    defineColor("yellow", 0xFFFF00);
    defineColor("yellowgreen", 0x9ACD32);
  }

  public static Color cssNamedColor(String name) {
    return (Color)_cssColorNameMap.get(name);
  }

  /**
   * Extracts and returns the background color from a CSS style
   * string.
   */
  public static Color extractBackgroundColorFromStyle(String style) {
    try {
      return (new CSSStyleParser(style) {
        boolean background;
        Color color;

        protected void declaration(String name) {
          background = ("background".equalsIgnoreCase(name) ||
                        "background-color".equalsIgnoreCase(name));
        }

        protected void color(int rgb) {
          if (background) {
            color = new Color(rgb);
          }
        }

        protected void ident(String name) {
          if (background) {
            Color c = cssNamedColor(name);
            if (c != null) {
              color = c;
            }
          }
        }

        Color color() throws ParseException {
          parse();
          return color;
        }
      }).color();
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static StringBuffer escapeString(StringBuffer buf, String str) {
    if (str != null) {
      for (int i=0, n=str.length() ; i<n ; ++i) {
        char ch = str.charAt(i);

        if (ch == '\"' || ch == '\'' || ch == '\\') {
          buf.append('\\').append(ch);
        } else if ((' ' <= ch && ch <= '~') || ('\200' <= ch && ch <= '\377') || ch == '\t') {
          buf.append(ch);
        } else {
          buf.append('\\').append(Integer.toHexString(ch)).append(' ');
        }
      }
    }
    return buf;
  }

  public static String escapeString(String str) {
    if (str == null) {
      return "";
    } else {
      return escapeString(new StringBuffer(str.length()*2), str).toString();
    }
  }

} // CSSUtils
