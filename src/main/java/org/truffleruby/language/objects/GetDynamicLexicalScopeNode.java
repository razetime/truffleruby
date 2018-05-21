/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

public class GetDynamicLexicalScopeNode extends RubyNode {

    @Override
    public Object execute(VirtualFrame frame) {
        return RubyArguments.getMethod(frame).getLexicalScope();
    }

}
