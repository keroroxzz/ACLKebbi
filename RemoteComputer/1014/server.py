#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket


import numpy as np
import urllib
import cv2

HOST = '127.0.0.1'
PORT = 1111

s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
s.bind((HOST, PORT))

print("Start server...")

while True:
    indata,addr = s.recvfrom(512)

    if len(indata) == 0:
        conn.close()
        print('client closed connection.')
        break

    print(indata)