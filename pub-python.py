# coding=utf8

import paramiko
import datetime

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


def main():
    for item in HOSTS:
        ssh = SSH().connect(item['HOST'],item['PORT'],username=item['USER'])
        ##back
        base = '/root/mpush/mpush-jar-with-dependency.tar.gz'
        to = '/root/mpush/back/mpush-jar-with-dependency.tar.gz.'+datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        ssh.exe('cp %s %s '%(base,to))
        ssh.close()


if __name__ == "__main__":
    main()
