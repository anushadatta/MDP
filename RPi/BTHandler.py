import multiprocessing
from Queue import Queue
import os
import sys
from bluetooth import *
import threading
import time
import struct
import logging
import json





def removeFilesInFolder(folderpath):
    if os.path.exists(folderpath):
        for filepath in os.listdir(folderpath):
            os.remove(os.path.join(folderpath, filepath))


class BTHandler(multiprocessing.Process):

 
    m = multiprocessing.Manager()
    queueHandler = m.Queue()

 
    deviceList = [
        "68:B3:5E:58:96:E8",  # Android Tablet
        "60:AB:67:91:09:2C",  # DianWei's Phone
    ]

    resultFolder = "/home/pi/checklist-results"
    imageFolder = "/home/pi/checklist-images"
    proResults = []

    def __init__(
        self, port, queueJob, header, fpReceived, fpNow
    ):
        multiprocessing.Process.__init__(self)

        self.fpReceived = fpReceived
        self.fpNow = fpNow
        self.header = header
        self.queueJob = queueJob
        self.daemon = True
        self.port = port
        self.logger = logging.getLogger(__name__)
        self.c = None
        
        self.start()

    def recv(self, c):
        while True:
            try:
                data = c.recv(1024)
              

                if sys.version_info[0] == 3 and type(data) == bytes:  
                    data = data.decode("utf8")

                if len(data) > 0:
                    packet = data
                    self.logger.info("[AND] " + str(packet))
                    if "reset" in str(packet):
                        removeFilesInFolder(self.resultFolder)
                        removeFilesInFolder(self.imageFolder)
                        self.proResults = []
                        self.fpReceived = True
                        self.fpNow = False
                        
                    elif "finish_explore" in str(packet):
                        self.logger.info("finish_explore detected")
                        self.queueJob.put(self.header + ":D:finish_explore")     
                 
                    elif "cmd:explore" in str(packet):
                        self.logger.info("cmd:explore detected")
                        self.queueJob.put(self.header + ":D:create_run")
                     
                
                  
                  
                    self.queueJob.put(self.header + ":" + str(packet))

            except BluetoothError as e:
                self.logger.error("[error][AND] disconnected")
                self.logger.error(e)

                break
            time.sleep(0.2)
        self.c.close()
        self.c = None

    def send(self, c, message):
        if self.c == None:
            self.logger.error("[error][AND] no device connected")
        else:

            self.logger.info("[sending][AND]: " + message)
            self.c.send(str(message + "\n"))
           

    def handleProcessor(self):
        while True:
            if self.queueHandler.qsize() != 0:
                packet = self.queueHandler.get()
                self.queueHandler.task_done()

                self.send(self.c, packet)

            time.sleep(0.2)

    def handle(self, packet):
        self.queueHandler.put(packet)
        
    def closeConnect(self, c):
        self.c.close()

    def getPacketHeader(self):
        return self.header    

    def run(self):
        t2 = threading.Thread(target=self.handleProcessor, args=())
        t2.start()

        socketServer = BluetoothSocket(RFCOMM)
        socketServer.bind(("", self.port))
        socketServer.listen(1)

        while True:
            self.logger.info("[LOG][AND] waiting for connection from device")
            self.c, address = socketServer.accept()
            if address[0] in self.deviceList:

                self.logger.info("[LOG][AND] Connection from: " + str(address))

                t = threading.Thread(target=self.recv, args=(self.c,))
                t.start()
                t.join()
            else:
                self.logger.error(
                    "[error][AND] unlisted device tried to connect. mac address: " + address
                )
                self.c.close()

        self.c.close()
        socketServer.close()
        t2.join()


