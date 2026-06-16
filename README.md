# Flash-Detection-Software-for-Linux
A fork of the Flash Detection Software of the National Observatory of Athens, made running and compilable for linux. 

## About
The Flash Detection Software is a software of the National Observatory of Athens written for the NEOLIOTA project to monitor the moon for impact flashes. The software consists of two parts: A firecapture plugin and the standalone detection software. 

More information and the original sources you can find here: https://kryoneri.astro.noa.gr/en/flash-detection-software/

Unfurtunately the original software was not able to run under Linux OS. This fork does.

## Compilation
To compile the software use gradle:

```
sudo apt install default-jre gradle
gradle clean build
```

## Running
To run the compiled JAR use:

```
java --module-path build/deps/ --add-modules javafx.controls,javafx.fxml,commons.math3 --enable-native-access=javafx.graphics -jar build/libs/ImpactDetection.jar
```
The required modules are automatically downloaded and saved in build/deps/ directory during compilation.


## Credits
This software was developed by the National Observatory of Athens / Kryoneri Observatory.
https://kryoneri.astro.noa.gr/en/flash-detection-software/
