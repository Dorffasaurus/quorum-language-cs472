#!/bin/sh
#This script takes any generated documentation from the compiler and places it
#into the existing webpage.


echo "Copying documentation."
cp -r Output/Build/Documents/Libraries/ ../../quorum-support/QuorumWebsite/html/Libraries

echo "Finished."