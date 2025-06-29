package org.pierce;

import java.util.List;

public interface Selector<T> {

    T select(List<T> list);

    T select(T[] list);

}
