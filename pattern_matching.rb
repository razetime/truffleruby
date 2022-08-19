v = [0, [1, 2, 3], 42]
loop do
case v
in [e, *a, d] # pattern e=0
  p [e, d]
else
  p :failed
end
end

__END__
(RootParseNode,
  (BlockParseNode,
    (LocalAsgnParseNode:v,
      (ArrayParseNode, (FixnumParseNode[value=0]), (ArrayParseNode, (FixnumParseNode[value=1]), (FixnumParseNode[value=2]), (FixnumParseNode[value=3])))
    ),
    (CaseInParseNode, # case
      (LocalVarParseNode:v), # expression
      (ArrayParseNode, # list of in's
        (InParseNode, # in
          (ArrayPatternParseNode, (ArrayParseNode, (LocalAsgnParseNode:e, ), (LocalAsgnParseNode:d, ))), # pattern
          (FCallParseNode:p, (ArrayParseNode, (ArrayParseNode, (LocalVarParseNode:e), (LocalVarParseNode:d))), null), null))))) # body
