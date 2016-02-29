# coding=utf8

import paramiko
import datetime
import telnetlib

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

STARTPROCESS = 'java -jar mpush-cs.jar'

GITLABPATH = '/data/localgit/mpush/mpush/target/mpush-jar-with-dependency.tar.gz'


class SSH():
    def __init__(self):
        self.client = None

    def connect(self,host,port=22,username='root',password=None):
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        self.client.connect(host, port, username=username, password=password, timeout=10)
        return self

    def exe(self,cmd,isprint=True):
        if not cmd:
            return
        stdin, stdout, stderr = self.client.exec_command(cmd)
        if isprint:
            for std in stdout.readlines():
                print std,
        return stdin, stdout, stderr


    def close(self):
        if self.client:
            self.client.close()

def getPid(ssh):
    pids = []
    stdin, stdout, stderr = ssh.exe('ps aux|grep "java -jar mpush-cs.jar" |grep -v "grep"')
    for std in stdout.readlines():
        x = std.split(' ')
        if len(x) < 10:
            continue
        if x.index(STARTPROCESS) < 0 :
            continue
        pid = filter(lambda ch: ch!='', std.split(' '))[1]
        pids.append(pid)

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
    for item in HOSTS:
        ssh = SSH().connect(item['HOST'],item['PORT'],username=item['USER'])

        ##1 backup
        base = BASEPATH+'/mpush-jar-with-dependency.tar.gz'
        to = BASEPATH+'/back/mpush-jar-with-dependency.tar.gz.'+datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        ssh.exe('cp %s %s '%(base,to))
        print greenText('backup mpush ok')

        ##telnet remove zk  info
        #ssh.exe('telent 127.0.0.1 4001')
        #ssh.exe('')

        ##2 kill process
        pids = getPid(ssh)
        if len(pids) > 1:
            print redText('mpush-cs server has more than one process.pls kill process by you self')
        elif len(pids) == 0:
            print yellowText('there is no mpush-cs process to kill')
        elif len(pids) == 1:
            ssh.exe('kill -9 %s'%(pids[0]))

        ##3 scp
        runShell('scp -P %s %s %s:%s'%(item['PORT'],GITLABPATH,item['HOST'],BASEPATH+'/mpush')

        ##4  tar package
        ssh.exe('tar -xzvf %s/mpush/mpush-jar-with-dependency.tar.gz'%(BASEPATH))

        ##5 start process
        ssh.exe('java -jar %s/mpush/mpush-cs.jar %'%(BASEPATH))

        ssh.close()


if __name__ == "__main__":
    main()
