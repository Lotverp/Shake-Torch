# 🔦 Shake-Torch ⚡️

Turn on your **Torch** (Flashlight) instantly, just by **shaking your device**! No need to unlock the screen or find the quick settings tile. It's magic! ✨

## 🚀 Key Features

* **Shake & Light!** 💡 Activate the flashlight with a quick shake.
* **Background Operation:** Works even when your screen is off or you're using other apps. 🏃‍♂️
* **Simple & Fast:** Minimalistic design, maximum speed. 💨
* **Open Source:** Check out the source code and build it yourself! 🧑‍💻

## 🤔 Why the Weird Permissions?

You might notice that Shake-Torch asks for **Camera** and **Notification** permissions. This is **CRUCIAL** for the app's core functionality and is due to how Android manages system resources. **We do not take photos or spam you with ads!** 🙅

### 📸 CAMERA Permission (The Flashlight API)

* **Why is it needed?** On Android, the LED flash (the "torch") is technically controlled via the **Camera Hardware Interface**.
* **The Reality:** To turn the flashlight ON and OFF, the application must interact with the Camera API.
* **The Promise:** We only use this permission to control the flash/torch on your device. Nothing else! ✅

### 🔔 NOTIFICATION Permission (Keeping the Service Alive)

* **Why is it needed?** To detect the shake gesture at any time—even when the screen is locked or you're browsing the web—the app must run a **Foreground Service** in the background. 📡
* **The Android Rule:** The Android Operating System requires that every Foreground Service displays a **persistent, visible notification** to the user. This is a system mandate to inform you that an app is consuming background resources.
* **The Requirement:** On modern Android versions (like Android 13+), to display this persistent notification, the app must explicitly request the `POST_NOTIFICATIONS` permission from you.
* **The Benefit:** This notification ensures that the operating system **DOES NOT KILL** the shake-monitoring service, making the app reliable! 🛡️

***

## 🛠️ Usage & Installation

1.  **Download the latest `shake-torch.apk`** from the [Releases page](https://github.com/Lotverp/Shake-Torch/releases).
2.  **Install** the APK on your Android device. 📱
3.  When you open the app for the first time, you will be prompted to grant the necessary **Camera** and **Notification** permissions. **Accept them!** 🙏
4.  Start shaking! You can now lock your screen and shake the device to activate the light. 🥳

## 🤝 Contributing

We welcome contributions! If you have any ideas, bug reports, or feature suggestions, feel free to open an [Issue](/Lotverp/Shake-Torch/issues) or submit a [Pull Request](/Lotverp/Shake-Torch/pulls).

Happy shaking! 👋
