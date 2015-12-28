# -*- coding: utf-8 -*-
"""
Created on Sun Dec 14 15:50:54 2014

@author: render
"""

import os
import re

filecontents = ""
manifest_filename = os.path.dirname(os.path.realpath(__file__)) + "/assets/share/lua/5.1/buttonserver.lua"

with open(manifest_filename,"r") as f:
    contents = [i for i in f]
    #print contents
    mysearch = r'(^.+local buttonWindowName = ")(button_window_ex)(".+$)'
    for line in contents:
        
        match = re.match(r'^(.+")(button_window_ex)(".+$)',line)
        if match:
            prefix = match.group(1)
            value = match.group(2)
            suffix = match.group(3)
            valueinc = 'button_window'
            print "Updating {0} buttonWindowName =  {1} to {2}.".format(manifest_filename,value,valueinc)
            newline = prefix + str(valueinc) + suffix + '\n'
            
            filecontents = filecontents + newline
        else:
            filecontents = filecontents + line

#manifest_filename = manifest_filename + "2"
with open(manifest_filename,"w") as f:
    f.write(filecontents)

filecontents = ""
manifest_filename = os.path.dirname(os.path.realpath(__file__)) + "/assets/share/lua/5.1/buttonwindow.lua"

with open(manifest_filename,"r") as f:
    contents = [i for i in f]
    #print contents
    for line in contents:

        match = re.match(r'^(.+")(Button Sets Ex)(".+$)',line)
        if match:
            prefix = match.group(1)
            value = match.group(2)
            suffix = match.group(3)
            valueinc = 'Button Sets'
            print "Updating {0} buttonWindowName =  {1} to {2}.".format(manifest_filename,value,valueinc)
            newline = prefix + str(valueinc) + suffix + '\n'

            filecontents = filecontents + newline
        else:
            filecontents = filecontents + line

#manifest_filename = manifest_filename + "2"
with open(manifest_filename,"w") as f:
    f.write(filecontents)

