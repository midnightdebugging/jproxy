package org.pierce.imp;

import org.pierce.Selector;

import java.util.List;

public class DefaultSelector<T> implements Selector<T> {

    double random = Math.random();

    @Override
    public T select(List<T> list) {
        int i = (int) (random * list.size());
        return list.get(i);
    }

    @Override
    public T select(T[] array) {
        int i = (int) (random * array.length);
        return array[i];
    }
}
