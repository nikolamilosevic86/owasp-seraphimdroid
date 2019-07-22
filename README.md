# [Project summary](https://docs.google.com/document/d/1WzNZed2Et8eRn7xLYvWI_Wb-FUqpKK88eBp7XNqt20I/edit?usp=sharing) for GSoC 2019 
**Project name**: OWASP Seraphimdroid 

**Mentor**: Nikola Milosevic (nikola.milosevic@owasp.org)


# Project Description

OWASP Seraphimdroid has previously applied a system, based on permissions, which is able to distinguish malicious apps from non-malicious. This time, my contribution is ultilise the data usage information for Seraphimdroid to detect malicious behavior.

# Work Flow

This project developing periods could be divided into 3 parts: data collection, data analysis and function integration.
  - Data collection (log)
    - Android development preparation and Feature selection
    - Merge open source applications
      - Another Monitor
      - Network Monitor
  - Data analysis (modelling)
      - Explore different models for data analysis
        - Supervised learning
            - KNN, Random forest, SVM, Neural network, CNN
        - Anomaly detection
            - LSTM, Autoencoder, PCA
      - Labelling, data collection and malware testing
  - Function integration: 
    - Integrate Autoencoder and LSTM methods into android application
    - Warning notification

# What can we collect from Seraphimdroid?

Currently, Seraphimdroid is able to collect different device usage information thanks to these two open source applications: AnotherMonitor and NetworkMonitor. Another Monitor helped us collect cpu usage, memory usage and cached information, while Network Monitor provided functions to obtain system battery status and other network information. 

Example: What data could AnotherMonitor collect?


Device usage|
Totoal CPU |
Seraphi’s CPU|
Totoal Memory|
Seraphi’s Memory|
Cached|
---|---|---|---|---|---
Android 7|✔️|✔️|✔️|✔️|✔️
Androdi 8+|❌|❌|✔️|✔️|✔️


# What can we learn from collected data?

Compared with benign, malware’s behavior would cause the abnormal device usage, for example, cpu used rapidly, battery ran out quicker. Since Seraphimdroid is able to record these information, it is possible to detect malicious behaviors based on previous behaviors. 

# Techinical details

Our anomaly detection techniques are the implementation of Autoencoder and LSTM. The main idea is to predict the data at current stage, and by calculating the distance between our prediction and the collected data and comparing with our threshold, we are able to decide whether this behavior is abnormal or not. 

Autoencoder is a special neural network that encodes the real data into a small dimension, and then decodes it back. After this operation, it could usually recover well for normal data. However, if it is a malicious behavior, it is possible to obtain a large distance between model prediction and real data.

(Figure: Autoencoder architechture)
Similarly, our LSTM methods is based on previous M timelines data to predict the M+1 data. By calculating the distance between prediction and real data and comparing with threshold, it could detect the anomaly.

# Future work & other effort

For malware classification part, I still explored other machine learning techniques, like CNN, Random Forest, SVM, etc. It could performs well for our naive labelling (when malware runs, say it is abnormal). However, since we don’t have the source code of malware, it is impossible to know when the malicious behavior happen exactly. It worth to say that our model for predicting if the user plays youtube videos perform quite well, which shows that if we label correctly our model could learn better. So the future work could be find a proper way to label the collected data. Once we did that, I believe it could detect the anomaly more precisely. 

# Malware Testing

During the data collecting period, I have tested 10 different malware as well as 10 benign which can be found here. By labelling the whole period as malware running period and benign running period, our random forest is able to classify the malicious data out with max 99.69% accuracy and 91% F1 score. (details: pre1, pre2, pre3) In the future, the improvement should be focus on fixing labelling issue. Luckily, in our implementation, it could be detected precisely for SMS malware which sends message out and could be seen in the user interface.

# Where can you find the code?
Our code can be found in these two github repos:
  - Main repo
  - Private repo



