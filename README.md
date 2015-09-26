owasp-seraphimdroid
===================

##Mission:

To create, as a community, an open platform for education and protection of Android users against privacy and security threats.

##Project description

OWASP Seraphimdroid is a privacy and security protection app for Android devices. It enables users to protect their devices against malicious software (viruses, trojans, worms, etc.), phishing SMS, MMS messages, execution of dangerous USSD codes, theft and loosing. Also, it enables user to protect their privacy and to control the usage of applications and services via various kinds of locks.

OWASP Seraphimdroid has two aims:
- To protect user's privacy and secure the device against malicious features that may cost user money
- To educate user about threats and risks for their privacy, privacy of their data and security of their device.

OWASP Seraphimdroid is free and open source community driven project, supported by OWASP (Open Web Application Security Project) Foundation. Everyone is welcome to participate. If you are interested, please join our mailing list (https://lists.owasp.org/mailman/listinfo/owasp_seraphimdroid_project)

Feel free to contact project leader if you want to participate or contribute to the project 

website: https://www.owasp.org/index.php/OWASP_SeraphimDroid_Project

Google play: https://play.google.com/store/apps/details?id=org.owasp.seraphimdroid


##How to set up Eclipse for project.
###Important note: Project was ported for Android Studio, so the following instruction does not stand anymore. New instruction will be available soon
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



