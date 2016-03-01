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

ENV= 'daily'


class SSH(object):
    def __init__(self):
        self.client = None
        self.chan = None
        self.shell = None

    def __enter__(self):
        return self

    def __exit__(self, typ, value, trace):
        self.close()

    def connect(self, host, port=22, username='root', password=None):
        self.client = paramiko.SSHClient()
        ##self.client.load_system_host_keys()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        self.client.connect(host, port, username=username, password=password, timeout=10)
        return self

    def close(self):
        if self.client:
            self.client.close()

    def exec_command(self, cmd, isprint=True):
        """执行命令，每次执行都是新的session"""
        if not cmd:
            return
        print_cmd(cmd)

        stdin, stdout, stderr = self.client.exec_command(cmd)
        out = stdout.read()
        if isprint:
            print_out(out)

        err = stderr.read()
        if err:
            print_out(err)
        return out, err

    def _invoke_shell(self):
        """创建一个shell"""
        self.shell = self.client.invoke_shell(width=200)
        is_recv = False
        while True:
            if self.shell.recv_ready():
                print_out_stream(self.shell.recv(1024))
                is_recv = True
            else:
                if is_recv:
                    return
                else:
                    time.sleep(0.1)

    def shell_exec(self, cmd):
        """在shell中执行命令,使用的是同一个session"""
        if not cmd:
            return

        if not self.shell:
            self._invoke_shell()

        self.shell.send(cmd + "\n")

        out = ''
        is_recv = False
        while True:
            if self.shell.recv_ready():
                tmp = self.shell.recv(1024)
                out += tmp
                print_out_stream(tmp)
                is_recv = True
            else:
                if is_recv:
                    return out
                else:
                    time.sleep(0.1)

def getPid(ssh):
    stdout = ssh.shell_exec(' ps aux|grep %s |grep -v "grep"|awk \'{print $2}\' '%PROCESS_KEY_WORD)
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

        ssh = SSH().connect(item['HOST'],item['PORT'],item['USER'])

        ##3 backup
        base = BASEPATH+'/'+MPUSH_TAR_NAME
        to = BASEPATH+'/back/'+MPUSH_TAR_NAME+'.'+datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        ssh.shell_exec('mv %s %s '%(base,to))
        print showText('backup mpush ok','greenText')

        ## remove zk info


        ##4 kill process
        pid = getPid(ssh)
        if pid :
            ssh.shell_exec('kill -9 %s'%pid)
        else:
            print showText('there is no process to kill','YELLOW')

        ##5 scp
        runShell('scp -P %s %s %s:%s'%(item['PORT'],GITLABPATH,item['HOST'],BASEPATH))
        print showText('scp success','greenText')

        ##6  tar package
        ssh.shell_exec('cd %s && rm -rf mpush/ && tar -xzvf ./%s'%(BASEPATH,MPUSH_TAR_NAME))
        print showText('tar success','greenText')

        ##7 start process
        ssh.shell_exec('nohup %s -jar %s/mpush/%s >> %s/mpush/nohup.out 2>&1 &'%(JAVA_PATH,BASEPATH,PROCESS_KEY_WORD,BASEPATH))
        print showText('start process success','greenText')


        ssh.close()


if __name__ == "__main__":
    main()
