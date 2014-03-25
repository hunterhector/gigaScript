#!/usr/bin/python 
h = open ("allHeads.txt.sorted")
fullOut = open("duplicate.count.full",'w')
parOut = open("duplicate.count.head",'w')

previous = ""
dupIds = []
isFirstLine = True
dCount = 0

for line in h:
	parts = line.rstrip().rsplit(' ', 1)
	text = parts[0]
	docId = parts[1]

	if isFirstLine:
		isFirstLine = False
		previous = text
		previousId = docId 
		continue

	if text == previous:
		dupIds.append(previousId)
		#find duplicate
		dCount +=1
	else:
		#not duplicate with previous
		if dCount > 0:
			dupIds.append(previousId)
			fullOut.write(previous+"\t"+str(dCount)+"\t"+str(dupIds)+"\n")
			parOut.write(str(dCount)+"\t"+dupIds[0]+"\n")
			dupIds = []
			dCount = 0
		
	previous = text
	previousId = docId

if dCount > 0:
	fullOut.write(previous+"\t"+str(dCount)+"\t"+str(dupIds)+"\n")
	parOut.write(str(dCount)+"\t"+dupIds[0]+"\n")
