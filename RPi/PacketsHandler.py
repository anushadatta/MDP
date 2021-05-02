import logging
import struct
import os
from time import sleep




class PacketHandler:

    handlers = {}
    camera_mode = False

    def __init__(self):
    
        self.logger = logging.getLogger(self.__class__.__name__)

  
    def regHandler(self, instance):
        packetID = instance.getPacketHeader()
        if packetID not in self.handlers:
            self.handlers[packetID] = instance
        else:
            self.logger.error(
                "select another packetID"
            )
    

    def unRegHandler(self, packetID):
        try:
            del self.handlers[packetID]
        except KeyError:
            self.logger.error("cannot find handler")

    def convertToName(self, header):
        if header == "A":
            return "ARDUINO"
     
        elif header == "R":
            return "RPI"
            
        elif header == "B":
            return "ANDROID"
        elif header == "P":
            return "PC"

    def handle(self, packet):
        packet = str(packet)
       
        if "P:" in packet.strip():
         
            return

        self.logger.info("[packet] %s" % packet)
    
        separateData = packet.split(":")
     

        if len(separateData) > 1:
            receivedFrom = separateData[0]

            packetID = separateData[1]

            if packetID in self.handlers:

             

                if not packet.startswith(
                    "P:A:set:startposition"
                ):  
                    lo = (
                        "[message]["
                        + self.convertToName(receivedFrom)
                        + "->"
                        + self.convertToName(packetID)
                        + "]:"
                        + packet[2:]
                    )
            
                self.handlers[packetID].handle(packet[2:] + "\n")
        else:
            self.logger.error("[error][handler]:" + packet)
