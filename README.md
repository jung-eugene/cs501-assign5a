# CS 501 Individual Assignment 5 Question 1 — What's for Dinner?

## Overview
**What's for Dinner?** is a 3-screen Android app built with **Jetpack Compose** that allows users to browse, add, and view recipes.  
It demonstrates **Compose Navigation with arguments**, **ViewModel state management**, and **Bottom Navigation** integration.

## Features
- **Home Screen:** Displays a list of recipe titles using `LazyColumn`. Tap a recipe to see details.  
- **Detail Screen:** Shows the recipe’s title, ingredients, and steps (passed as navigation arguments).  
- **Add Recipe Screen:** A form for adding new recipes, stored in memory using a `ViewModel`.  
- **Bottom Navigation:** Lets users switch between Home, Add, and Settings.  
- **Backstack Control:** Uses `popUpTo()` and `launchSingleTop` to prevent duplicate screens when navigating.

## How to Run
1. Open the project in **Android Studio**.  
2. In `app/build.gradle.kts`, make sure you have these dependencies:
   ```kotlin
   implementation("androidx.navigation:navigation-compose:2.8.3")
   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
   implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
   ```
3. Sync Gradle and run the app on an emulator or physical device.

### App Flow
1. **Home → Detail:** Tap a recipe name to open the full recipe page.  
2. **Add → Home:** Add a recipe using the form, then navigate back to see it listed.  
3. **Bottom Navigation:** Switch between Home, Add, and Settings easily.

### Architecture
| Component | Description |
|------------|-------------|
| `RecipeViewModel` | Manages recipe data and ensures it persists across configuration changes. |
| `NavHost` & `Routes` | Define navigation paths and pass recipe data using route arguments. |
| `Scaffold` | Provides consistent structure for the top app bar and bottom navigation bar. |

## AI Usage Documentation

### How AI Was Used
- Helped identify **missing dependencies** for Jetpack Compose Navigation after errors like the following were encountered:
  ```
  Unresolved reference 'navigation'
  Unresolved reference 'rememberNavController'
  ```
  
  **Fix:** Added the correct dependencies for Navigation and ViewModel integration:
  ```kotlin
  implementation("androidx.navigation:navigation-compose:2.8.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
  ```

- Helped fix a **parameter order error** in `ElevatedCard`.  

  The original code was:
  ```kotlin
  ElevatedCard(Modifier.fillMaxWidth(), onClick = { ... })
  ```
  which caused:
  ```
  Argument type mismatch: actual type is 'Modifier', but 'kotlin.Function0<Unit>' was expected
  ```
  **Fix:** Named parameters properly:
  ```kotlin
  ElevatedCard(onClick = { ... }, modifier = Modifier.fillMaxWidth())
  ```

### Where AI Misunderstood
AI initially assumed the project already had `compose.*` and `navigation.*` imports configured.  
This led to unresolved reference errors when trying to use `NavHost`, `composable`, and `rememberNavController`.  
After manually adding the correct dependencies and imports, the navigation system worked correctly.
