#!/usr/bin/env runhaskell

\begin{code}

import Control.Monad
import Data.List
import System.Environment
import System.IO
import Text.ParserCombinators.Parsec

-- for pretty printing a single file
main = do
    args <- getArgs
    
    if (length args) /= 1
        then do
            error "expecting a single argument"
        else do
            putStr . fixStatFile . head $ args

-- for pretty printing many lines
--main = getContents >>= putStr . unlines . map fixStatFile . lines

-- for pretty printing old value followed by fixed value
{-
main = do
    contents <- getContents
    let valuesToFix = lines contents
        fixedValues = map fixStatFile valuesToFix
        longestValToFix = longest valuesToFix
        badThenGood =
            zipWith
                (\bad good -> buffWhitespace (longest valuesToFix) bad ++ good)
                valuesToFix
                fixedValues
    
    putStr $ unlines badThenGood
    
    where
        longest = maximum . map length
        buffWhitespace len str = str ++ replicate (1 + len - length str) ' '
-}            

fixStatFile :: String -> String
fixStatFile statFileToFix =
    case parse parseAndFixAny statFileToFix statFileToFix of
        Left err        -> error $ show err
        Right fixedHdr  -> fixedHdr

parseAndFixAny :: GenParser Char st String
parseAndFixAny =
    foldl1 (<|>) $ map try allParsers

allParsers :: [GenParser Char st String]
allParsers =
    [oneDietAllStrains,
     oneSexAllStrains,
     oneStrainSexSexFatFat,
     oneStrainSexFatFat,
     oneStrainSexSexFat,
     oneStrainSexFat,
     oneStrainSexSexFatFatStrainVsStrain,
     parseAndFixOneDietHighVsLow,
     parseAndFixOneSexFVsM,
     oneStrainSexFatStrainVsStrain,
     twoDietSexSexRandStrainFatVsFat,
     twoDietSexRandStrainFatVsFat,
     twoSexFatRandStrainSexVsSex,
     twoSexFatFatRandStrainSexVsSex,
     threeStrainByDietSexFatFatAllStrains,
     threeStrainByDietSexSexFatFatAllStrains,
     threeStrainByDietSexStrainVsStrain,
     fourStrainByDietBySex,
     overallGroup]

-- deal with 1_DIET_F.M.AllStrains_HF.vs.6C_071808_1000PERM.txt
oneDietAllStrains :: GenParser Char st String
oneDietAllStrains = do
    string "1_DIET_F.M.AllStrains_HF.vs.6C_071808_1000PERM.txt"
    eof
    
    return $ modelStr ++ "1_Diet_Female_Male_AllStrains_HighFat_vs_LowFat.txt"

-- deal with 1_SEX_LF.HF.AllStrains_F.vs.M_071608_1000PERM.txt
oneSexAllStrains :: GenParser Char st String
oneSexAllStrains = do
    string "1_SEX_LF.HF.AllStrains_F.vs.M_071608_1000PERM.txt"
    eof
    
    return $ modelStr ++ "1_Sex_LowFat_HighFat_AllStrains_Female_vs_Male.txt"

-- deal with files like "1_STRAIN_F.HF_129.vs.A_071808_1000PERM.txt"
oneStrainSexFatStrainVsStrain :: GenParser Char st String
oneStrainSexFatStrainVsStrain = do
    string "1_STRAIN_"
    sex <- parseSex
    char '.'
    fat <- parseFat
    char '_'
    strain1 <- parseAnyStrain
    string ".vs."
    strain2 <- parseAnyStrain
    
    parseCommonEnding
    
    return $ modelStr ++ "1_Strain_" ++ sex ++ "_" ++ fat ++ "_" ++ strain1 ++ "_vs_" ++ strain2 ++ ".txt"

-- deal with something like 1_STRAIN_F.HF_121108_1000PERM.txt
oneStrainSexFat :: GenParser Char st String
oneStrainSexFat = do
    string "1_STRAIN_"
    sex <- parseSex
    char '.'
    fat <- parseFat
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "1_Strain_" ++ sex ++ "_" ++ fat ++ ".txt"

-- deal with something like 1_STRAIN_F.LF.HF_121108_1000PERM.txt
oneStrainSexFatFat :: GenParser Char st String
oneStrainSexFatFat = do
    string "1_STRAIN_"
    sex <- parseSex
    char '.'
    fat1 <- parseFat
    char '.'
    fat2 <- parseFat
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "1_Strain_" ++ sex ++ "_" ++ fat1 ++ "_" ++ fat2 ++ ".txt"

