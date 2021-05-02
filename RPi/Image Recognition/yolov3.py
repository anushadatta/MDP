import os
import paramiko
from paramiko.ssh_exception import SSHException

from multiprocessing import Queue, Process

import shutil
from time import sleep
import cv2
import traceback
from collections import Counter

import annotated_gridlines

import annotated_areas

from imageai.Detection.Custom import CustomObjectDetection

from pathlib import Path


from datetime import datetime
import logging
logs_dir = "logs"
if not os.path.exists(logs_dir):
    os.makedirs(logs_dir)

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(message)s',
              
                    filename=os.path.join(logs_dir, datetime.now().strftime("%Y%m%d-%H%M%S") + '.log'),
                    filemode='w')

console = logging.StreamHandler()
console.setLevel(logging.INFO)
console.setFormatter(logging.Formatter('%(asctime)s %(message)s'))
logging.getLogger('').addHandler(console)


#set true if actual run
actualRun = True 

sendRPI = True
#percentage for min
imageMinProb = 55
#percentage for max
imageMaxProb = 92


imageMinInterAreaPerc = 65
borderTopBtm = True

#link image to their ids
imageIDToActualImage = {
    "up": "1",
    "down": "2",
    "right": "3",
    "left": "4",
    "go": "5",
    "6": "6",
    "7": "7",
    "8": "8",
    "9": "9",
    "0": "10",
    "v": "11",
    "w": "12",
    "x": "13",
    "y": "14",
    "z": "15"
}

#RPI folder details
rpiResultFolder = "/home/pi/rresults"
pcResultsFolder = "resultsFolder"

if actualRun:
    imageFolderIn = "imageInFolder"
    imageFolderOut = "imageOutFolder"
    spareImageFolder = "imageSpareFolder"
    borderImageFolder = "imageBorderFolder"
    imageStorage = "imageStorageFolder"
    imageConcatStorage = "imageConcatFolder"
else:
    mainFolder = ""
    imageFolderIn = os.path.join(mainFolder, "imageInFolder")
    imageFolderOut = os.path.join(mainFolder, "imageOutFolder")
    spareImageFolder = os.path.join(mainFolder, "imageSpareFolder")
    borderImageFolder = os.path.join(mainFolder, "imageBorderFolder")
    imageStorage = os.path.join(mainFolder, "imageStorageFolder")
    imageConcatStorage = os.path.join(mainFolder, "imageConcatFolder")

createFolders = [imageFolderIn, imageFolderOut, spareImageFolder, borderImageFolder, imageStorage, imageConcatStorage]

for fldr in createFolders:
    if not os.path.exists(fldr):
        os.makedirs(fldr)

#function to join ideal iamges
def joinImages():
    imageIDConcatted = []
    imageFileConcat = []
    for imageFileNameOut in os.listdir(imageFolderOut):
        if "(" not in imageFileNameOut:
 
            fileNameTemporary = imageFileNameOut.split("_")[2].split(".")[0]
            x, y, imageID = fileNameTemporary.split("-")[0], fileNameTemporary.split("-")[1], fileNameTemporary.split("-")[2]
            if imageID not in imageIDConcatted:
                imageIDConcatted.append(imageID)
                imageFileConcat.append(os.path.join(imageFolderOut, imageFileNameOut))
    imageNeedToConcat = []
    for files in imageFileConcat:
        image = cv2.imread(files)
        imageNeedToConcat.append(image)

    print("len(imageNeedToConcat)=%s" % len(imageNeedToConcat))
    if len(imageNeedToConcat) > 1:
        concatted = cv2.vconcat(imageNeedToConcat)
        cv2.imwrite("concatted.jpg", concatted)
        logging.info("[cv] more than 1 image was found, thus joining together to concatted.jpg")
        shutil.copy2("concatted.jpg", os.path.join(imageConcatStorage, datetime.now().strftime("%Y%m%d-%H%M%S") + "_concatted.jpg"))
    #if there is only 1 image    
    elif len(imageNeedToConcat) == 1:
        logging.info("[cv] theres 1 image")
        cv2.imwrite("concatted.jpg", imageNeedToConcat[0])
        shutil.copy2("concatted.jpg", os.path.join(imageConcatStorage, datetime.now().strftime("%Y%m%d-%H%M%S") + "_concatted.jpg"))
    elif len(imageNeedToConcat) == 0:
        logging.info("[cv] nothing was found so no image to join")

