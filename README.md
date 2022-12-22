# AndroidSensors
> <b>Author: Nicola De Nicolais</b>

## Screen preview
<img height="500em" src="Sensors.png" title="Sensors's screen preview">

## 📍 Description
This app provides an easy way to access sensors in Android phones. The outputs of measure realized by the sensors of the app are . txt files that are saved inside the folder created at first opening of app.

## 💎 Features

### Compass
The device uses the SensorManager class using the Accelerometer (TYPE_ACCELEROMETER) and Magnetic Field (TYPE_MAGNETIC_FIELD) sensors.<br>
The magnetometer measures the magnetic field of the Earth by providing the intensity of this field along the three axes X, Y and Z in μT.<br>
The accelerometer allows you to measure the acceleration along the three axes X, Y and Z in m/s2 and these data allow you to correct the magnetometer.

### Luxmeter
The device uses the SensorManager class using the Light sensor (TYPE_LIGHT).<br>
The value is shown in lux which, unlike lumens, allows to indicate the amount of light present in an area showing the light levels in lux units.

### Accelerometer
The accelerometer allows you to measure the acceleration along the three axes X, Y and Z in m/s2 and these data allow you to correct the magnetometer.<br>
The device uses the SensorManager class using the Accelerometer sensor (TYPE_ACCELEROMETER)

### Phonometer
The device uses the AudioRecord class to capture audio from the device’s input hardware, the AudioFormat class that is used to access a number of audio and channel format configuration constants, and the MediaRecorder class that allows recording audio input.

### AR Measurements
The device uses the archon API that allows you to create 3D objects using the device’s camera to identify points within the environment for you to place the virtual object. In addition, arcore can detect flat surfaces and can perform an estimate of the average lighting of the surrounding area.<br>
The device also uses the Sceneform framework that allows you to easily view 3D objects in AR and non-AR apps using a renderer.