-- like: 1_STRAIN_F.M.HF_121108_1000PERM.txt
oneStrainSexSexFat :: GenParser Char st String
oneStrainSexSexFat = do
    string "1_STRAIN_"
    sex1 <- parseSex
    char '.'
    sex2 <- parseSex
    char '.'
    fat <- parseFat
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "1_Strain_" ++ sex1 ++ "_" ++ sex2 ++ "_" ++ fat ++ ".txt"

-- like: 1_STRAIN_F.M.LF.HF_121108_1000PERM.txt
oneStrainSexSexFatFat :: GenParser Char st String
oneStrainSexSexFatFat = do
    string "1_STRAIN_"
    sex1 <- parseSex
    char '.'
    sex2 <- parseSex
    char '.'
    fat1 <- parseFat
    char '.'
    fat2 <- parseFat
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "1_Strain_" ++ sex1 ++ "_" ++ sex2 ++ "_" ++ fat1 ++ "_" ++ fat2 ++ ".txt"

-- like: 1_STRAIN_F.M.LF.HF_129.vs.A_071608_1000PERM.txt
oneStrainSexSexFatFatStrainVsStrain :: GenParser Char st String
oneStrainSexSexFatFatStrainVsStrain = do
    string "1_STRAIN_"
    sex1 <- parseSex
    char '.'
    sex2 <- parseSex
    char '.'
    fat1 <- parseFat
    char '.'
    fat2 <- parseFat
    char '_'
    strain1 <- parseAnyStrain
    string ".vs."
    strain2 <- parseAnyStrain
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "1_Strain_" ++ sex1 ++ "_" ++ sex2 ++ "_" ++ fat1 ++ "_" ++ fat2 ++ "_" ++ strain1 ++ "_vs_" ++ strain2 ++ ".txt"

-- deal with files formatted like:
-- 1_DIET_F.129_HF.vs.6C_071808_1000PERM.txt
parseAndFixOneDietHighVsLow :: GenParser Char st String
parseAndFixOneDietHighVsLow = do
    string "1_DIET_"
    sex <- parseSex
    string "."
    strain <- parseAnyStrain
    
    -- we have all that we need, discard the rest
    string "_HF.vs.6C"
    parseCommonEnding
    
    return $ modelStr ++ "1_Diet_" ++ sex ++ "_" ++ strain ++ "_HighFat_vs_LowFat.txt"

-- deal with files formatted like:
-- 1_SEX_HF.129_F.vs.M_071608_1000PERM.txt
parseAndFixOneSexFVsM :: GenParser Char st String
parseAndFixOneSexFVsM = do
    string "1_SEX_"
    fat <- parseFat
    string "."
    strain <- parseAnyStrain
    
    -- we have all that we need, discard the rest
    string "_F.vs.M"
    parseCommonEnding
    
    return $ modelStr ++ "1_Sex_" ++ fat ++ "_" ++ strain ++ "_Female_vs_Male.txt"

-- like 2_DIET_F.M.RandomStrain_HF.vs.6C_071808_1000PERM.txt
twoDietSexSexRandStrainFatVsFat :: GenParser Char st String
twoDietSexSexRandStrainFatVsFat = do
    string "2_DIET_"
    sex1 <- parseSex
    char '.'
    sex2 <- parseSex
    string ".RandomStrain_"
    fat1 <- parseFat
    string ".vs."
    fat2 <- parseFat
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "2_Diet_" ++ sex1 ++ "_" ++ sex2 ++ randStrainStr ++ fat1 ++ "_vs_" ++ fat2 ++ ".txt"

-- like 2_DIET_F.RandomStrain_HF.vs.6C_071808_1000PERM.txt
twoDietSexRandStrainFatVsFat :: GenParser Char st String
twoDietSexRandStrainFatVsFat = do
    string "2_DIET_"
    sex <- parseSex
    string ".RandomStrain_"
    fat1 <- parseFat
    string ".vs."
    fat2 <- parseFat
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "2_Diet_" ++ sex ++ randStrainStr ++ fat1 ++ "_vs_" ++ fat2 ++ ".txt"

-- like 2_SEX_HF.RandomStrain_F.vs.M_071808_1000PERM.txt
twoSexFatRandStrainSexVsSex :: GenParser Char st String
twoSexFatRandStrainSexVsSex = do
    string "2_SEX_"
    fat <- parseFat
    string ".RandomStrain_"
    sex1 <- parseSex
    string ".vs."
    sex2 <- parseSex
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "2_Sex_" ++ fat ++ randStrainStr ++ sex1 ++ "_vs_" ++ sex2 ++ ".txt"

