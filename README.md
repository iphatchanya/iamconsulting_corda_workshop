

# Example CorDapp

Welcome to the example CorDapp. This CorDapp is documented [here](http://docs.corda.net/tutorial-cordapp.html).


# Let's setup!!!
1. Java jdk 1.8 https://www.oracle.com/technetwork/java/javase/downloads/index.html
 
2. IntelliJ (Community version just fine) https://www.jetbrains.com/idea/download
 
3. Git https://git-scm.com/
    
4. Gradle 4.10.3
https://gradle.org/releases/ or
https://gradle.org/next-steps/?version=4.10.3&format=bin

5. H2 console 
http://www.h2database.com/html/download.html

# Open project

1. Using Git for clone project from https://gitlab.com/iam_corda/iam-cordapp-example.git.
**Don’t use IntelliJ for clone project.

2. Using IntelliJ for OPEN project “iam-cordapp-example” folder.
**OPEN not IMPORT.

3. Import gradle
![Image description](https://gitlab.com/iam_corda/iam-cordapp-example/raw/7f0a78f5351e6c7cd9a3ee4b7afc317cbe54ff52/images/import_gradle.png)

4. Choose
    1. Build and Run using "intelliJ"
    2. Use Gradle from "gradle-wrapper.properties file"
    3. Choose JDK 1.8 path
  
  ****For window
  Select “Gradle user home” as “C:/g”**
  
![Image description](https://gitlab.com/iam_corda/iam-cordapp-example/raw/7f0a78f5351e6c7cd9a3ee4b7afc317cbe54ff52/images/setup_gradle.png)

5. Test run
  
  by execute “Example Cordapp - Kotlin“

![Image description](https://gitlab.com/iam_corda/iam-cordapp-example/raw/107a6c6795cf0e94e9842c872d662045963063b1/images/test_run.png)


# Frequently Problem
1. "Missing the '-javaagent' JVM argument" when run test or start NodeDriver
   - add "-ea -javaagent:../lib/quasar.jar" at vm option in run configuration and run again


2. Invalid certificate
    - Change region format to United State and re-run again

Other problem please ask staff, we don't bite ;)