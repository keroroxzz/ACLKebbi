#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import button
import mySocket
import numpy as np
import cv2

HOST = '192.168.50.122'
PORT = 1111

print("Start client...")
socket = mySocket.UdpSocket(HOST, PORT)

# prepare the UI files

background = np.asarray(cv2.imread("background.jpg", cv2.IMREAD_COLOR), dtype=np.uint16)
blurred = np.asarray(cv2.imread("blurred.jpg", cv2.IMREAD_COLOR), dtype=np.uint16)
blurstrength = 0.0

cv2.namedWindow('YEE', cv2.WINDOW_AUTOSIZE)
cv2.setMouseCallback('YEE', button.onMouseClicked)

while True:
    cv2.imshow('YEE',np.clip(background + blurred*np.sin(blurstrength),0,255).astype(np.uint8))

    blurstrength += 0.05

    key = cv2.waitKey(1)
    if key>=0:
        socket.sendToKebbi(chr(key))
        print('Key {0} is pressed.'.format(chr(key)))