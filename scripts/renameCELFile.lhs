#!/usr/bin/env runhaskell

This file is literate haskell. If that doesn't mean anything to you it's worth
a quick web search before continuing...

Here is an example header row (wrapped for readability):
================================================================================
probeid
GC_430_2_129.F.6C.1_053106_1.CEL
GC_430_2_129.F.6C.2_060506_1.CEL
GC_430_2_129.F.6C.3_052606_1.CEL
GC_430_2_129.F.HF.1_060506_1.CEL
GC_430_2_129.F.HF.2_053006_1.CEL
GC_430_2_129.F.HF.3_060206_1.CEL
GC_430_2_129.M.6C.1_060706_1.CEL
GC_430_2_129.M.6C.2_053106_1.CEL
GC_430_2_129.M.6C.3_060906_1.CEL
GC_430_2_129.M.HF.1_060206_1.CEL
GC_430_2_129.M.HF.2_053106_1.CEL
GC_430_2_129.M.HF.3_060806_1.CEL
GC_430_2_A.F.6C.1_052606_1.CEL
GC_430_2_A.F.6C.2_061206_1.CEL
GC_430_2_A.F.6C.3_060906_1.CEL
GC_430_2_A.F.HF.1_052606_1.CEL
GC_430_2_A.F.HF.2_060706_1.CEL
GC_430_2_A.F.HF.3_061206_1.CEL
GC_430_2_A.M.6C.1_060106_1.CEL
GC_430_2_A.M.6C.2_060806_1.CEL
GC_430_2_A.M.6C.3_052506_1.CEL
GC_430_2_A.M.HF.1.060506_1.CEL
GC_430_2_A.M.HF.2_060906_1.CEL
GC_430_2_A.M.HF.3_060506_1.CEL
GC_430_2_B6.F.6C.1_061206_1.CEL
GC_430_2_B6.F.6C.2_060606_1.CEL
GC_430_2_B6.F.6C.3_060106_1.CEL
GC_430_2_B6.F.HF.1_060606_1.CEL
GC_430_2_B6.F.HF.2_060606_1.CEL
GC_430_2_B6.F.HF.3_060606_1.CEL
GC_430_2_B6.M.6C.1_060906_1.CEL
GC_430_2_B6.M.6C.2_052606_1.CEL
GC_430_2_B6.M.6C.3_060706_1.CEL
GC_430_2_B6.M.HF.1_060906_1.CEL
GC_430_2_B6.M.HF.2_060506_1.CEL
GC_430_2_B6.M.HF.3_060206_1.CEL
GC_430_2_BALB.F.6C.1_060606_1.CEL
GC_430_2_BALB.F.6C.2_060806_1.CEL
GC_430_2_BALB.F.6C.3_060706_1.CEL
GC_430_2_BALB.F.HF.1_060906_1.CEL
GC_430_2_BALB.F.HF.2_060206_1.CEL
GC_430_2_BALB.F.HF.3_052506_1.CEL
GC_430_2_BALB.M.6C.1_053006_1.CEL
GC_430_2_BALB.M.6C.2_053006_1.CEL
GC_430_2_BALB.M.6C.3_053106_1.CEL
GC_430_2_BALB.M.HF.1_052506_1.CEL
GC_430_2_BALB.M.HF.2_060106_1.CEL
GC_430_2_BALB.M.HF.3_052506_1.CEL
GC_430_2_C3H.F.6C.1_060606_1.CEL
GC_430_2_C3H.F.6C.2_052506_1.CEL
GC_430_2_C3H.F.6C.3_060706_1.CEL
GC_430_2_C3H.F.HF.1_060806_1.CEL
GC_430_2_C3H.F.HF.2_053106_1.CEL
GC_430_2_C3H.F.HF.3_053006_1.CEL
GC_430_2_C3H.M.6C.1_052606_1.CEL
GC_430_2_C3H.M.6C.2_060806_1.CEL
GC_430_2_C3H.M.6C.3_060106_1.CEL
GC_430_2_C3H.M.HF.1_060106_1.CEL
GC_430_2_C3H.M.HF.2_060606_1.CEL
GC_430_2_C3H.M.HF.3_052606_1.CEL
GC_430_2_CAST.F.6C.1_060806_1.CEL
GC_430_2_CAST.F.6C.2_053006_1.CEL
GC_430_2_CAST.F.6C.3_052506_1.CEL
GC_430_2_CAST.F.HF.1_060706_1.CEL
GC_430_2_CAST.F.HF.2_053106_1.CEL
GC_430_2_CAST.F.HF.3_060806_1.CEL
GC_430_2_CAST.M.6C.1_060906_1.CEL
GC_430_2_CAST.M.6C.2_060206_1.CEL
GC_430_2_CAST.M.6C.3_060606_1.CEL
GC_430_2_CAST.M.HF.1_060606_1.CEL
GC_430_2_CAST.M.HF.2_053006_1.CEL
GC_430_2_CAST.M.HF.3_060506_1.CEL
GC_430_2_DBA.F.6C.1_060106_1.CEL
GC_430_2_DBA.F.6C.2_052506_1.CEL
GC_430_2_DBA.F.6C.3_060706_1.CEL
GC_430_2_DBA.F.HF.1_053006_1.CEL
GC_430_2_DBA.F.HF.2_053006_1.CEL
GC_430_2_DBA.F.HF.3_060706_1.CEL
GC_430_2_DBA.M.6C.1_060506_1.CEL
GC_430_2_DBA.M.6C.2_060906_1.CEL
GC_430_2_DBA.M.6C.3_060506_1.CEL
GC_430_2_DBA.M.HF.1_061206_1.CEL
GC_430_2_DBA.M.HF.2.060506_1.CEL
GC_430_2_DBA.M.HF.3_052506_1.CEL
GC_430_2_I.F.6C.1_053106_1.CEL
GC_430_2_I.F.6C.2_061206_1.CEL
GC_430_2_I.F.6C.3_053006_1.CEL
GC_430_2_I.F.HF.1_061206_1.CEL
GC_430_2_I.F.HF.2_053106_1.CEL
GC_430_2_I.F.HF.3_052506_1.CEL
GC_430_2_I.M.6C.1_060206_1.CEL
GC_430_2_I.M.6C.2_061206_1.CEL
GC_430_2_I.M.6C.3_052606_1.CEL
GC_430_2_I.M.HF.1_060206_1.CEL
GC_430_2_I.M.HF.2_060906_1.CEL
GC_430_2_I.M.HF.3_060106_1.CEL
GC_430_2_MRL..F.HF.3_060106_1.CEL
GC_430_2_MRL.F.6C.1_060206_1.CEL
GC_430_2_MRL.F.6C.2_060606_1.CEL
GC_430_2_MRL.F.6C.3_060806_1.CEL
GC_430_2_MRL.F.HF.1_060206_1.CEL
GC_430_2_MRL.F.HF.2_053006_1.CEL
GC_430_2_MRL.M.6C.1_053006_1.CEL
GC_430_2_MRL.M.6C.2_060806_1.CEL
GC_430_2_MRL.M.6C.3_060106_1.CEL
GC_430_2_MRL.M.HF.1_052606_1.CEL
GC_430_2_MRL.M.HF.2_060106_1.CEL
GC_430_2_MRL.M.HF.3_060706_1.CEL
GC_430_2_NZB.F.6C.1_060906_1.CEL
GC_430_2_NZB.F.6C.2_060706_1.CEL
GC_430_2_NZB.F.6C.3_060906_1.CEL
GC_430_2_NZB.F.HF.1_052606_1.CEL
GC_430_2_NZB.F.HF.2_053106_1.CEL
GC_430_2_NZB.F.HF.3_053006_1.CEL
GC_430_2_NZB.M.6C.1_060806_1.CEL
GC_430_2_NZB.M.6C.2_060106_1.CEL
GC_430_2_NZB.M.6C.3_052606_1.CEL
GC_430_2_NZB.M.HF.1_061206_1.CEL
GC_430_2_NZB.M.HF.2_060806_1.CEL
GC_430_2_NZB.M.HF.3_053106_1.CEL
GC_430_2_PERA.F.6C.1_061206_1.CEL
GC_430_2_PERA.F.6C.2_060506_1.CEL
GC_430_2_PERA.F.6C.3_052506_1.CEL
GC_430_2_PERA.F.HF.1_052506_1.CEL
GC_430_2_PERA.F.HF.2_061206_1.CEL
GC_430_2_PERA.F.HF.3_060106_1.CEL
GC_430_2_PERA.M.6C.1_060506_1.CEL
GC_430_2_PERA.M.6C.2_060506_1.CEL
GC_430_2_PERA.M.6C.3_053106_1.CEL
GC_430_2_PERA.M.HF.1_060706_1.CEL
GC_430_2_PERA.M.HF.2_052506_1.CEL
GC_430_2_PERA.M.HF.3_052606_1.CEL
GC_430_2_SM.F.6C.1_053106_1.CEL
GC_430_2_SM.F.6C.2_060206_1.CEL
GC_430_2_SM.F.6C.3_060606_1.CEL
GC_430_2_SM.F.HF.1_060606_1.CEL
GC_430_2_SM.F.HF.2_052606_1.CEL
GC_430_2_SM.F.HF.3_060206_1.CEL
GC_430_2_SM.M.6C.1_061206_1.CEL
GC_430_2_SM.M.6C.2_060906_1.CEL
GC_430_2_SM.M.6C.3_060806_1.CEL
GC_430_2_SM.M.HF.1_060206_1.CEL
GC_430_2_SM.M.HF.2_061206_1.CEL
GC_430_2_SM.M.HF.3_060706_1.CEL