def returnXYOff(bestBB):
    logging.info("bestBB=%s" % (bestBB))
    x1, y1, x2, y2 = bestBB[0], bestBB[1], bestBB[2], bestBB[3]
    logging.info("x1=%s, y1=%s, x2=%s, y2=%s" % (x1, y1, x2, y2))
    xmin, ymin, xmax, ymax = x1, y1, x2, y2
    logging.info("xmin=%s, ymin=%s, xmax=%s, ymax=%s" % (xmin, ymin, xmax, ymax))

    xOff, yOff = annotated_gridlines.get_xOff(xmin,ymin,xmax,ymax), annotated_areas.get_yOff(xmin,ymin,xmax,ymax)
    if xOff is None and yOff is None:
        return -999, -999
    elif xOff is None:
        return -999, yOff
    elif yOff is None:
        return xOff, -999
    else:
        return xOff, yOff

def returnResult(absX, absY, imageID, xRelOff, yRelOff, robotCurrentDirection, robotCurrentX, robotCurrentY, sendRPI):
    result_filename = str(absX) + "_" + str(absY) + "_" + str(imageID) + "." + str(xRelOff) + "_" + str(yRelOff) + "_" + str(robotCurrentDirection) + "." + str(robotCurrentX) + "_" + str(robotCurrentY) + ".result"
    local_result_filepath = os.path.join(pcResultsFolder, result_filename)
    open(local_result_filepath,'a').close()

    
    logging.info("results sent to %s" % local_result_filepath)

def exportImageAbs(image, filePathOut, filePathStorage, bestBB, closestName, robotCurrentX, robotCurrentY, robotCurrentDirection):
    x1, y1, x2, y2 = bestBB[0], bestBB[1], bestBB[2], bestBB[3]

    cv2.rectangle(image, (x1, y1), (x2, y2), (0,0,255), 2)
    cv2.putText(image, "%s" % closestName, (x1,y2), cv2Font, 0.5, (11,255,255), 2, cv2.LINE_AA)
    
    xRelOff, yRelOff = returnXYOff(bestBB)
    xRelOff, yRelOff = int(xRelOff), int(yRelOff)

    logging.info("robotCurrentX=%s, robotCurrentY=%s, robotCurrentDirection=%s" % (robotCurrentX, robotCurrentY, robotCurrentDirection))
    robotCurrentX, robotCurrentY, robotCurrentDirection = int(robotCurrentX), int(robotCurrentY), int(robotCurrentDirection)

    if robotCurrentDirection == 0:
        #0 is up
        absX = robotCurrentX + xRelOff     
        absY = robotCurrentY - yRelOff - 1
    
        pass
    elif robotCurrentDirection == 1:
        #1 is right
        absX = robotCurrentX + yRelOff + 2     
        absY = robotCurrentY + xRelOff  
    
        pass
    elif robotCurrentDirection == 2:
        #2 is down
        absX = robotCurrentX - xRelOff   
        absY = robotCurrentY + yRelOff + 1
     
        pass
    elif robotCurrentDirection == 3:
        #3 is left
        absX = robotCurrentX - yRelOff - 2     
        absY = robotCurrentY - xRelOff - 1
        pass
    else:
        logging.error("invaild robotCurrentDirection recevied=%s" % robotCurrentDirection)
        return

    imageID = imageIDToActualImage[closestName]
 
    filePathOut = "-".join(filePathOut.split("-")[:-1]) + "_" + str(absX) + "-" + str(absY) + "-" + str(imageID) + "." + filePathOut.split(".")[-1]
    logging.info("[FINAL] Saving %s prediction=%s and absX=%s,absY=%s" % (filePathOut, closestName, absX, absY))

    cv2.imwrite(filePathOut, image)
    cv2.imwrite(filePathStorage, image)

    if actualRun:
        sendRPI = True
    else:
        sendRPI = False

    returnResult(absX, absY, imageID, xRelOff, yRelOff, robotCurrentDirection, robotCurrentX, robotCurrentY, sendRPI=sendRPI)

