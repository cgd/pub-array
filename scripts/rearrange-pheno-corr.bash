#!/bin/bash

tabtocsv GeneExpr.Pheno_corr_probeset.dat | tssql -table phenocorr - 'select probeset_id,cor_BUN,cor_CA,cor_CHOL,cor_GLDH,cor_GLU,cor_HDLD,cor_NEFA,cor_T4,cor_TG,cor_WGT, abs(cor_BUN) as absolute_cor_BUN,abs(cor_CA) as absolute_cor_CA,abs(cor_CHOL) as absolute_cor_CHOL,abs(cor_GLDH) as absolute_cor_GLDH,abs(cor_GLU) as absolute_cor_GLU,abs(cor_HDLD) as absolute_cor_HDLD,abs(cor_NEFA) as absolute_cor_NEFA,abs(cor_T4) as absolute_cor_T4,abs(cor_TG) as absolute_cor_TG,abs(cor_WGT) as absolute_cor_WGT from phenocorr' | csvtotab -