These column headers match the name of the CEL files that they came from. We
want to reformat them according to the following:
================================================================================
- Change "6C" to "LF"
- HF=>High Fat, 6C=>Low Fat, M=>Male, F=>Female
- Remove dates and "_1" after dates 
- Add “Replicate” to each biological replicate number at end of header name
  (before date)
- remove GC_430_2 prefix

Special cases for Diet label:
================================================================================
We have 6 special cases which are driven by the the following label miss match
(the design Diet columns are correct but the Array name is "wrong")

Array                          |Dye|Sex|Strain|Diet|Sex_Diet|Group  |Sample|BIOL_REP
GC_430_2_SM.M.6C.1_061206_1.CEL|1  |M  |SM    |HF  |M_HF    |SM_M_HF|139   |1
GC_430_2_SM.M.6C.2_060906_1.CEL|1  |M  |SM    |HF  |M_HF    |SM_M_HF|140   |2
GC_430_2_SM.M.6C.3_060806_1.CEL|1  |M  |SM    |HF  |M_HF    |SM_M_HF|141   |3
GC_430_2_SM.M.HF.1_060206_1.CEL|1  |M  |SM    |6C  |M_6C    |SM_M_6C|142   |1
GC_430_2_SM.M.HF.2_061206_1.CEL|1  |M  |SM    |6C  |M_6C    |SM_M_6C|143   |2
GC_430_2_SM.M.HF.3_060706_1.CEL|1  |M  |SM    |6C  |M_6C    |SM_M_6C|144   |3

