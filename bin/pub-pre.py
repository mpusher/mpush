# coding=utf8

import paramiko
import datetime
import telnetlib
import os
import sys


BASEPATH = '/root/mpush'

STARTPROCESS = 'java -jar boot.jar'

GITLABPATH = '/data/localgit/mpush/mpush-boot/target/mpush-release.tar.gz'

ENV= 'pre'


class SSH():
    def __init__(self):
        self.client = None

    def connect(self,host,port=22,username='root',password=None):
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

def getPid(ssh):
    stdin, stdout, stderr = ssh.exe(''' ps aux|grep "mpush-cs.jar" |grep -v "grep"|awk '{print $2}' ''',False)
    return stdout.read().strip()
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

    ##0 assembly
    runShell('mvn clean')
    runShell('mvn package -P %s'%ENV)
    print showText('assembly success','greenText')

    ##1 包创建时间
    runShell('stat -c "%%y" %s'%GITLABPATH)

    confirmPub = raw_input("确认发布(Y/N)：")

    if confirmPub != 'Y':
       return

    ##4 cp
    runShell('cp %s %s'%(GITLABPATH,BASEPATH))
    print showText('cp success','greenText')

    ##5  tar package
    runShell('cd /root/mpush/ && tar -xzvf ./mpush-jar-with-dependency.tar.gz')
    print showText('tar success','greenText')

    ##6 start process
    runShell('nohup /opt/shinemo/jdk1.7.0_40/bin/java -Dio.netty.leakDetectionLevel=advanced -jar /root/mpush/mpush/mpush-cs.jar >> /root/mpush/mpush/nohup.out 2>&1 &')
    print showText('start process success','greenText')


if __name__ == "__main__":
    main()
