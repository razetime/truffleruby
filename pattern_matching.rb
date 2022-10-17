# a=3
# case [0]
# in ^a
#   p a
# else
#   "not matched"
# end

h = {a: 42}
case h
in { a: hpatternval }
  p hpatternval
end
