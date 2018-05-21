/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import org.jcodings.Encoding;
import org.truffleruby.core.basicobject.BasicObjectLayout;

@Layout
public interface EncodingLayout extends BasicObjectLayout {

    DynamicObjectFactory createEncodingShape(DynamicObject logicalClass,
                                             DynamicObject metaClass);

    DynamicObject createEncoding(DynamicObjectFactory factory,
                                 Encoding encoding,
                                 DynamicObject name,
                                 boolean dummy);

    boolean isEncoding(DynamicObject object);
    boolean isEncoding(Object object);

    Encoding getEncoding(DynamicObject object);

    DynamicObject getName(DynamicObject object);

    boolean getDummy(DynamicObject object);

}
