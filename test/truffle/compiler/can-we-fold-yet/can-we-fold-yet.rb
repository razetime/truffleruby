# Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Use --experimental-options --engine.IterativePartialEscape

require 'readline'

abort 'You need the GraalVM Compiler to run this' unless TruffleRuby.jit?

puts 'Can Truffle constant fold yet?'

loop do
  code = Readline.readline('> ', true)

  test_thread = Thread.new do
    begin
      eval "loop { Primitive.assert_compilation_constant #{code}; Primitive.assert_not_compiled; Thread.pass }", nil, __FILE__, __LINE__
    rescue Truffle::GraalError => e
      if e.message.include? 'Primitive.assert_not_compiled'
        puts "Yes! Truffle can constant fold this to #{eval(code).inspect}"
      elsif e.message.include? 'Primitive.assert_compilation_constant'
        puts "No :( Truffle can't constant fold that"
      else
        puts 'There was an error executing that :('
      end
    end
  end

  unless test_thread.join(5)
    puts 'That timed out :( either it takes too long to execute or to compile'
  end
end
