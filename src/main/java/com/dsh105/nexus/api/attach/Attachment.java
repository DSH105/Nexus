package com.dsh105.nexus.api.attach;

public abstract class Attachment<T> {

    public abstract T get();

    @Override
    public String toString() {
       return get().toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
