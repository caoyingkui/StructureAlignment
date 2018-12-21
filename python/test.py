# -- coding: utf-8 -
from nltk.corpus import wordnet as wn
import sqlite3
import numpy as np
from math import *
import math
import sys


def similarity_base_on_wordNet(word1 , word2):
    max = 0
    for wordElement1 in wn.synsets(word1):
        for wordElement2 in wn.synsets(word2):
            temp = wordElement1.path_similarity(wordElement2)
            if temp is not None and temp > max:
                max = temp
    return max

if __name__ == '__main__':
    size = int(len(sys.argv) / 2)
    result = ""
    for i in  [(2 * i + 1) for i in range(0, size)]:
        word1 = sys.argv[i]
        word2 = sys.argv[i + 1]
        result += word1 + " " + word2 + " " + str(similarity_base_on_wordNet(word1, word2)) + "\n"

    print(result)