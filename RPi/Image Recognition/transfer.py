import shutil
import os
from time import sleep
import paramiko
from paramiko.ssh_exception import SSHException


rpiServer = "192.168.9.9"
rpiUsername = "pi"
rpiPassword = "raspberry"

rpiImageDirectory = "/home/pi/images"
imageDirectory = "imageFolder"
results_dir = "resultsFolder"
backupDirectory = "backupImageFolder"

createFolders = [imageDirectory, resultsDirectory, backupDirectory]

for fldr in createFolders:
    if not os.path.exists(fldr):
        os.makedirs(fldr)


#connect to rpi 
ssh = paramiko.SSHClient() 

ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(rpiServer, rpiUsername=rpiUsername, rpiPassword=rpiPassword)
sftp = ssh.open_sftp()


print("\n Connected to RPI\n")

# function to delete all files in a folder
def deleteFiles(folderpath):
    if os.path.exists(folderpath):
        for filepath in os.listdir(folderpath):
            os.remove(os.path.join(folderpath, filepath))
            
#delete all files in results for each new run            
deleteFiles(resultsDirectory)

while True:
    try

        # remove from computer if not found in RPI
        for filename in os.listdir(imageDirectory):
            if filename.lower().endswith(".jpg") and filename not in sftp.listdir(rpiImageDirectory):
                localFilePath = os.path.join(imageDirectory, filename)
                os.remove(localFilePath)
                print("Deleting " + filename + "...")

        # transfer images from RPI to the computer 
        for filename in sftp.listdir(rpiImageDirectory):
            if filename.lower().endswith(".jpg") and filename not in os.listdir(imageDirectory):
               
                remotepath = rpiImageDirectory + "/" + filename
             
                #rename the image files 
                localFilePath = os.path.join(imageDirectory, filename + "-temporary")
                sftp.get(remotepath, localFilePath, callback=None)
                print("Transfering " + filename + "...")
                newLocalFilePath = localFilePath.replace("-temporary","")
                os.rename(localFilePath, newLocalFilePath)
                backupFilePath = os.path.join(backupDirectory, filename)
                shutil.copy2(newLocalFilePath, backupFilePath)
                
        sleep(1.5)

#catch other errors
    except SSHException as e:
        print(str(e))
        sleep(1)
        print("Attempting to retry")

        ssh = paramiko.SSHClient() 
        ssh.load_host_keys(os.path.expanduser(os.path.join("~", ".ssh", "known_hosts")))
        ssh.connect(rpiServer, rpiUsername=rpiUsername, rpiPassword=rpiPassword)
        sftp = ssh.open_sftp()
        print("\n Connected to RPI\n")        

    except Exception as e:
        print(str(e))
        sleep(1)
        print("Error detected, skipping the transfer of file\n")

print
