#! /usr/bin/python
# coding=utf8

import paramiko

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

HOSTS = [
    {
        'HOST':'hive1_host',
        'PORT':9092,
        'USER':'shinemo'
    },
    {
        'HOST':'hive2_host',
        'PORT':9092,
        'USER':'shinemo'
    }
]


def main():
    for item in HOSTS:
        ssh = SSH().connect(item['HOST'],item['PORT'],username=item['USER'])
        ssh.exe('ls -l')
        ssh.close()


if __name__ == "__main__":
    main()
