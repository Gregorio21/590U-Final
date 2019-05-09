#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed May  1 17:21:11 2019

@author: eric
"""

import numpy as np
import matplotlib.pyplot as plt

filename='data_clean/EMGdata_eric3.csv'
filename2='data_clean/EMGdata_greg3.csv'
threshold=0

emg = np.genfromtxt(filename, delimiter=',', dtype=int)
emg=emg[emg[:,0]>threshold]
plt.plot(emg[:,0], label='user1')

emg = np.genfromtxt(filename2, delimiter=',', dtype=int)
emg=emg[emg[:,0]>threshold]
plt.plot(emg[:,0], label='user2')
#plt.plot(emg[:,2]*np.max(emg[:,0])/4, label='Fatigue')

plt.title('Comparison of EMG Decline')
plt.xlabel('Observations')
plt.ylabel('EMG Value')
plt.legend()

plt.show()