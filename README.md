owasp-seraphimdroid
===================

OWASP Seraphimdroid is a privacy and security protection app for Android devices. It enables users to protect their devices against malicious software (viruses, trojans, worms, etc.), phishing SMS, MMS messages, execution of dangerous USSD codes, theft and loosing. Also, it enables user to protect their privacy and to control the usage of applications and services via various kinds of locks.

OWASP Seraphimdroid has two aims:
- To protect user's privacy and secure the device against malicious features that may cost user money
- To educate user about threats and risks for their privacy, privacy of their data and security of their device.

Features:
•	Permission scanner. Permission scanner will show you the list of all installed application and the permission they are using. Also app will describe potential malicious use of certain permissions. Seraphimdroid is using machine learning in order to predict whether application might be malicious (be a virus, Trojan, worm, rootkit, etc) or not and will notify the user. 
•	Application locker. With OWASP Seraphimdroid, you may lock access to certain or to all of your application with password
•	Service locker. This feature enables user to lock usage of WiFi, mobile network and Bluetooth with a password.
•	Install lock. This feature can lock all installing and uninstalling action on your device. Great for parental control.
•	Incoming SMS blocker. This feature will scan all incoming messages and alert user if it find in the content potential phishing
•	Outgoing SMS scanner. The application will monitor outgoing SMS and alert user if the some of the application is trying to send SMS. This is the usual scenario how malware creators earn money - by sending premium SMS messages.
•	Outgoing call blocker. This feature will allow you to perform normally outgoing calls, but it will block outgoing calls performed by other installed applications. Similarly to outgoing SMSes, this is the scenario malware creators use to earn money.
•	Geo-fencing. This feature allows user to set a location range where the device should be. If the device exits the range it may set up alarm or start sending messages to the defined number with its location.
•	SIM change detector. Ask password when SIM card is changed in order to assure that the owner of the device is changing SIM card. Perfect for theft protection.
•	Remote location. If you lost your phone, you'll be able to send SMS with a defined secret code as a content and your phone will reply with the location coordinates of the device. 
•	Remote lock. Similarly, you may lock your device using a message with secret code
•	Remote wipe. If your phone is stolen, you may send a message with secret code and wipe all user data from the phone.


OWASP Seraphimdroid is an open source project, supported by OWASP (Open Web Application Security Project) Foundation. 

Feel free to contact project leader if you want to participate or contribute to the project 

website: https://www.owasp.org/index.php/OWASP_SeraphimDroid_Project

##How to set up Eclipse for project.
  Requirement
  1. Android Support Library
  2. Google Play Services

Download these libraries using android-sdk-manager.
After downloading follow these steps
  1. Go to file->import->Existing android code into workspace
  2. Select android-sdk-folder ->extras->android->support->v7
  3. Select appcompat from the list.
  4. Similarly import google-play-service from android-sdk-folder->extras->google->google_play_service->libproject
  5. Select google-play-services_lib.

Import these two projects, then add these to project library as folllows.

  1. Go to properties of the project.
  2. Then go to Android
  3. Click add button
  4. Add these projects as libraries.
  5. Build workspace
  6. Done.

Start digging.


####API Key
You need to create a debug API key for google maps to work. Follow the steps [here](https://developers.google.com/maps/documentation/android/start) to get the key.

You need to generate the SHA1 for your eclipse, to do that enter this command 

#####For windows
keytool -list -alias -keystore "C:\Documents
and Settings\Administrator\.android\debug.keystore" -storepass android -keypass
android

##### for linux
keytool -list -alias -keystore "~\.android\debug.keystore" -storepass android -keypass
android



