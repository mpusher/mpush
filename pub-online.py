# coding=utf8

import paramiko
import datetime
import telnetlib
import os
import sys
import time

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

ENV= 'online'

class Telnet(object):
    def __init__(self, chan):
        self.chan = chan

    def send(self, cmd,isprint=True):
        self.chan.send(cmd+'\n')
        print_cmd(cmd)
        out = ''
        is_recv = False
        is_recv_err = False
        while True:
            # 结束
            if self.chan.recv_stderr_ready():
                tmp = self.chan.recv_stderr(1024)
                if isprint:
                    print_out_stream(tmp)
                out += tmp
                is_recv_err = True
            else:
                if is_recv_err:
                    return out
                else:
                    time.sleep(0.1)

            if self.chan.recv_ready():
                tmp = self.chan.recv(1024)
                if isprint:
                    print_out_stream(tmp)
                out += tmp
                is_recv = True
            else:
                if is_recv:
                    return out
                else:
                    time.sleep(0.1)

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

    def telnet(self, cmd,isprint=True):
        chan = self.client.get_transport().open_session(timeout=10)
        chan.exec_command(cmd)

        t = Telnet(chan)

        is_recv = False

        while True:
            if chan.recv_ready():
                if isprint:
                    print_out_stream(chan.recv(1024))
                is_recv = True
            else:
                if is_recv:
                    return t
                else:
                    time.sleep(0.1)


    def close(self):
        if self.client:
            self.client.close()

def getPid(keyword,ssh):
    stdin, stdout, stderr = ssh.exe(' ps aux|grep %s |grep -v "grep"|awk \'{print $2}\' '%keyword,False)
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

def print_cmd(s):
    """打印执行的命令"""
    print yellowText(s)


def print_out(s):
    """打印执行命令的结果"""
    print greenText(s)


def print_out_stream(s):
    """打印执行命令的结果"""
    sys.stdout.write(greenText(s))

def sleep(checkCount):
    while(checkCount>1):
        checkCount = checkCount - 1
        sys.stdout.write(greenText('  .  '))
        sys.stdout.flush()
        time.sleep(1)
    print greenText('  .  ')

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

        ## remove zk info
        try:
            telnet = ssh.telnet('telnet 127.0.0.1 4001')
            telnet.send(' ',False)
            telnet.send('rcs') ## 删除zk
            telnet.send('quit') ## 关闭连接
        except:
            print showText('telnet exception','redText')


        print showText('start kill process','greenText')

        ##4 kill process  先kill执行。等待15秒后，如果进程还是没有杀掉，则执行kill -9
        pid = getPid(PROCESS_KEY_WORD,ssh)
        if pid :
            ssh.exe('kill %s'%pid)
            sleep(15)
        else:
            print showText('there is no process to kill','YELLOW')
        pid = getPid(PROCESS_KEY_WORD,ssh)
        if pid:
            ssh.exe('kill -9 %s'%pid)


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
