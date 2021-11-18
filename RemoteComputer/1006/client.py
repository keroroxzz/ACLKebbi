#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket

print("starting...")

HOST = '192.168.0.129'
#HOST = '192.168.0.167'
PORT = 1111

print("init...")
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((HOST, PORT))

print("start the loop...")
while True:

    print("try getting inpput...")
    outdata = input('please input message: ')
    print('send: ' + outdata)
    s.send(outdata.encode())
    
    indata = s.recv(1024)
    if len(indata) == 0: # connection closed
        s.close()
        print('server closed connection.')
        break
    print('recv: ' + indata.decode())