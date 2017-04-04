#!/usr/bin/env python2
# -*- coding: utf-8 -*-
"""
Created on Mon Apr  3 16:38:19 2017

Scripts to plot simulation outputs

@author: haoxiang
"""


import glob
import numpy
import matplotlib.pyplot as plt
import re
from pandas import Series


def plot_idle_ratio_versus_num_tasks():
    filenames = glob.glob('IdleRatio-NumTasks/*.csv')
    # plot idle ratio and profits versus Number of Tasks
    fig = plt.figure()
    for f in filenames:
        #print(f)
        label = re.findall(r"M=[\d.]* Rate=[\d]*\.[\d]*",f)[0]
        print(label)
        data = numpy.loadtxt(fname=f, dtype = 'S10,float,float', delimiter=',', unpack=True)
        xticks = [x.replace("\"","") for x in data[0]]
        idle_ratios = data[1]
        profits = data[2]
        idx = range(1, len(idle_ratios)+1)
        plt.xticks(idx, xticks)
        plt.xlabel('Number of Tasks (N)', fontsize=12)
        plt.ylabel('Profits in 1000 Hours ($)', fontsize=12)
        plt.title("Profits versus Number of Tasks")
        plt.plot(idx,profits,label=label)
        plt.legend(loc="center right", bbox_to_anchor=(1.45,0.5),
             ncol=1)
    fig.savefig("Profits versus Number of Tasks.png", bbox_inches='tight')
    
    
    fig = plt.figure()
    for f in filenames:
        #print(f)
        label = re.findall(r"M=[\d.]* Rate=[\d]*\.[\d]*",f)[0]
        print(label)
        data = numpy.loadtxt(fname=f, dtype = 'S10,float,float', delimiter=',', unpack=True)
        xticks = [x.replace("\"","") for x in data[0]]
        idle_ratios = data[1]
        idx = range(1, len(idle_ratios)+1)
        plt.xticks(idx, xticks)
        plt.xlabel('Number of Tasks (N)', fontsize=12)
        plt.ylabel('Idle Ratio', fontsize=12)
        plt.title("Idle Ratios versus Number of Tasks")
        plt.plot(idx,idle_ratios,label=label)
        plt.legend(loc="center right", bbox_to_anchor=(1.45,0.5),
             ncol=1)
    fig.savefig("Idle Ratios versus Number of Tasks.png", bbox_inches='tight')

def plot_dile_ratio_versus_time():
        
    #plot idle ratio evolving with time
    filenames = glob.glob('IdleRatio-Time/*.csv')
    
    for f in filenames:
        title = f.split('/')[-1].replace(".csv","")
        figure = plt.figure()
        series = Series.from_csv(f)
        series = numpy.log(numpy.add(series.astype("float"),1))
        plt.xlabel("Time")
        plt.ylabel("log(Idle Ratio + 1)")
        plt.title(title)
        series.plot()
        figure.savefig("IdleRatio-Time Plots/" + title + ".png")
    


