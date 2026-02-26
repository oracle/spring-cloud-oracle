package com.oracle.database.spring.cloud.stream.binder.sample;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class WordSupplier implements Supplier<String> {
    private final String[] words;
    private final AtomicInteger idx = new AtomicInteger(0);
    private final AtomicBoolean done = new AtomicBoolean(false);

    public WordSupplier(String phrase) {
        this.words = phrase.split(" ");
    }

    @Override
    public String get() {
    	int currentIdx = idx.getAndIncrement();

        // Check if we still have words to send
        if (currentIdx < words.length) {
            // If this was the very last word, mark us as done
            if (currentIdx == words.length - 1) {
                done.set(true);
            }
            return words[currentIdx];
        }

        done.set(true);
        
        // Returning null tells Spring Cloud Stream to skip this poll
        return null;
    }

    public boolean done() {
        return done.get();
    }
}
