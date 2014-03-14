#!/usr/bin/python

from os import listdir
from os.path import isfile,basename

for f in listdir("tuple"):
  if isfile(f):
	  print basename(f)
