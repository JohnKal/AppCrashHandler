# AppCrashHandlerLibrary

[![API](https://img.shields.io/badge/API-27%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=27)
![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)
<a href="https://gr.linkedin.com/pub/giannis-kalimeris/33/76a/240">
 <img src="https://static.licdn.com/scds/common/u/img/webpromo/btn_viewmy_160x33.png" width="160" height="33" border="0" alt="View Giannis Kalimeris's profile on LinkedIn">
</a>

This library provides a easy and simple way for developers to catch exceptions.
You put it on your project and you can catch an exception with a simple material dialog.
This library provides:

1. A material dialog with message for the user
2. Send report with email to developer
3. Append to mail the exception log
4. Append to mail a screenshot of the app at the moment of crash.
5. Customize the dialog with your own title, message, colors, etc.


## Download

### Gradle

Add it in your root build.gradle at the end of repositories:
```
allprojects {
        repositories {
                ...
                maven { url 'https://jitpack.io' }
        }
}
```

Step 2. Add the dependency

```
dependencies {
         implementation 'com.github.JohnKal:AppCrashHandler:v1.0'
}
```

### Maven
```
<repositories>
        <repository>
                <id>jitpack.io</id>
                <url>https://jitpack.io</url>
        </repository>
</repositories>
```

Step 2. Add the dependency
```
<dependency>
          <groupId>com.github.JohnKal</groupId>
          <artifactId>AppCrashHandler</artifactId>
          <version>v1.0</version>
</dependency>
```

## Usage

You put this in your base activity to catch all exceptions. You can see the example project that exists with the library.
```java
import com.github.johnkal.appcrashhandlerlibrary.AppCrashHandler;

Thread.setDefaultUncaughtExceptionHandler(
                new AppCrashHandler.Builder().setActivity(this)
                                    .setTitle("Error")
                                    .setDescription("An error occured. Do you want to send report to" +
                                            " developer?")
                                    .setBackgroundColor(R.color.white)
                                    .setTitleColor(R.color.black)
                                    .setDescriptionColor(R.color.black)
                                    .setIcon(R.drawable.ic_bug_report_black_24dp)
                                    .setPositiveText(R.string.send)
                                    .setNegativeText(R.string.cancel)
                                    .setTakeScreenshot(true)
                                    .setEmailTo("jkalimer13@gmail.com")
                                    .setEmailSubject("App Crash")
                                    .create());
```

Also put this in your app build.gradle file.
```
android {
   compileOptions {
       sourceCompatibility JavaVersion.VERSION_1_8
       targetCompatibility JavaVersion.VERSION_1_8
   }
}
```

If you would like to attach screenshot of the crash app in mail you have to put the write external storage permission in your AndroidManifest.xml.
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

If you don't want the library to take screenshot of your app you have to set 
```
.setTakeScreenshot(false)
```

if the targetSdkVersion of your app is 23 or above you have to request the permission at runtime like this
```
if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
    // Permission is not granted
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
}
```

## Authors
This project was created by [John Kalimeris](https://www.linkedin.com/in/giannis-kalimeris-24076a33/).


## License

```
Copyright 2018 John Kalimeris

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
