#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import socket

class UdpSocket:
    
    thesocket = None
    HOST = '192.168.50.122'
    PORT = 1111

    def __init__(self, host, port):
        self.HOST = host
        self.PORT = port
        self.thesocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        self.thesocket.connect((self.HOST, self.PORT))

    # MSG is everything
    def sendToKebbi(self, msg):
        self.thesocket.send(msg.to_bytes(len(msg), byteorder="little"))
        print('Send: {0}.'.format(msg))