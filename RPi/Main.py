
from ArduinoHandler import ArduinoHandler
import logging
import time
from datetime import datetime
from Queue import Queue
import os
import sys

import multiprocessing

from PCHandler import PCHandler
from BTHandler import BTHandler
from CameraHandler import CameraHandler

from PacketsHandler import *


jobList = []
m = multiprocessing.Manager()
queueJob = m.Queue()

logsDirectory = "logs"
if not os.path.exists(logsDirectory):
    os.makedirs(logsDirectory)

logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s %(message)s",

    filename=os.path.join(logsDirectory, datetime.now().strftime("%Y%m%d-%H%M%S") + ".log"),
    filemode="w",
)


currentRunNumber = None
algoVer = 1 
debugGoThru = False
waitRpi = True
sendCamera = False


fpReceived = True  
fpNow = False  



ph = PacketHandler()

def remvoveFilesInFolder(folderpath):
    if os.path.exists(folderpath):
        for filepath in os.listdir(folderpath):
            os.remove(os.path.join(folderpath, filepath))

logging.info("rpi start")
pc = PCHandler("192.168.9.9", 8081, queueJob, "P")
ph.registerHandler(pc)
jobList.append(pc)



logging.info("bluetooth start")
bt = BTHandler(4, queueJob, "B", fpReceived, fpNow)
ph.registerHandler(bt)
jobList.append(bt)

logging.info("arduino start")  
arduino = ArduinoManager(
    "/dev/ttyACM0",
    115200,
    0,
    queueJob,
    "A",
    sendCamera,
    fpReceived,
    fpNow,
) 
ph.registerHandler(arduino)
jobList.append(arduino)



logging.info("camera start")
c = CameraHandler(queueJob, "R", sendCamera, currentRunNumber, algoVer)
ph.registerHandler(c)
jobList.append(c)


if algoVer == 1:
    resultsFolder = "/home/pi/checklist-results"
    imageFolder = "/home/pi/checklist-images"
    statusFolder = "/home/pi/checklist-status"

    remvoveFilesInFolder(resultsFolder)
    remvoveFilesInFolder(imageFolder)
    remvoveFilesInFolder(statusFolder)

while True:
 
    if queueJob.qsize() != 0:

        if debugGoThru:
            if sys.version_info[0] == 3:
                x = input("enter to cont")
            else:
                x = raw_input("enter to cont") 

       
        if algoVer == 1 and not fpNow: 
            logging.info("[FP] algo=1, fp=no")
            for resultFile in os.listdir(resultsFolder):
                if (
                    resultFile.endswith(".result")
                    and resultFile not in bt.proResults
                ):
                
                    finalFileName = resultFile.split(".")[0]
                    imgRecPacket = (
                        "R:B:map:absolute:"
                        + finalFileName.split("-")[0]
                        + ":"
                        + finalFileName.split("-")[1]
                        + ":"
                        + finalFileName.split("-")[2]
                    )
                    queueJob.put(imgRecPacket)
                    bt.proResults.append(resultFile)
                    logging.info(
                        "[raspberry] image reg packet is in queue - %s"
                        % imgRecPacket
                    )

    
        elif algoVer == 2 and not fpNow: 
            proPacket = "R:D:read_initial_processed"
            queueJob.put(proPacket)

        ph.handle(queueJob.get())
        queueJob.task_done()

for t in jobList:
    t.join()

