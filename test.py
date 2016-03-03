# coding=utf8

import paramiko
import datetime
import telnetlib
import os
import sys


class SSH():
    def __init__(self):
        self.client = None

    def connect(self,host,port=9092,username='shinemo',password=None):
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        self.client.connect(host, port, username=username, password=password, timeout=120)
        return self

    def exe(self,cmd,isprint=True):
        if not cmd:
            return
        print greenText(cmd)
        stdin, stdout, stderr = self.client.exec_command(cmd,get_pty=True)
        if isprint:
            for std in stdout.readlines():
                print std,
        print stderr.read()
        return stdin, stdout, stderr


    def close(self):
        if self.client:
            self.client.close()

def showText(s, typ):
    if typ == 'RED':
        return redText(s)
    elif typ == 'GREEN':
        return greenText(s)
    elif typ == 'YELLOW':
        return yellowText(s)
    else:
        return s

def redText(s):
    return "\033[1;31m%s\033[0m" % s

def greenText(s):
    return "\033[1;32m%s\033[0m" % s


def yellowText(s):
    return "\033[1;33m%s\033[0m" % s

def runShell(c):
    print c
    os.system(c)

def main():


    confirmPub = raw_input("")

    print(confirmPub)


if __name__ == "__main__":
    main()
