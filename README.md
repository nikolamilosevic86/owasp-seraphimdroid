# Project Name
OWASP Seraphimdroid

# Project Expectation
Develop an advanced system/model to detect the anomaly usage and distinguish different types of malicious apps by applying machine learning techniques.

# Project Description
OWASP Seraphimdroid has previously applied a system, based on permissions, which is able to distinguish malicious apps from non-malicious. But it still has some false positive, like foodpanda app. In order to improve OWASP Seraphimdroid’s  performance, we would like to learn from other outputs (like network, CPU, buttery and memory usage, system call logs) can monitor about application whether it can be malicious.  

# Project Overview
My plan mainly consists of three stages: preparation, application designing and modelling, comparison.

In the preparation stage, I will firstly work on data collection and Android malware behaviours research. Now, I have found two datasets contain the network traffic data for both malware and benign applications, which might be useful in our project. Also, there is an open source app named AnotherMonitor, through which we are able to get the CPU and memory usage information. However, it should be noticed that in Android 7+ Google has made undocumented changes and has significantly restricted access to the proc file system. This means it is hard to obtain more information for new Android versions. As for Android malware behaviours research, I will combine with reading paper and analysing malware data to obtain a better intuition.

In the application designing and modelling stage, since Seraphimdroid has the ability to distinguish some malicious apps already, my plan can be divided into two main parts:

The first part is to collect user’s dynamic CPU, memory, etc. usage data, through which we are able to build a model for anomaly detection to calculate the probability of the situation occurring given the normal data. If the probability is lower than the threshold we defined, the system will give the user a warning message. Here is a diagram of my thought.

(Diagram: zoom in, the installed application here means the test application)

The second part is to train the existing dynamic dataset do malware classification. After the unknown application installed, we can do the same job to collect smartphone usage information (for this application). After collecting a certain period, two days, for example, we are able to train the machine to distinguish this application from malware and benign. In addition, rather than only malware and benign classification, it can contain more clusters for different types of malware apps. If the malware data is limited, SMOTE algorithm might be a feasible choice for oversampling.

As for system log files, I have a rough idea either use topic modelling methods to extract the abnormal behaviours or inductive logic programming to build a rule for classification, like behaviour 1, behaviour 2 -> malware behaviour.

Lastly, it is necessary to do a comparison to evaluate our model. For the anomaly detection part, choosing a suitable model distributions and thresholds might be the key to improve. I will compare different models and thresholds and try to visualise the results. For the classification part, ideally, Deep Learning can perform well given a large training set. It is worth to test the previous false positive apps and see how the system improved.
