package org.ishwar.sparkchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class AutoCompletionProvider
{
    public final static int DEFAULT_WINDOW_SIZE = 2;

    private SharedPreferences mDatabase;
    private int mWindowSize;
    
    public AutoCompletionProvider(Context context, String modelName, int windowSize) {
        String mModelName = AutoCompletionProvider.class.getSimpleName() + Objects.requireNonNull(modelName);
        this.mDatabase = context.getSharedPreferences(mModelName, Context.MODE_PRIVATE);
        this.mWindowSize = windowSize;
    }

    // Overload the constructor without the window size parameter and set a default value
    public AutoCompletionProvider(Context context, String modelName) {
        this(context, modelName, DEFAULT_WINDOW_SIZE);
    }
    
    public int getWindowSize(){
        return mWindowSize;
    }
    
    public void deleteModel(){
        mDatabase.edit().clear().apply();
    }
    
    public void update(String... lines) {
		new Thread(()->{
			SharedPreferences.Editor editor = mDatabase.edit();
            for (String line : lines) {
            	List<String> words = CompletionTokenizer.split(line.toLowerCase());
            	StringBuilder currentWordBuilder = new StringBuilder();
            	for (int j = 0; j < words.size() - mWindowSize; ++j) {
                	currentWordBuilder.setLength(0); // Clear the StringBuilder for re-use
                	for (int k = j; k < j + mWindowSize; ++k) {
                    	currentWordBuilder.append(words.get(k));
                	}
                	String currentWord = currentWordBuilder.toString();
                	String nextWord = words.get(j + mWindowSize);

                	Set<String> set = mDatabase.getStringSet(currentWord, null);
                	if(set == null){
                    	set = new HashSet<>();
                	}
                
                	if(!set.contains(nextWord)){
                    	set.add(nextWord);
                    	editor.putStringSet(currentWord, set);
                	}
                
            	}
        	}
        editor.apply();
		}).start();

    }
    
    public String nextToken(String sequence) {
        List<String> words = CompletionTokenizer.split(sequence.toLowerCase());
        StringBuilder sequenceBuilder = new StringBuilder();
        for(int i = words.size() - mWindowSize; i > -1 && i<words.size(); ++i){
            sequenceBuilder.append(words.get(i));
        }
        sequence = sequenceBuilder.toString();
        if(mDatabase.contains(sequence)){
            Set<String> nextTokens = mDatabase.getStringSet(sequence, null);
            if(nextTokens != null){
                return chooseToken(nextTokens);
            }
        }
        return null;
    }

    private String chooseToken(Set<String> nextTokens) {
        int randomIndex = ThreadLocalRandom.current().nextInt(nextTokens.size());
        String[] tokens = nextTokens.toArray(new String[0]);
        return tokens.length > randomIndex ? tokens[randomIndex] : null;
    }
}
