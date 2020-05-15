/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array.library;

import java.util.Set;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
@ImportStatic(ArrayGuards.class)
public class DelegatedArrayStorage implements ObjectGraphNode {

    public final Object storage;
    public final int offset;
    public final int length;

    @ExportMessage
    protected static boolean accepts(DelegatedArrayStorage store,
            @CachedLibrary(limit = "1") ArrayStoreLibrary backingStores) {
        return backingStores.accepts(store.storage);
    }

    @ExportMessage
    protected Object read(int index,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.read(storage, index + offset);
    }

    @ExportMessage
    protected boolean isPrimitive(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isPrimitive(storage);
    }

    @ExportMessage
    @TruffleBoundary
    protected String toString(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return String.format("Delegate of (%s)", stores.toString(storage));
    }

    @ExportMessage
    protected int capacity() {
        return length;
    }

    @ExportMessage
    protected Object expand(int capacity) {
        return new DelegatedArrayStorage(storage, offset, capacity);
    }

    @ExportMessage
    protected Object extractRange(int start, int end) {
        return new DelegatedArrayStorage(storage, (offset + start), (end - start));
    }

    @ExportMessage
    protected Object[] boxedCopyOfRange(int start, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.boxedCopyOfRange(storage, offset + start, length);
    }

    @ExportMessage
    protected void copyContents(int srcStart, Object destStore, int destStart, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary srcStores,
            @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary destStores) {
        for (int i = 0; i < length; i++) {
            destStores.write(destStore, i + destStart, srcStores.read(storage, srcStart + offset + i));
        }
    }

    @ExportMessage
    protected Object toJavaArrayCopy(int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        Object newStore = stores.allocator(storage).allocate(length);
        stores.copyContents(storage, 0, newStore, offset, length);
        return newStore;
    }

    @ExportMessage
    protected void sort(int size) {
        throw new UnsupportedOperationException();
    }

    @ExportMessage
    protected Iterable<Object> getIterable(int from, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.getIterable(storage, from + offset, length);
    }

    @ExportMessage
    protected ArrayAllocator generalizeForValue(Object newValue,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.generalizeForValue(storage, newValue);
    }

    @ExportMessage
    protected ArrayAllocator generalizeForStore(Object newStore,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.generalizeForStore(newStore, storage);
    }

    @ExportMessage
    protected Object allocateForNewValue(Object newValue, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.allocateForNewValue(storage, newValue, length);
    }

    @ExportMessage
    protected Object allocateForNewStore(Object newStore, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.allocateForNewStore(storage, newStore, length);
    }

    @ExportMessage
    protected boolean isDefaultValue(Object value,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isDefaultValue(storage, value);
    }

    @ExportMessage
    protected ArrayAllocator allocator(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.allocator(storage);
    }

    public DelegatedArrayStorage(Object storage, int offset, int length) {
        assert offset >= 0;
        assert length >= 0;
        assert !(storage instanceof DelegatedArrayStorage);
        this.storage = storage;
        this.offset = offset;
        this.length = length;
    }

    public boolean hasObjectArrayStorage() {
        return storage != null && storage.getClass() == Object[].class;
    }

    @Override
    public void getAdjacentObjects(Set<DynamicObject> reachable) {
        if (hasObjectArrayStorage()) {
            final Object[] objectArray = (Object[]) storage;

            for (int i = offset; i < offset + length; i++) {
                final Object value = objectArray[i];
                if (value instanceof DynamicObject) {
                    reachable.add((DynamicObject) value);
                }
            }
        }
    }

}
