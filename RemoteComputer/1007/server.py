#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket

import pickle

import numpy as np
import urllib
import cv2

from caca import AnalyzeOneImage


#HOST = '192.168.42.75'
HOST = '192.168.50.197'
PORT = 1111

s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
#s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
#s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((HOST, PORT))
#s.listen(5)

print("Start server...")

'''while True:
    #conn, addr = s.accept()
    indata,addr=s.recvfrom(8192*64)
    #print('connected from ' + str(addr))

    #indata = conn.recv(8192)
    if len(indata) == 0: # connection closed
        conn.close()
        print('client closed connection.')
        break
    
    image=np.asarray(bytearray(indata), dtype="uint8")
    image=cv2.imdecode(image, cv2.IMREAD_COLOR)
    cv2.imshow('URL2Image',image)
    key = cv2.waitKey(1)
    if key == 13:
        AnalyzeOneImage(image, (8, 6))
    elif key == 27:
        break

ret, mtx, dist, rvecs, tvecs = cv.calibrateCamera(objpoints, imgpoints, gray.shape[::-1], None, None)'''