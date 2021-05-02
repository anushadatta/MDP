import time
from picamera import PiCamera
from picamera.array import PiRGBArray
import threading
import multiprocessing
import struct

from Queue import Queue
from datetime import datetime
import logging
import numpy as np
import os


class CameraHandler(multiprocessing.Process):
    m = multiprocessing.Manager()
    queueHandler = m.Queue()
    running = False 


    noOfPhotosPerCMD = 1

    def __init__(self, queueJob, header, sendCam, runID, algoVer):

        self.algoVer = algoVer

        self.runID = runID
        self.sendCam = sendCam

        self.logger = logging.getLogger(__name__)

        multiprocessing.Process.__init__(self)
        self.header = header  
        self.queueJob = queueJob  

        self.logger.info("CameraManager started")
        self.daemon = True
        self.start()

    def run(self):
        self.camera = PiCamera()
        
        self.camera.framerate = 30
        
        self.camera.image_effect = "denoise"
        self.camera.exposure_mode = "antishake"
      
        self.camera.start_preview()
        self.logger.info("Warming up camera...")

        t3 = threading.Thread(target=self.handleProcessor, args=())
        t3.start()

        t3.join()

    def getPacketHeader(self):  
        return self.header

    def handleProcessor(self):
        while True:

            
	    if self.queueHandler.qsize() != 0:	
		print("In If statement")
                
                packet = "R:cam:2:18:1"
		print(packet)


               
                if packet.split(":")[0] != "R" and packet.split(":")[1] != "cam":
                    print("error in packet")
		    self.logger.error("packet with error, skip this packet" + packet)
                    self.queueHandler.task_done()
             
                else:
		    print("Cam packet received")
                    try:
			print(packet)
                        x, y, direction = (
                            packet.split(":")[2],
                            packet.split(":")[3],
                            packet.split(":")[4].strip(),
                        )
                     
                        self.logger.info(
                            "[raspberry] camera is dealing with" + packet + " - right now"
                        )

                        if self.running:
                            self.logger.warn(
                                "[raspberry][warning] cam still busy, packet ignored"
                            )
                        elif not self.running:
                            self.running = True

                            if self.algoVer == 1:
                                for i in range(self.noOfPhotosPerCMD):
                                   
                                    now = datetime.now()
                                    fileNameCustom = (
                                        "checklist-images/"
                                        + now.strftime("%Y%m%d_%H%M%S")
                                        + "-"
                                        + x
                                        + "_"
                                        + y
                                        + "_"
                                        + direction
                                        + "_("
                                        + str(i)
                                        + ").jpg"
                                    )
                                  
                                    fileCustom = open(fileNameCustom, "wb")
                                   
                                    fileCustom.close()
				    self.camera.capture(fileNameCustom)
                                    self.logger.info(
                                        "[raspberry] taken %s" % fileNameCustom
                                    )

                                    newFilePath = fileNameCustom.replace("-temp", "")
                                    os.rename(fileNameCustom, newFilePath)
                                    self.logger.info("[raspberry] changing to %s" % newFilePath)

                            elif self.algoVer == 2:
                                now = datetime.now()
                                filepath = (
                                    "checklist-images/"
                                    + now.strftime("%Y%m%d_%H%M%S")
                                    + ".jpg"
                                )
                                fileCustom = open(filepath, "wb")
                                self.camera.capture(fileCustom, "jpeg")
                                fileCustom.close()
                                self.logger.info("[raspberry] taken %s" % filepath)

                                self.queueJob.put(
                                    self.header
                                    + ":D:save_image:"
                                    + filepath
                                    + ":"
                                    + x
                                    + ":"
                                    + y
                                    + ":"
                                    + direction
                                )

                            self.running = False

                    except Exception as e:
                        self.logger.error("[raspberry][error] " + str(e))
                        self.logger.info("error so skip this image")

                    finally:
                        self.running = False
                      
                        if self.sendCam:
                            self.queueJob.put(self.header + ":A:cam_ok")

    def handle(self, packet):
      
        self.queueHandler.put(packet)

