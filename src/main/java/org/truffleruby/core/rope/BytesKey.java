/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.encoding.EncodingManager;

import java.util.Arrays;

public class BytesKey {

    private final byte[] bytes;
    private final Encoding encoding;
    private final int bytesHashCode;
    private final Hashing hashing;

    public BytesKey(byte[] bytes, Encoding encoding, Hashing hashing) {
        this.bytes = bytes;
        this.encoding = encoding;
        this.bytesHashCode = Arrays.hashCode(bytes);
        this.hashing = hashing;
    }

    @Override
    public int hashCode() {
        return hashing.hash(bytesHashCode);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BytesKey) {
            final BytesKey other = (BytesKey) o;
            return ((encoding == other.encoding) || (encoding == null)) && Arrays.equals(bytes, other.bytes);
        }

        return false;
    }

    @Override
    public String toString() {
        return RopeOperations.decode(EncodingManager.charsetForEncoding(encoding), bytes, 0, bytes.length);
    }

}
