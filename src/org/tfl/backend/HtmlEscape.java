
package org.tfl.backend;

public class HtmlEscape
{

    private static final int BUFOFFSET = 32;

    /**
     * Method to escape UTF-8 encoded html content. ", ', &, < , > to be displayed 
     * within html elements. 
     * Method should not be used for escaping content to be used in html attributes, 
     * inside script tags etc...
     * 
     * @param str
     * @return escaped str
     */
    public static String escapeHTML(String str)
    {

        StringBuilder buf = new StringBuilder(str.length() + BUFOFFSET);

        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);

            switch (c)
            {
                case '"':
                    buf.append("&quot;");
                    break;
                case '\'':
                    buf.append("&#39;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                default:
                    buf.append(c);
                    break;

            }

        }

        return buf.toString();

    }

    
}

