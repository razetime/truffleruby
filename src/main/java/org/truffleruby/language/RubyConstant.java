/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;

public class RubyConstant {

    private final DynamicObject declaringModule;
    private final Object value;
    private final boolean isPrivate;
    private final boolean autoload;
    private final boolean isDeprecated;
    private final SourceSection sourceSection;

    public RubyConstant(DynamicObject declaringModule, Object value, boolean isPrivate, boolean autoload, boolean isDeprecated,
                        SourceSection sourceSection) {
        assert RubyGuards.isRubyModule(declaringModule);
        this.declaringModule = declaringModule;
        this.value = value;
        this.isPrivate = isPrivate;
        this.autoload = autoload;
        this.isDeprecated = isDeprecated;
        this.sourceSection = sourceSection;
    }

    public DynamicObject getDeclaringModule() {
        return declaringModule;
    }

    public Object getValue() {
        return value;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public RubyConstant withPrivate(boolean isPrivate) {
        if (isPrivate == this.isPrivate) {
            return this;
        } else {
            return new RubyConstant(declaringModule, value, isPrivate, autoload, isDeprecated, sourceSection);
        }
    }

    public RubyConstant withDeprecated() {
        if (this.isDeprecated()) {
            return this;
        } else {
            return new RubyConstant(declaringModule, value, isPrivate, autoload, true, sourceSection);
        }
    }

    public boolean isVisibleTo(RubyContext context, LexicalScope lexicalScope, DynamicObject module) {
        CompilerAsserts.neverPartOfCompilation();

        assert RubyGuards.isRubyModule(module);
        assert lexicalScope == null || lexicalScope.getLiveModule() == module;

        if (!isPrivate) {
            return true;
        }

        // Look in lexical scope
        if (lexicalScope != null) {
            while (lexicalScope != context.getRootLexicalScope()) {
                if (lexicalScope.getLiveModule() == declaringModule) {
                    return true;
                }
                lexicalScope = lexicalScope.getParent();
            }
        }

        // Look in ancestors
        if (RubyGuards.isRubyClass(module)) {
            for (DynamicObject included : Layouts.MODULE.getFields(module).parentAncestors()) {
                if (included == declaringModule) {
                    return true;
                }
            }
        }

        // Allow Object constants if looking with lexical scope.
        if (lexicalScope != null && context.getCoreLibrary().getObjectClass() == declaringModule) {
            return true;
        }

        return false;
    }

    public boolean isAutoload() {
        return autoload;
    }

}
