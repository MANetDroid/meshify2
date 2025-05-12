# Meshify2

Meshify2 is a library designed to facilitate communication between devices using various wireless technologies such as Bluetooth, Bluetooth LE, and Wi-Fi Direct. It provides a robust framework for managing connections, sending messages, and handling device discovery.

## Setup

### Prerequisites

- Android Studio installed on your machine.
- Minimum SDK version: 24
- Target SDK version: 34

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/meshify2.git
   cd meshify2```
2. Open the project in Android Studio.

3. Sync the project with Gradle files.

4. Add the following permissions to your app's AndroidManifest.xml file:
   ```xml
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
   <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
   <uses-permission android:name="android.permission.INTERNET" />
   ```
5. Ensure the following dependencies are included in your `build.gradle.kts` file:
   ```kotlin
   dependencies {
       implementation("androidx.appcompat:appcompat:1.6.1")
       implementation("com.google.android.material:material:1.11.0")
       implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
       implementation("com.google.code.gson:gson:2.8.9")
       implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
       implementation("io.reactivex.rxjava2:rxjava:2.2.21")
   }
   ```

## Usage
Initialization
1. Initialize Meshify in your `Application` class:
   ```java
   import com.manetdroid.meshify2.api.Meshify;
   import com.manetdroid.meshify2.api.MeshifyClient;

   public class MyApp extends Application {
       @Override
       public void onCreate() {
           super.onCreate();
           Meshify.create(this, new MeshifyClient.Builder(this).setApiKey("YOUR_KEY").build());
       }
   }
   ```
2. Add the `MyApp` class to your `AndroidManifest.xml`:
   ```xml
   <application
       android:name=".MyApp"
       ... >
   </application>
   ```

Starting Meshify
Start Meshify with optional listeners for messages and connections:
```java
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.MessageListener;
import com.manetdroid.meshify2.api.ConnectionListener;

Meshify.start(new MessageListener() {
    @Override
    public void onMessageReceived(Message message) {
        // Handle received message
    }
}, new ConnectionListener() {
    @Override
    public void onConnected(Device device) {
        // Handle device connection
    }

    @Override
    public void onDisconnected(Device device) {
        // Handle device disconnection
    }
}, new Config.Builder().setAutoConnect(true).build());
```
Sending Messages
To send a message to a specific device:
```java
import com.manetdroid.meshify2.api.Message;

Message message = new Message.Builder()
    .setReceiverId("DEVICE_ID")
    .setContent(new HashMap<String, Object>() {{
        put("key", "value");
    }})
    .build();

Meshify.sendMessage(message);
```
## License
This project is licensed under the MIT License. See the LICENSE file for details.