def returnBestImage(image, filePathOut, bestBB, closestName):
    # Draw using cv2
    x1, y1, x2, y2 = bestBB[0], bestBB[1], bestBB[2], bestBB[3]

    cv2.rectangle(image, (x1, y1), (x2, y2), (0,0,255), 2) # BGR
    cv2.putText(image, "%s;h%s;w%s;a%s" % (closestName,y2-y1,x2-x1,(y2-y1)*(x2-x1)), (x1,y2), cv2Font, 0.5, (11,255,255), 2, cv2.LINE_AA)
 
    xOff, yOff = returnXYOff(bestBB)
    filePathOut = "".join(filePathOut.split(".")[:-1]) + "_" + closestName + "_" + str(xOff) + "_" + str(yOff) + "." + filePathOut.split(".")[-1]
    logging.info("[last] Saving %s prediction=%s and xOff=%s,yOff=%s" % (filePathOut, closestName, xOff, yOff))


    cv2.imwrite(filePathOut, image)

def removeFile(filepath):
    if os.path.exists(filepath):
        os.remove(filepath)

def removeAllFolderFiles(folderpath):
    if os.path.exists(folderpath):
        for filepath in os.listdir(folderpath):
            os.remove(os.path.join(folderpath, filepath))

def exportedFiles(fileNameIn, imageFolderOut):
   
    trimmed_filename = "".join(fileNameIn.split(".")[:-1]).split("-")[0]
  
    for f in os.listdir(imageFolderOut):
     
        if f.startswith(trimmed_filename):
          
            return True

    return False


cv2Font = cv2.font_HERSHEY_SIMPLEX


modelFilePath = "detection_model-ex-144--loss-0006.693.h5"



removeAllFolderFiles(spareImageFolder)
if actualRun:
    removeAllFolderFiles(imageFolderOut)
    removeFile("concatted.jpg")

logging.info("\n program loaded \n")

