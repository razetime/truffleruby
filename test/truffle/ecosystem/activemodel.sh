#!/usr/bin/env bash

set -e
set -x

bin/jruby-truffle-tool --dir ../jruby-truffle-gem-test-pack/gem-testing ci --offline activemodel
