# avro-compat-repro
Reproducing forward incompatibility issue with avro generic -> specific conversion


repro'ing issue where schema is updated at runtime but a generic record -> specific record deepcopy fails at runtime due to compiled version being outdated even though the change is forward compatible.

This all comes down to the line here https://github.com/apache/avro/blame/a440eb67d781322d08544c2096a1b0877aa5e027/lang/java/compiler/src/main/velocity/org/apache/avro/compiler/specific/templates/java/classic/record.vm#L223
Where unknown values throw instead of gracefully handle
