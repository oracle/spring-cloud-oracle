package com.oracle.cstream.sample;

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
        int i = idx.getAndAccumulate(words.length, (x, y) -> {
            if (x < words.length - 1) {
                return x + 1;
            }
            done.set(true);
            return 0;
        });
        return words[i];
    }

    public boolean done() {
        return done.get();
    }
}
