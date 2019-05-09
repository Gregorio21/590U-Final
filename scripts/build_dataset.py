#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu May  2 14:43:06 2019

@author: eric
"""

import numpy as np
import os


src_folder = "/Users/eric/Desktop/CS590U/project/data_clean/"
    
# settings
window_size=10
window_shift=5

feature_string = "mean,var,range"
feature_funcs = [np.mean, np.var, np.ptp] 


output = []
# extract features from each file and store in output
for filename in os.listdir(src_folder):
    if not filename.endswith(".csv"): continue
    # get raw data
    filepath=src_folder+filename
    raw_data = np.genfromtxt(filepath, delimiter=',', dtype=float)
    X=raw_data[:,0]
    y=raw_data[:,2]
    # interpolate fatigue
    classes=np.unique(y)
    for level in classes:
        inds=np.where(y==level)
        lin = np.linspace(level-1, level, len(inds[0]), endpoint=False)
        y[inds]=lin
    # feature extraction
    window_start=0
    window_end=window_start+window_size
    while(window_end < raw_data.shape[0]):
        window_X = X[window_start:window_end]
        window_y = y[window_start:window_end]
        features = [func(window_X) for func in feature_funcs]
        fatigue = np.mean(window_y)
        output.append(np.hstack([features,fatigue]))
        window_start+=window_shift
        window_end+=window_shift
    
np.savetxt(src_folder+"../dataset.csv", output, 
           delimiter=",", fmt='%.5f', header=feature_string+",fatigue")
    
    