-- like 2_SEX_LF.HF.RandomStrain_F.vs.M_071808_1000PERM.txt
twoSexFatFatRandStrainSexVsSex :: GenParser Char st String
twoSexFatFatRandStrainSexVsSex = do
    string "2_SEX_"
    fat1 <- parseFat
    char '.'
    fat2 <- parseFat
    string ".RandomStrain_"
    sex1 <- parseSex
    string ".vs."
    sex2 <- parseSex
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "2_Sex_" ++ fat1 ++ "_" ++ fat2 ++ randStrainStr ++ sex1 ++ "_vs_" ++ sex2 ++ ".txt"

-- like 3_STRAIN.by.DIET_F.LF.HF.AllStrains_120908_1000PERM.txt
threeStrainByDietSexFatFatAllStrains :: GenParser Char st String 
threeStrainByDietSexFatFatAllStrains = do
    string "3_STRAIN.by.DIET_"
    sex <- parseSex
    char '.'
    fat1 <- parseFat
    char '.'
    fat2 <- parseFat
    string ".AllStrains"
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "3_StrainByDiet_" ++ sex ++ "_" ++ fat1 ++ "_" ++ fat2 ++ "_AllStrains.txt"

-- like 3_STRAIN.by.DIET_F.M.LF.HF.AllStrains_071808_1000PERM.txt
threeStrainByDietSexSexFatFatAllStrains :: GenParser Char st String 
threeStrainByDietSexSexFatFatAllStrains = do
    string "3_STRAIN.by.DIET_"
    sex1 <- parseSex
    char '.'
    sex2 <- parseSex
    char '.'
    fat1 <- parseFat
    char '.'
    fat2 <- parseFat
    string ".AllStrains"
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "3_StrainByDiet_" ++ sex1 ++ "_" ++ sex2 ++ "_" ++ fat1 ++ "_" ++ fat2 ++ "_AllStrains.txt"

-- like 3_STRAIN.by.DIET_F_129vsA_071808_1000PERM.txt
threeStrainByDietSexStrainVsStrain :: GenParser Char st String
threeStrainByDietSexStrainVsStrain = do
    string "3_STRAIN.by.DIET_"
    sex <- parseSex
    char '_'
    strain1 <- parseAnyStrain
    string "vs"
    strain2 <- parseAnyStrain
    
    -- throw out the end
    parseCommonEnding
    
    return $ modelStr ++ "3_StrainByDiet_" ++ sex ++ "_" ++ strain1 ++ "_vs_" ++ strain2 ++ ".txt"

-- 4_STRAIN.by.DIET.by.SEX_F.M.LF.HF.AllStrains_121608_1000PERM.txt
fourStrainByDietBySex :: GenParser Char st String
fourStrainByDietBySex = do
    string "4_STRAIN.by.DIET.by.SEX_F.M.LF.HF.AllStrains_121608_1000PERM.txt"
    eof
    
    return $ modelStr ++ "4_StrainByDietBySex_Female_Male_LowFat_HighFat_AllStrains.txt"

-- OverallModel_Group_F.M.LF.HF_071808_1000PERM.txt
overallGroup :: GenParser Char st String
overallGroup = do
    string "OverallModel_Group_F.M.LF.HF_071808_1000PERM.txt"
    eof
    
    return $ "OverallModel_Group_Female_Male_LowFat_HighFat.txt"

parseSex :: GenParser Char st String
parseSex =
    (char 'F' >> return "Female") <|>
    (char 'M' >> return "Male")

parseFat :: GenParser Char st String
parseFat =
    (string "HF" >> return "HighFat") <|>
    ((string "6C" <|> string "LF") >> return "LowFat")

parseAnyStrain :: GenParser Char st String
parseAnyStrain = foldl1 (<|>) $ map (try . string) allStrains
    where allStrains =
            ["129", "A", "B6", "BALB", "C3H", "CAST", "DBA", "I", "MRL", "NZB",
             "PERA", "SM"]

-- A common function to consume the common "_071808_1000PERM.txt" like tail that
-- all of the names have
parseCommonEnding :: GenParser Char st ()
parseCommonEnding = char '_' >> replicateM_ 6 digit >> string "_1000PERM.txt" >> eof

-- used in a lot of places
modelStr = "Model"
randStrainStr = "_RandomStrain_"

\end{code}
