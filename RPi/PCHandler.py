import socket
import time
import threading
from Queue import Queue
import sys
import traceback
import multiprocessing
import logging





class PCHandler(multiprocessing.Process):
    m = multiprocessing.Manager()
    threadLock = threading.Lock()
    queueHandler = m.Queue()

    def __init__(self, host, port, queueJob, header):

     

        multiprocessing.Process.__init__(self)
        self.header = header
        self.port = port
      
        self.logger = logging.getLogger(__name__)
        self.host = host
        self.queueJob = queueJob
        self.c = None
        self.daemon = True

        self.start()

    def run(self):
        t2 = threading.Thread(target=self.handleProcessor, args=(0.00001,))
        t2.start()

        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((self.host, self.port))
        s.listen(6)
        while True:
            self.logger.info("[laptop] waiting for connection")
            self.c, addr = s.accept()

            self.threadLock.acquire()
            self.logger.info(
                "[laptop] Connection from:" + str(addr[0]) + ":" + str(addr[1])
            )
            self.queueJob.put(self.header + ":B:stat:PC_Connected")

            t1 = threading.Thread(
                target=self.receiveThread, args=(self.c, self.queueJob,)
            )

            t1.start()
            t1.join()

        s.close()
        t2.join()

   

    def handleProcessor(self, delay):
        while True:
            if self.queueHandler.qsize() != 0:
                packet = self.queueHandler.get()
                self.logger.info("[laptop] " + packet)
                self.queueHandler.task_done()
                splitData = packet.split(":")

                if splitData[1] == "set":
                    if splitData[2] == "startposition":
                        self.queueJob.put(
                            self.header
                            + ":A:set:startposition:"
                            + splitData[3]
                            + ":"
                            + splitData[4]
                        )

                try:
                    if sys.version_info[0] == 3:
                        self.socketSend((packet + "\n").encode("utf8"))
                    else:
                        self.socketSend(str(packet + "\n"))
                except Exception as e:
                    self.logger.error("[laptop][error] " + str(e))
                    traceback.print_exc()

            time.sleep(delay)
            
     def returnPackHeader(self):
        return self.header

    def handle(self, packet):
        self.queueHandler.put(packet)
    
    def receiveThread(self, c, queueJob):
        while True:
            try:
                data = c.recv(1024)
                data = data.strip() 

                if sys.version_info[0] == 3 and type(data) == bytes:
                    data = data.decode("utf8")

                if not data:
                    self.threadLock.release()
                    break

                if len(data) > 0:
                    packets = data.split("$")
                    for line in packets:
                        packet = line
                   
                        queueJob.put(self.header + ":" + packet)

            except socket.error as e:
                self.logger.error(e)
                self.threadLock.release()
                break
            time.sleep(0.2)
        c.close()



    def socketSend(self, message):
        try:
            if self.c == None:
                self.logger.error("[error][laptop] sending but no device connected")
                self.queueJob.put(self.header + ":B:PC not connected")
            else:
               
                self.c.sendall(message)
        except socket.error as e:
            self.logger.error(e)

    