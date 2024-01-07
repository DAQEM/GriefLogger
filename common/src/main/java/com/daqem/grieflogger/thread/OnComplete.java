package com.daqem.grieflogger.thread;

@FunctionalInterface
public interface OnComplete<T> {

    void onComplete(T type);
}