def detectImage(queue):
    
    detector = CustomObjectDetection()
    detector.setModelTypeAsYOLOv3()
    detector.setModelPath(modelFilePath)
    detector.setJsonPath("detection_config.json")
    detector.loadModel()

    while True:
        
        if queue.qsize() > 0:

            try:
                fileNameIn = queue.get()
                logging.info("[detectImage] Processing %s" % fileNameIn)

                filePathIn = os.path.join(imageFolderIn, fileNameIn)
                filePathOut = os.path.join(imageFolderOut, fileNameIn)
                filePathStorage = os.path.join(imageStorage, fileNameIn)
                logging.info(filePathOut)
                filePathSpare = os.path.join(spareImageFolder, fileNameIn)
                filepathBorder = os.path.join(borderImageFolder, fileNameIn)
                
                logging.info("\n---------------------------------------")
                logging.info("moving to next image")
                if borderTopBtm:
                    logging.info("filepathBorder: " + filepathBorder)
                else:
                    logging.info("filePathIn: " + filePathIn)
                logging.info("---------------------------------------")

                if borderTopBtm:
                    borderImage = cv2.imread(filePathIn)
                    #border bottom
                    cv2.rectangle(borderImage, (0,525), (1280,720), (0,0,0), -1) 
                    ##border top
                    cv2.rectangle(borderImage, (0,0), (1280,140), (0,0,0), -1) 
                    cv2.imwrite(filepathBorder, borderImage)

                
                if borderTopBtm:
                    logging.info("[YOLO] Processing %s..." % filepathBorder)
                    detections = detector.detectObjectsFromImage(
                        imageIn=filepathBorder, 
                        imageOut=filePathSpare,
                        minProbPerc=imageMinProb)
                else:
                    logging.info("[YOLO] Processing %s..." % filePathIn)
                    detections = detector.detectObjectsFromImage(
                        imageIn=filePathIn, 
                        imageOut=filePathSpare,
                        minProbPerc=imageMinProb)

                probPercBest = 0
                closestName = None
                bestBB = None

                img = cv2.imread(filePathIn)

                for detection in detections:
                    name = detection["name"]
                    probPerc = detection["probPerc"]
                    bbPoints = detection["bbPoints"] 
                    logging.info("name=%s, probPerc=%s, bbPoints=%s" % (name, probPerc, bbPoints))

                    if probPerc > probPercBest:
                        probPercBest = probPerc
                        closestName = name
                        bestBB = bbPoints

        
                if bestBB is None:
                    logging.info("Nothing is detected, skipping image...")
                    shutil.copy2(filePathIn, filePathOut)
                    shutil.copy2(filePathIn, filePathStorage)
                    continue

                
                if probPercBest >= imageMaxProb:
                    logging.info("Model is sure that closestName is " + closestName )
                    logging.info("fileNameIn: %s" % fileNameIn)
                    logging.info("fileNameIn.split('_'): %s" % fileNameIn.split("_"))
                  
                    robotCurrentX, robotCurrentY, robotCurrentDirection = fileNameIn.split("-")[1].split("_")[0], fileNameIn.split("-")[1].split("_")[1], fileNameIn.split("-")[1].split("_")[2].split('.')[0]
                    exportImageAbs(img, filePathOut, filePathStorage, bestBB, closestName, robotCurrentX, robotCurrentY, robotCurrentDirection)
                    continue
                else:                
                    logging.warn("Model is not confident")
                    shutil.copy2(filePathIn, filePathOut)
                    shutil.copy2(filePathIn, filePathStorage)
                    continue   

            except Exception as e:
                logging.error("\nException %s\n" % e)
                traceback.print_exc()

                if "!_img.empty()" in str(e):
                    logging.error("Model does not detect images ")
                    if os.path.exists(filePathIn):
                        shutil.copy2(filePathIn, filePathOut)
                        shutil.copy2(filePathIn, filePathStorage)
                    continue

def writeToMP(queue):

    sentToQueue = []

    while True:
        try:
            for fileNameIn in os.listdir(imageFolderIn):
                
                if fileNameIn in sentToQueue or not fileNameIn.endswith(".jpg"): 
                    continue
                
                joinImages()

                queue.put(fileNameIn)
                sentToQueue.append(fileNameIn)
                logging.info("[writeToMP] Sent to queue - %s" % fileNameIn)

   

        except Exception as e:
            logging.error("\nException %s\n" % e)
            traceback.print_exc()
            continue

def writeToResult():

    sentToResultQueue = []

    rpiServer = "192.168.9.9"
    rpiUsername = "pi"
    rpiPassword = "raspberry"

    ssh = paramiko.SSHClient() 

    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(rpiServer, rpiUsername=rpiUsername, rpiPassword=rpiPassword)
    sftp = ssh.open_sftp()
    logging.info("SFTP connected!")

    while True:
        try:
            for checklist_result in os.listdir(pcResultsFolder):
                
                if checklist_result in sentToResultQueue: 
                    continue
                
                local_result_filepath = os.path.join(pcResultsFolder, checklist_result)
                remote_result_filepath = rpiResultFolder + "/" + checklist_result

                if sendRPI:
                    sftp.put(local_result_filepath, remote_result_filepath)

                sentToResultQueue.append(checklist_result)
                logging.info("[writeToResult] Sent result - %s" % checklist_result)

            sleep(0.3)

        except Exception as e:
            logging.error("[writeToResult] Exception %s" % e)
            traceback.print_exc()
            continue

if __name__ == '__main__':

    queue = Queue()
    result_queue = Queue()
    sizeOfPool = 3

    master = Process(target=writeToMP, args=(queue,))
    if actualRun:
        writer = Process(target=writeToResult)

    processes = [Process(target=detectImage, args=(queue,)) for _ in range(sizeOfPool)]

    master.start()
    if actualRun:
        writer.start()

    for process in processes:
        process.start()

    for process in processes:
        process.join()
