# coding=utf8

import paramiko
import datetime
import telnetlib
import os
import sys

HOSTS = [
    {
        'HOST':'hive1_host',
        'PORT':9092,
        'USER':'root'
    },
    {
        'HOST':'hive2_host',
        'PORT':9092,
        'USER':'root'
    }
]

BASEPATH = '/root/mpush'

MPUSH_TAR_NAME = 'mpush-jar-with-dependency.tar.gz'

PROCESS_KEY_WORD = 'mpush-cs.jar'

GITLABPATH = '/data/localgit/mpush/mpush/target/'+MPUSH_TAR_NAME

JAVA_PATH = '/opt/shinemo/jdk1.7.0_40/bin/java'

ENV= 'daily'


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
        stdin, stdout, stderr = self.client.exec_command(cmd)
        if isprint:
            for std in stdout.readlines():
                print std,
        print stderr.read()
        return stdin, stdout, stderr


    def close(self):
        if self.client:
            self.client.close()

def getPid(ssh):
    stdin, stdout, stderr = ssh.exe(' ps aux|grep %s |grep -v "grep"|awk \'{print $2}\' '%PROCESS_KEY_WORD,False)
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

    ##0 git pull
    runShell('git pull origin master')
    print showText('git pull master success','greenText')

    ##1 assembly
    runShell('mvn clean install  assembly:assembly -P %s'%ENV)
    print showText('assembly success','greenText')

    ##2 包创建时间
    runShell('stat -c "%%y" %s'%GITLABPATH)

    confirmPub = raw_input("确认发布(y/n)：")

    if confirmPub != 'y':
       return

    for item in HOSTS:

        pubHost = raw_input("发布 %s (y/n)："%item['HOST'])
        if pubHost != 'y':
           return

        ssh = SSH().connect(item['HOST'],item['PORT'],username=item['USER'])

        ##3 backup
        base = BASEPATH+'/'+MPUSH_TAR_NAME
        to = BASEPATH+'/back/'+MPUSH_TAR_NAME+'.'+datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        ssh.exe('mv %s %s '%(base,to))
        print showText('backup mpush ok','greenText')

        ##4 kill process
        pid = getPid(ssh)
        if pid :
            ssh.exe('kill -9 %s'%pid)
        else:
            print showText('there is no process to kill','YELLOW')

        ##5 scp
        runShell('scp -P %s %s %s:%s'%(item['PORT'],GITLABPATH,item['HOST'],BASEPATH))
        print showText('scp success','greenText')

        ##6  tar package
        ssh.exe('cd %s && rm -rf mpush/ && tar -xzvf ./%s'%(BASEPATH,MPUSH_TAR_NAME),False)
        print showText('tar success','greenText')

        ##7 start process
        ssh.exe('nohup %s -jar %s/mpush/%s >> %s/mpush/nohup.out 2>&1 &'%(JAVA_PATH,BASEPATH,PROCESS_KEY_WORD,BASEPATH))
        print showText('start process success','greenText')


        ssh.close()


if __name__ == "__main__":
    main()
