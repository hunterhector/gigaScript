#!/usr/bin/python
import sys
import os
from os import listdir

arg = sys.argv

batchName = arg[1]
baseDir = arg[2]+"/"

print "batch name is ",batchName

tupleHeader = "gigaScript_"+batchName+"_Tuples"
bigramHeader = "gigaScript_"+batchName+"_BigramCounts"

tupleDir = baseDir+"tuple"
bigramDir = baseDir+"bigram"
outDir = baseDir+"out"

if not os.path.exists(outDir):
    os.mkdir(outDir)

for f in os.listdir(tupleDir):
    basename = os.path.basename(f)
    if basename.startswith(tupleHeader):
        batchIndex = basename[len(tupleHeader):]
        bigramFile = open(bigramDir + "/" + bigramHeader+batchIndex)
        tupleFile = open(tupleDir +"/" + f)

        output = open(outDir+"/lexicalized_bigram_"+batchIndex,'w')

        index2Tuple = {}
        for line in tupleFile.readlines():
            parts = line.split("\t");
            if len(parts) != 4:
                #        print parts
                continue
            tupleText = parts[0]
            tupleIndex = parts[1].split("_")[0]
            index2Tuple[tupleIndex] = tupleText

        for line in bigramFile.readlines():
            parts = line.split("\t",1)
            bigramIndices = parts[0].split("_")[0].split(",")

            if not index2Tuple.has_key(bigramIndices[0]):
                continue
            if not index2Tuple.has_key(bigramIndices[1]):
                continue

            output.write(index2Tuple[bigramIndices[0]])
            output.write("\t")
            output.write(index2Tuple[bigramIndices[1]])
            output.write("\t")
            output.write(parts[1])

        output.close()