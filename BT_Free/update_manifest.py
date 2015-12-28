# -*- coding: utf-8 -*-
"""
Created on Sun Dec 14 15:50:54 2014

@author: render
"""

import re

filecontents = ""
manifest_filename = "AndroidManifest.xml"

with open(manifest_filename,"r") as f:
    contents = [i for i in f]
    #print contents
    mysearch = r'(^.+<meta-data android:value=")(\d+)(" android:name="BLOWTORCH_LUA_LIBS_VERSION"/>.+$)'
    for line in contents:
        
        match = re.match(r'^(.+")(\d+)(" android:name="BLOWTORCH_LUA_LIBS_VERSION".+$)',line)
        if match:
            prefix = match.group(1)
            value = int(match.group(2))
            suffix = match.group(3)
            valueinc = value + 1
            print "Updating {0} from version {1} to {2}.".format(manifest_filename,value,valueinc)
            newline = prefix + str(valueinc) + suffix
            
            filecontents = filecontents + newline
        else:
            filecontents = filecontents + line

#manifest_filename = manifest_filename + "2"
with open(manifest_filename,"w") as f:
    f.write(filecontents)
