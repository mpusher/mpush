#!/usr/bin/python
# coding=utf8

import datetime
import os
import sys
import time

str = raw_input("请输入：");
print "你输入的内容是: ", str

def redText(s):
    return "\033[1;31m%s\033[0m" % s

def greenText(s):
    return "\033[1;32m%s\033[0m" % s


def yellowText(s):
    return "\033[1;33m%s\033[0m" % s

def sleep(checkCount):
    while(checkCount>1):
        checkCount = checkCount - 1
        sys.stdout.write(greenText('  .  '))
        sys.stdout.flush()
        time.sleep(1)
    print greenText('  .  ')

sleep(3)
