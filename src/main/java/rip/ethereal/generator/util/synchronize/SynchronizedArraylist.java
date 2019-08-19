package rip.ethereal.generator.util.synchronize;

import java.util.ArrayList;

/**
 * @author antja03
 * @since 8/7/2019
 */
public class SynchronizedArraylist<T> extends ArrayList<T> {

    @Override
    public synchronized boolean add(T t) {
        return super.add(t);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return super.remove(o);
    }

}
