#!/usr/bin/python
__author__ = 'zhengzhongliu'

import os
import operator
import ast
import sys

previousTuple = ""
tupleIdx = 0
isFirstLine = True
tupleCount = 0
arg1TypeMap = {}
arg2TypeMap = {}
out = open('reduced_tuples','w')

def flush():
	#write out previous 
	arg1Type = '-'  if not arg1TypeMap else max(arg1TypeMap.iteritems(), key=operator.itemgetter(1))[0]
	arg2Type = '-'  if not arg2TypeMap else max(arg2TypeMap.iteritems(), key=operator.itemgetter(1))[0]
	out.write("%s\t%d\t%d\t%s\t%s\n"%(previousTuple,tupleIdx,tupleCount,arg1Type,arg2Type))	

base = 'sorted_tuples'

for filename in sorted(os.listdir(base)):
	file = open(base+'/'+filename)
	for line in file:
		parts = line.rstrip().split("\t")
		#sometimes I screw up
		if len(parts) != 4:
			continue
		
		newTuple = parts[0].lower()
		count = int(parts[2])
		typeTuple = parts[3]
		types = typeTuple.rstrip(')').strip('(').split(",")

		if isFirstLine or newTuple == previousTuple:
			if isFirstLine:
				previousTuple = newTuple
				isFirstLine = False
		else:
			flush()	
			#clean up previous container
			previousTuple = newTuple
			tupleCount = 0
			tupleIdx +=1
			arg1TypeMap = {}
			arg2TypeMap = {}
		
		#extend tuple information
		tupleCount +=count 
		
		if len(types) == 2:
			#extend type information 
			if not types[0] == 'null':
				try:
					arg1TypeMap[types[0]] += count
				except KeyError:
					arg1TypeMap[types[0]] = count

			if not types[1] == 'null':
				try:
					arg2TypeMap[types[1]] += count
				except KeyError:
					arg2TypeMap[types[1]] = count


flush()
out.close()
