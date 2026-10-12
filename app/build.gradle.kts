plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "org.oneflex.tv"
 compileSdk = 35
 defaultConfig { applicationId = "org.oneflex.tv"; minSdk = 28; targetSdk = 28; versionCode = 1; versionName = "1.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}