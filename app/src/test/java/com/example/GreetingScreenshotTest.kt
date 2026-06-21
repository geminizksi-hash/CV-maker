package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.ResumeProfile
import com.example.ui.RenderOnScreenResume
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockProfile = ResumeProfile(
        profileName = "Screenshot Test Resume",
        fullName = "Farhan Kabir",
        jobTitle = "Software Craftsman",
        email = "farhan@test.com",
        phone = "+8801555555555",
        address = "Sylhet, Bangladesh",
        website = "www.farhan.dev",
        linkedin = "linkedin.com/in/farhan",
        github = "github.com/farhan",
        summary = "Highly experienced mobile engineer crafting pixel perfect reactive apps using Jetpack Compose and modern architecture patterns.",
        resumeType = "professional"
    )

    composeTestRule.setContent { 
        MyApplicationTheme { 
            RenderOnScreenResume(profile = mockProfile) 
        } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
