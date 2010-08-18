1) followed instructions in shockley email for applying updates to dataset

2) for i in *.txt; do dos2unix $i; done

3) cd 1000PERM_DataBase_081808
   for i in *.txt; do tabtocsv $i | tssql -table tbl - 'select ProbeSetID, FoldChange, Fs_Values, `Ptab_Fs.Permutation` as Ptab, `Qvalue_Fs.Permutation` as Qvalue from tbl' | csvtotab - > ../reformatted/`../renameStatsFile.lhs $i`; done

4) to select all annotations:
tssql -table anno Mouse430_2.na25.annot.csv 'select `Probe Set ID`,`GeneChip Array`,`Species Scientific Name`,`Annotation Date`,`Sequence Type`,`Sequence Source`,`Transcript ID(Array Design)`,`Target Description`,`Representative Public ID`,`Archival UniGene Cluster`,`UniGene ID`,`Genome Version`,Alignments,`Gene Title`,`Gene Symbol`,`Chromosomal Location`,`Unigene Cluster Type`,Ensembl,`Entrez Gene`,SwissProt,EC,OMIM,`RefSeq Protein ID`,`RefSeq Transcript ID`,FlyBase,AGI,WormBase,`MGI Name`,`RGD Name`,`SGD accession number`,`Gene Ontology Biological Process`,`Gene Ontology Cellular Component`,`Gene Ontology Molecular Function`,Pathway,InterPro,`Trans Membrane`,QTL,`Annotation Description`,`Annotation Transcript Cluster`,`Transcript Assignments`,`Annotation Notes` from anno'

5) rename the data headers
   renameCELFile.lhs < Affy061406rma.dat