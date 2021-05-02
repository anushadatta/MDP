import serial
from sys import stderr
import multiprocessing
import time
from Queue import Queue
from collections import Counter
import sys
import json
import logging
import threading


class ArduinoHandler(multiprocessing.Process):
    threadLock = threading.Lock()
    serialPort = serial.Serial()
    m = multiprocessing.Manager()
    queueHandler = m.Queue()
    moveCount = 0
    orientation = "right"
    calibrateAllCell = []
    thresholds = [3, 3, 3, 5, 3, 3]
    connected = False
    x1 = 1
    y1 = 18
    count = 0
    sensorDataNoNoise = []

    def __init__(
        self,
        port,
        baud,
        timeout,
        queueJob,
        header,
        sendCamTrue,
        fpStatReceived,
        fpNow,
    ):

        self.sendCamTrue = sendCamTrue
        self.fpStatReceived = fpStatReceived
        self.fpNow = fpNow

        multiprocessing.Process.__init__(self)
       
        self.serialPort.timeout = timeout
        self.y1 = int(self.y1)
        self.x1 = int(self.x1)
        self.header = header
        self.serialPort.baudrate = baud
        self.serialPort.port = port
      
        self.daemon = True
        self.queueJob = queueJob
        self.logger = logging.getLogger(__name__)
    
        self.start()

    def connect(self):

     
        if self.serialPort.isOpen():
            self.serialPort.close()

        self.logger.info("[AR] opening serial port")

        error_already_logged = False

        while True:  
            try:
                self.serialPort.open()
                self.connected = True
                self.logger.info("[AR] arduino started")
                return True
            except serial.SerialException as e:
                time.sleep(0.6)
                if not error_already_logged:
                    self.logger.error(e)
                    error_already_logged = True
               


    def run(self):
        if self.connect() == True:
            t1 = threading.Thread(target=self.read, args=(self.queueJob,))
            t2 = threading.Thread(target=self.handleProcessor, args=())
            t1.start()
            t2.start()

            t1.join()
            t2.join()

    def handleProcessor(self):
        while True:
            if self.queueHandler.qsize() != 0:
                packet = self.queueHandler.get().strip()
                self.logger.info("[AR] Handling : %s \n" % packet)
                splitData = packet.split(":")
                command = splitData[1]
                
                if "start_path" in packet:
                    self.fpNow = True


                if command == "cmd":
                    direction = splitData[2].strip()
                    self.moveCount = self.moveCount + 1

                    if direction == "forward":
                        if self.orientation == "right":
                            self.x1 = self.x1 + 1
                        if self.orientation == "left":
                            self.x1 = self.x1 - 1
                        if self.orientation == "up":
                            self.y1 = self.y1 - 1
                        if self.orientation == "down":
                            self.y1 = self.y1 + 1
                    if direction == "bottom":
                        if self.orientation == "right":
                            self.x1 = self.x1 - 1
                        if self.orientation == "left":
                            self.x1 = self.x1 + 1
                        if self.orientation == "up":
                            self.y1 = self.y1 + 1
                        if self.orientation == "down":
                            self.y1 = self.y1 - 1
                    if direction == "right":
                        self.orientation = {
                            "up": "right",
                            "down": "left",
                            "right": "down",
                            "left": "up",
                            
                        }[self.orientation]
                    if direction == "left":
                        self.orientation = {
                            
                            "up": "left",
                            "down": "right",
                            "right": "up",
                            "left": "down",
                        }[self.orientation]

                self.queueHandler.task_done()

            time.sleep(0.2)
            
    
  

        while True:

            if not self.serialPort.isOpen():
                self.logger.error(
                    "[AR] Serial port not open, trying to reconnect"
                )

            elif self.serialPort.isOpen():

               

                while True:
                    if self.fpNow and "cmd" in message:
                        if not self.fpStatReceived:
                            pass
                            self.logger.info(
                                "sleeping thread"
                            )
                            time.sleep(0.001)
                        else:
                            self.logger.info(
                                "fastest path, send next command"
                            )
                            self.fpStatReceived = False
                            break
                    else:
                        break

                if sys.version_info[0] == 3: 
                    self.serialPort.write(str.encode(message + "\n"))
                else:
                    self.serialPort.write(str(message + "\n"))

                self.logger.info("[SEND][AR] " + str(message))

              
                packet = str(message)
                
                if "start_explore" in packet:
                    try:
                        wait_cam_ok = packet.split("start_explore:")[1].split(":")[0]
                    except IndexError:
                        wait_cam_ok = None 

                    if wait_cam_ok == "0":
                        self.sendCamTrue = False
                        self.logger.info("sendCamTrue set to False")
                     
                    elif wait_cam_ok == "1":
                        self.sendCamTrue = True
                        self.logger.info("sendCamTrue set to True")
                     
                    else:
                        self.logger.error(
                            "[PC] only start:explore:1"
                        )
             
                return
            else:
              
                self.logger.error("reconnecting")
                self.connect()
                time.sleep(0.6)

    def read(self, queueJob):
        in_buffer = ""

        while True:
            try:
                data = self.serialPort.readline().strip()

                if len(data) == 0:
                    continue

                if sys.version_info[0] == 3 and type(data) == bytes:
                    data = data.decode()
            

                if not data.endswith("$"):
                    in_buffer += data
                    continue

                in_buffer += data[:-1]

                splitData = in_buffer.split(":")
                if len(splitData) > 2:
                   
                    if splitData[1] == "cmd" and splitData[2] == "ack":
                        queueJob.put(self.header + ":" + in_buffer)
                    if splitData[1] == "dist":
                        sensorData = [
                            0 if x < 0 else (x - 5) for x in json.loads(splitData[2])
                        ]
                      
                        cells = [int(round(x / 10)) for x in sensorData]
                        cells = [
                            0 if x > threshold else x + 1
                            for x, threshold in zip(cells, self.thresholds)
                        ]
                        self.sensorDataNoNoise.append(Counter(cells).most_common(1)[0][0])

                    
                        self.count = self.count + 1
                        if self.count == 6:
                            self.count = 0
                            toSend = "map:sensor:[{},{}]:{}:{}".format(
                                self.x1,
                                self.y1,
                                self.orientation.strip().strip(),
                                self.sensorDataNoNoise,
                            )
                         
                            queueJob.put(self.header + ":P:" + toSend)
                          
                            self.calibrateAllCell = self.sensorDataNoNoise
                            self.sensorDataNoNoise = []
                    else:
                        self.logger.info("[receive][AR] " + in_buffer)
                        if "stat" in in_buffer:
                            self.fpStatReceived = True
                        queueJob.put(self.header + ":" + in_buffer)
                else:
                    self.logger.info("[receive][AR] " + in_buffer)
                    if "stat" in in_buffer:
                        self.fpStatReceived = True
                    queueJob.put(self.header + ":" + in_buffer)
                in_buffer = ""

            except serial.SerialException as e:
                print("print >>")
              
                self.logger.error(e)
                while self.connected == False:
                    self.connect()
                    self.logger.info("reconnect to arduino")
                    time.sleep(3.5)
                break

            time.sleep(0.2)
            
    def getPacketHeader(self):
        return self.header

    def handle(self, packet):
        self.queueHandler.put(packet)

    def write(self, message):
        message = message.strip()
