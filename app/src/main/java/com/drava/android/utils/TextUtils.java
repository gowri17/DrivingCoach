package com.drava.android.utils;

import android.support.design.widget.TextInputLayout;
import android.util.Base64;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    private static final String REGX_HASHTAG = "[`@!\\&×\\÷~#\\-\\+=\\[\\]{}\\^()<>/;:,.?'|\"\\*%$\\s+\\\\"
            + "•??£¢€°™®©¶¥??????????¿¡??¤??]"; // #$%^*()+=\-\[\]\';,.\/{}|":<>?~\\\\
    public static Pattern PATTERN_HASHTAG;

    static {
        PATTERN_HASHTAG = Pattern.compile(REGX_HASHTAG);
    }


    public static final String encodeToBase64(CharSequence content) {
        if (content == null) {
            return null;
        }
        byte[] bytes = Base64.encode(content.toString().getBytes(), Base64.DEFAULT);
        return new String(bytes).trim();
    }

    public static final String encodeToBase64(byte[] data) {
        byte[] bytes = Base64.encode(data, Base64.DEFAULT);
        return new String(bytes).trim();
    }

    public static final String decodeBase64(String base64String) {
        if (base64String == null) {
            return base64String;
        }

        try {
            return new String(Base64.decode(base64String, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return base64String;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().equals("") || value.trim().equals("null");
    }

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.toString().equals("") || value.toString().equals("null");
    }

    public static boolean isEmpty(TextInputLayout inputLayout) {
        if (inputLayout != null) {
            return isEmpty(inputLayout.getEditText());
        }
        return true;
    }

    public static boolean isEmpty(EditText editText) {
        if (editText != null) {
            return isEmpty(editText.getText().toString());
        }

        return true;
    }

    public static boolean isEmpty(TextView textView) {
        if (textView != null) {
            return isEmpty(textView.getText());
        }

        return true;
    }

    public static boolean isValidEmail(CharSequence target) {
        if (isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isValidWebUrl(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.WEB_URL.matcher(target).matches();
        }
    }

    public static boolean isValidHost(CharSequence target) {
        try {
            URI uri = new URI((String) target);
            if (uri.getHost() != null) {
                return true;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return false;

       /* if (target == null) {
            return false;
        } else {
            return Patterns.DOMAIN_NAME.matcher(target).;
        }*/
    }

    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
            value += "...";
        }
        return value;
    }

    public static boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isJsonData(String content) {
        return content != null && content.startsWith("{") && content.endsWith("}");
    }

    public final static String asUpperCaseFirstChar(final String target) {

        if ((target == null) || (target.length() == 0)) {
            return target; // You could omit this check and simply live with an
            // exception if you like
        }
        return Character.toUpperCase(target.charAt(0))
                + (target.length() > 1 ? target.substring(1) : "");
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().equals("") || value.trim().equals("null");
    }

    public static boolean isNullOrEmpty(CharSequence value) {
        return value == null || value.toString().equals("");
    }

    public static String arrayToString(ArrayList<String> array, String delimiter) {
        StringBuilder builder = new StringBuilder();
        if (array.size() > 0) {
            builder.append(array.get(0));
            for (int i = 1; i < array.size(); i++) {
                builder.append(delimiter);
                builder.append(array.get(i));
            }
        }
        return builder.toString();
    }

    public static ArrayList<String> stringToArray(String string) {
        return new ArrayList<>(Arrays.asList(string.split(",")));
    }

    public static String integerArrayToString(ArrayList<Integer> array, String delimiter) {
        StringBuilder builder = new StringBuilder();
        if (array.size() > 0) {
            builder.append(array.get(0));
            for (int i = 1; i < array.size(); i++) {
                builder.append(delimiter);
                builder.append(array.get(i));
            }
        }
        return builder.toString();
    }

    public static String capitalizeFirstLetter(String original) {
        if (original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public static String getOnlyDigits(String s) {
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(s);
        String number = matcher.replaceAll("");
        return number;
    }

}
