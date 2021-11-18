#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket

HOST = '192.168.43.232'
PORT = 1111

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((HOST, PORT))
s.listen(5)

print("Start server...")

while True:
    conn, addr = s.accept()
    print('connected from ' + str(addr))

    while True:
        indata = conn.recv(4096)
        if len(indata) == 0: # connection closed
            conn.close()
            print('client closed connection.')
            break
        f = open('test.jpg', "wb")
        f.write(indata)
        print('recv: ' + indata)