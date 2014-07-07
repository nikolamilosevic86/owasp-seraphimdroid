owasp-seraphimdroid
===================

SeraphimDroid is educational application for android devices that helps users learn about risks and threats coming from other android applications. SeraphimDroid scans your devices and teaches you about risks and threats coming from application permissions. Also this project will deliver paper on android permissions, their regular use, risks and malicious use. In second version SeraphimDroid will evolve to application firewall for android devices not allowing malicious SMS or MMS to be sent, USSD codes to be executed or calls to be called without user permission and knowledge.

website: https://www.owasp.org/index.php/OWASP_SeraphimDroid_Project

How to set up Eclipse for project.
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
