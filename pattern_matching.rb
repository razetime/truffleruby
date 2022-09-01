# a=3
# case [0]
# in ^a
#   p a
# else
#   "not matched"
# end

Point = Struct.new(:x, :y)
p [1,2].deconstruct
p (Point[1,2]).deconstruct
case Point[1,2]
in [a,b]
  p a
  p b
end