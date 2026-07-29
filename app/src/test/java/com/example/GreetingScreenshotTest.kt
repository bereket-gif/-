package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
    val sampleMezmur = com.example.data.local.MezmurEntity(
        id = "m1",
        title = "ፍቅርህ ማርኮኛል",
        artist = "የዘማሪ ዲያቆን ሉልሰገድ",
        category = "የንስሐ",
        lyrics = "ፍቅርህ ማርኮኛል አምላኬ አዳኜ",
        numberGeez = "፩",
        numberInt = 1
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.MezmurListItem(
            mezmur = sampleMezmur,
            onClick = {},
            onToggleFavorite = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