And here's the code that does all of this:
================================================================================
\begin{code}

import Control.Monad
import Data.List
import System.IO
import Text.ParserCombinators.Parsec

main = getContents >>= putStr . fixHeaderNames

-- fixes all of the CEL headers in the given TAB delimited input
fixHeaderNames :: String -> String
fixHeaderNames input =
    let (fstLine:remainingLines) = lines input
        (probeIdCol:celCols) = words fstLine
        fixedHeader = probeIdCol : map fixCelHeader celCols
    in
        unlines $ (intercalate "\t" fixedHeader) : remainingLines

-- fixes a single CEL header
fixCelHeader :: String -> String
fixCelHeader headerToFix =
    case parse (parseAndFixCelHeader needToSwapDietLabel) headerToFix headerToFix of
        Left err        -> error $ show err
        Right fixedHdr  -> fixedHdr
    where needToSwapDietLabel =
            headerToFix `elem` arraysWithBadDietLabel
          arraysWithBadDietLabel =
            "GC_430_2_SM.M.6C.1_061206_1.CEL" :
            "GC_430_2_SM.M.6C.2_060906_1.CEL" :
            "GC_430_2_SM.M.6C.3_060806_1.CEL" :
            "GC_430_2_SM.M.HF.1_060206_1.CEL" :
            "GC_430_2_SM.M.HF.2_061206_1.CEL" :
            "GC_430_2_SM.M.HF.3_060706_1.CEL" : []

-- | parses a single ugly column header and produces a pretty column header
-- | like GC_430_2_A.M.HF.1.060506_1.CEL
parseAndFixCelHeader :: Bool -> GenParser Char st String
parseAndFixCelHeader swapDietLabel = do
    let (hfStr, lfStr) = if swapDietLabel then ("6C.", "HF.") else ("HF.", "6C.")
    
    -- discard initial part
    string "GC_430_2_"
    
    -- pull out the parts we care about
    strain <- parseAnyStrain
    char '.'
    try (char '.') <|> return '.'
    
    sex <- (try (string "F.") >> return "Female") <|> (string "M." >> return "Male")
    diet <- (try (string hfStr) >> return "HighFat") <|> (string lfStr >> return "LowFat")
    replicate <- digit >>= \repDigit -> return $ "Replicate" ++ [repDigit]
    
    -- discard the date and trailing "_1.CEL"
    char '_' <|> char '.'
    replicateM_ 6 digit
    string "_1.CEL"
    
    -- put it all together into our new pretty string
    return . unwords $ [strain, sex, diet, replicate]

parseAnyStrain :: GenParser Char st String
parseAnyStrain = foldl1 (<|>) $ map (try . string) allStrains
    where allStrains =
            ["129", "A", "B6", "BALB", "C3H", "CAST", "DBA", "I", "MRL", "NZB",
             "PERA", "SM"]

\end{code}

Test:
================================================================================
here's a little test input to try out for the swapped columns:
probeid GC_430_2_SM.M.6C.1_061206_1.CEL GC_430_2_SM.M.6C.2_060906_1.CEL GC_430_2_SM.M.6C.3_060806_1.CEL GC_430_2_SM.M.HF.1_060206_1.CEL GC_430_2_SM.M.HF.2_061206_1.CEL GC_430_2_SM.M.HF.3_060706_1.CEL
1 2 3 4 5 6 7

Which gets you:
probeid SM Male HighFat Replicate1      SM Male HighFat Replicate2      SM Male HighFat Replicate3      SM Male LowFat Replicate1       SM Male LowFat Replicate2       SM Male LowFat Replicate3
1 2 3 4 5 6 7

Then for some normal columns:
probeid GC_430_2_NZB.F.6C.3_060906_1.CEL GC_430_2_NZB.F.HF.1_052606_1.CEL
1 2 3

Which gets you:
probeid NZB Female LowFat Replicate3    NZB Female HighFat Replicate1
1 2 3
