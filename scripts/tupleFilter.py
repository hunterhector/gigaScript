#!/usr/bin/python 

#filter low frequency tuples

inFile = open("enriched_tuples")
outFile2 = open('filtered_by_2_tuples','w')
outFile3 = open('filtered_by_3_tuples','w')

for line in inFile:
	parts = line.split("\t")
	if len(parts) != 5:
		print parts

	count = int(parts[2])
	if count >= 3:
		outFile3.write(line)
	if count >= 2:
		outFile2.write(line)

