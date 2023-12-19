package org.ishwar.sparkchat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompletionTokenizer
{
    private static final String DEFAULT_WORD_PATTERN = "[\\p{L}\\p{N}\\p{M}]+";
	private static final String DEFAULT_SYMBOL_PATTERN = "[^\\p{L}\\p{N}\\p{M}\\s]+";
    private static final String DEFAULT_WHITESPACE_PATTERN = "[\\s]+";

    private static String detectPattern(CharSequence text, int cursor){
        String currentChar = cursor > 0 && text.length() >= cursor ? Character.toString(text.charAt(cursor - 1)) : null;
        return currentChar != null ? currentChar.matches(DEFAULT_WORD_PATTERN) ? DEFAULT_WORD_PATTERN : currentChar.matches(DEFAULT_SYMBOL_PATTERN) ? DEFAULT_SYMBOL_PATTERN : currentChar.matches(DEFAULT_WHITESPACE_PATTERN) ? DEFAULT_WHITESPACE_PATTERN : null : null;
    }

    public static int findTokenStart(CharSequence text, int cursor){
        String detectedPattern = detectPattern(text, cursor);

        if(Objects.equals(DEFAULT_WHITESPACE_PATTERN, detectedPattern)){
            detectedPattern = null;
            while(cursor > -1 && ((detectedPattern = detectPattern(text, cursor)) == null || detectedPattern.equals(DEFAULT_WHITESPACE_PATTERN))){
                cursor--;
            }
        }

        if(detectedPattern == null){
            return -1;
        }

        while(cursor > 0 && Character.toString(text.charAt(cursor - 1)).matches(detectedPattern)){
            cursor--;
        }

        return cursor;
    }

    public static int findTokenEnd(CharSequence text, int cursor){
        String detectedPattern = detectPattern(text, cursor);
        if(detectedPattern == null) return -1;

        while(text.length() > cursor && Objects.equals(detectedPattern, DEFAULT_WORD_PATTERN) && Character.toString(text.charAt(cursor)).matches(detectedPattern)){
            cursor++;
        }

        while(text.length() > cursor && Character.toString(text.charAt(cursor)).matches(DEFAULT_WHITESPACE_PATTERN)){
            cursor++;
        }

        return cursor;
    }

    public static List<String> split(CharSequence text){
        List<String> words = new ArrayList<String>();
        int lastIndex = 0, index = 1;

        while(index > 0 && (index = findTokenEnd(text, index)) > -1){
            String word = text.subSequence(lastIndex, index).toString();
            words.add(word);
            lastIndex = index;
            index++;
        }


        return words;
    }
}
