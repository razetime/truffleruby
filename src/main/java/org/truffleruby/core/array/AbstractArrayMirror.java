/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractArrayMirror implements ArrayMirror {

    public AbstractArrayMirror() {
        super();
    }

    @Override
    public Object[] getBoxedCopy() {
        return getBoxedCopy(getLength());
    }

    @Override
    public Object[] getBoxedCopy(int newLength) {
        final Object[] boxed = new Object[newLength];
        copyTo(boxed, 0, 0, Math.min(getLength(), newLength));
        return boxed;
    }

    @Override
    public Iterable<Object> iterableUntil(final int length) {
        return new Iterable<Object>() {
    
            private int n = 0;
    
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
    
                    @Override
                    public boolean hasNext() {
                        return n < length;
                    }
    
                    @Override
                    public Object next() throws NoSuchElementException {
                        if (n >= length) {
                            throw new NoSuchElementException();
                        }
    
                        final Object object = get(n);
                        n++;
                        return object;
                    }
    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
    
                };
            }
    
        };
    }

}
