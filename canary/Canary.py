#########################################################################################################
#
# References:
#
# Global Variables - http://stackoverflow.com/questions/17774768/python-creating-a-shared-variable-between-threads
# isAlive() - http://stackoverflow.com/questions/23442651/check-if-the-main-thread-is-still-alive-from-another-thread
# Structure - Queue code https://www.safaribooksonline.com/library/view/python-cookbook-3rd/9781449357337/ch12s03.html
# Servos - https://software.intel.com/en-us/articles/programming-robotics-using-the-intel-xdk-nodejs-and-mraa-library
##########################################################################################################


#Author: Youcef O'Connor
#Student Number: x13114557
#Date: 27/April/2017


# This script is for controlling the robot


import dweepy, mraa, random, psutil,time,datetime, pyupm_grove as grove, pyupm_mic as upmMicrophone, IoT
import pyupm_biss0001 as upmMotion
dateString = '%d/%m/%Y %H:%M:%S'

from Queue import Queue
from threading import Thread
import threading


# The Gpio pins are number to the MRAA mapping,
# the Intel Edison uses Intel Brekout Board layout on the DFRomeo board
LEDRight = mraa.Gpio(33)                # Setting LEDRight to pin 7 Green LED
LEDLeft = mraa.Gpio(37)                 # Setting LEDLeft to pin 13 (White LED)
pir = upmMotion.BISS0001(38)            # Setting PIR motion sensor to pin 11
servoTilt = mraa.Pwm(14)                # Setting Tilt sero to pin 5
servoPan = mraa.Pwm(20)                 # Setting Pan sero to pin 3

# What to do at pin (input / Output)
LEDRight.dir(mraa.DIR_OUT)
LEDLeft.dir(mraa.DIR_OUT)
servoTilt.period_us(10000)
servoPan.period_us(10000)

runForever = True;


def listener(publisher_thread):
    print "listener thread..."
    for dweet in dweepy.listen_for_dweets_from('canary30_iot'):
        global lights_state                         #declaring global variable for sensor and actuator states
        global motion_state
        global Pan_state
        global Tilt_state

        print "Got the dweet things"

        my_dweet = dweet                            #creating a new variable my_dweet
        content = my_dweet["content"]               #my_dweet contains "content" which has the JSON dweet values
        lights_state = content["lights"]            #String lights is declared to lights_state
        Pan_state = content["pan"]                  #String pan is declared to Pan_state
        Tilt_state = content ["tilt"]               #String tilt is declared to Tilt_state

        print "Contents opened"

        # Lights
        if lights_state == "true":                  #This if statement turns on lights if lights equals to true
            LEDRight.write(1)
            LEDLeft.write(1)
            print "Right light is ON"
            lights_state = "On"


        elif lights_state == "false":               #This if statement turns off lights if lights equals to false
            print "<<<Turn off Right Light>>>"
            LEDRight.write(0)
            LEDLeft.write(0)
            print "<<<Right light is OFF>>>"
            lights_state = "Off"

        # Pan Right
        if Pan_state == "true":                     # This if statement turns the camera right if equals to true
            servoPan.enable(False);
            servoPan.pulsewidth_us(500)
            servoPan.enable(True);
            time.sleep(1)
            servoPan.enable(False);
            print "Pan turnned right"
            Pan_state = "right"

        # Pan Center
        elif Pan_state == "false":                  # This if statement turns the camera facing forward if equals to false
            servoPan.enable(False);
            servoPan.pulsewidth_us(1200)
            servoPan.enable(True);
            time.sleep(1)
            servoPan.enable(False);
            print "Pan is looking forward"
            Pan_state = "center"


        # Pan left
        if Pan_state == "left":
            servoPan.enable(False);
            servoPan.pulsewidth_us(2000)
            servoPan.enable(True);
            LEDLeft.write(1)
            time.sleep(1)
            servoPan.enable(False);
            print "Pan turned left"
            Pan_state = "left"

        elif Tilt_state == "down":
            servoTilt.enable(False);
            servoTilt.pulsewidth_us(1050)
            servoTilt.enable(True);
            time.sleep(1)
            servoTilt.enable(False);
            print "Tilted DOWN"
            Tilt_state = "up"

        # Tilt camera Up
        if Tilt_state == "true":
            servoTilt.enable(False);
            servoTilt.pulsewidth_us(650)
            servoTilt.enable(True);
            time.sleep(1)
            servoTilt.enable(False);
            print "Tilted UP"
            Tilt_state = "up"

        # Straighten camera
        elif Tilt_state == "false":
            servoTilt.enable(False);
            servoTilt.pulsewidth_us(850)
            servoTilt.enable(True);
            time.sleep(1)
            servoTilt.enable(False);
            print "Camera Level"
            Tilt_state = "straight"


        # PIR Motion Sensor
        if pir.value():                                     #The Motion sensor sends its values to canary30_iot
            print "***Motion is ON***"
            result = dweepy.dweet_for('canary30_pir', {'pir': "true"})
            print result
            dict= pir.value()

            # saves data to a local file (Canary_Backup_Data.txt) only when motion was detected
            backupFile = open('Canary_Backup_Data.txt', 'a')
            stringToWrite = str(dict)
            timeToWrite = datetime.datetime.now().strftime(dateString)
            backupFile.write(timeToWrite + " " + stringToWrite + '\n')
            backupFile.close()


        else:
            print "<<<Moton is OFF>>>"
            result = dweepy.dweet_for('canary30_pir', {'pir': "false"})
            print result
            time.sleep(2)


        if publisher_thread.is_alive():         # Activate or continue publisher
            print "Yes it is alive"
        else:
            publisher_thread.start()


def publisher():
    while runForever:  # Never changed, leave true
        time.sleep(3)
        print "publishing data"
        result = dweepy.dweet_for('canary30_iot2', {'pan': Pan_state, 'tilt': Tilt_state, 'lights': lights_state})  #Sends sensor values in JSON to my dweet thing (youcef_iot2)
        print result
        time.sleep(2)



q = Queue()

publisher_thread = Thread(target=publisher)
listener_thread = Thread(target=listener, args=(publisher_thread,))
listener_thread.start()