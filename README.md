OWASP Seraphimdroid
===================

### Mission:

To create, as a community, an open platform for education and protection of Android users against privacy and security threats.

### Project Description:

OWASP Seraphimdroid is a privacy and security protection app for Android devices. It enables users to protect their devices against malicious software (viruses, trojans, worms, etc.), phishing SMS, MMS messages, execution of dangerous USSD codes, theft and loss. Also, it enables the user to protect their privacy and to control the usage of applications and services via various kinds of locks.

This Project has two main aims:
* To protect user's privacy and secure the device against malicious features that may cost user money
* To educate user about threats and risks for their privacy, privacy of their data and security of their device.

OWASP Seraphimdroid is free and open source community driven project, supported by OWASP (Open Web Application Security Project) Foundation. Everyone is welcome to participate. If you are interested, Join our [mailing list](https://lists.owasp.org/mailman/listinfo/owasp_seraphimdroid_project).

Feel free to contact the project lead if you want to participate or contribute to the project.
More info available on the [website](https://www.owasp.org/index.php/OWASP_SeraphimDroid_Project).

The App is available on
[Google Play Store](https://play.google.com/store/apps/details?id=org.owasp.seraphimdroid).

#### Setting up Development Environment for Seraphimdroid:

###### Requirements:
  1. [Android Studio](https://developer.android.com/studio/intro/index.html)
  2. Android API v21
  3. Gradle Distribution (Optional)
  4. x86 Emulator or a Hardware Device for Testing (Optional)

###### Additional SDK Requirements:
* Google Repository
* Android Support Repository
* Android Support Library
* Android SDK Tools *(Included)*
* Android SDK Build-Tools *(Included)*
* Android SDK Platform-Tools *(Included)*
* HAXM *(Optional if Using x86 Emulator)*

*All of these Libraries can be installed using the Integrated Android SDK Manager available in Android Studio Detailed Info on this [wiki]().*

After Setting up Studio, Follow these:
  1. Clone owasp-seraphimdroid from Github to your desired location using :
            git clone https://github.com/nikolamilosevic86/owasp-seraphimdroid.git
  2. Click on Open an Existing Android Studio Project or Go to File -> Open.
  3. Select *'Seraphimdroid'* folder from the Cloned Repository. *(The project is not on root.)*
  4. As Soon as the Gradle Build Finishes, You are Ready with Your Project.
  5. You need to configure the Maps API for GPS functions to work. [Instructions  available here.](https://github.com/nikolamilosevic86/owasp-seraphimdroid/wiki/Setup-Maps-API-for-Seraphimdroid)
  6. Click the Run button & Start digging.

  *Detailed Info Available [here]().*

### Referencing
You may reference the following paper:
Milosevic, Nikola, Ali Dehghantanha, and Kim-Kwang Raymond Choo. ["Machine learning aided Android malware classification."](http://www.sciencedirect.com/science/article/pii/S0045790617303087) Computers & Electrical Engineering (2017).
##### Important note: Project has been ported for Android Studio, so the instructions for old Eclipse Setup are available on this [wiki](https://github.com/nikolamilosevic86/owasp-seraphimdroid/wiki/How-to-set-up-Eclipse-for-project).

### Note:
This Project is Under Active Development and Stable build is available on the [Master](https://github.com/nikolamilosevic86/owasp-seraphimdroid/tree/master).
This [build](https://github.com/nikolamilosevic86/owasp-seraphimdroid/tree/gsoc) will be kept free of errors mostly, but may still face some minor glitches.
The Knowledge Base is currently under development as a sister project and the results are fetched from the [API](https://github.com/addiittya2006/owasp-educate) deployed at [Openshift](http://educate-seraphimdroid.rhcloud.com).
All the Information related to that is available on [this Wiki](https://github.com/addiittya2006/owasp-educate/wiki).
