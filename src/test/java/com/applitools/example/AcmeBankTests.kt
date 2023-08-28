package com.applitools.example

import com.applitools.eyes.BatchInfo
import com.applitools.eyes.EyesRunner
import com.applitools.eyes.RectangleSize
import com.applitools.eyes.selenium.BrowserType
import com.applitools.eyes.selenium.ClassicRunner
import com.applitools.eyes.selenium.Configuration
import com.applitools.eyes.selenium.Eyes
import com.applitools.eyes.selenium.fluent.Target
import com.applitools.eyes.visualgrid.model.DeviceName
import com.applitools.eyes.visualgrid.model.ScreenOrientation
import com.applitools.eyes.visualgrid.services.RunnerOptions
import com.applitools.eyes.visualgrid.services.VisualGridRunner
import org.junit.jupiter.api.*
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.lang.Boolean
import java.net.MalformedURLException
import java.net.URL
import java.time.Duration
import kotlin.String
import kotlin.Throws

class AcmeBankTests {
    private lateinit var driver: WebDriver
    private lateinit var eyes: Eyes

    @BeforeEach
    @Throws(MalformedURLException::class)
    fun openBrowserAndEyes(testInfo: TestInfo) {
        val options = ChromeOptions().setHeadless(headless)
        driver = if (USE_EXECUTION_CLOUD) {
            RemoteWebDriver(URL(Eyes.getExecutionCloudURL()), options)
        } else {
            ChromeDriver(options)
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
        eyes = Eyes(runner)
        eyes.setConfiguration(config)
        eyes.open(
                driver,
                "ACME Bank Web App",
                testInfo.displayName,
                RectangleSize(1200, 600)
        )
    }

    @Test
    fun logIntoBankAccount() {
        driver["https://demo.applitools.com"]
        eyes.check(Target.window().fully().withName("Login page"))
        driver.findElement(By.id("username")).sendKeys("applibot")
        driver.findElement(By.id("password")).sendKeys("I<3VisualTests")
        driver.findElement(By.id("log-in")).click()
        eyes.check(Target.window().fully().withName("Main page").layout())
    }

    @AfterEach
    fun cleanUpTest() {
        eyes.closeAsync()
        driver.quit()
    }

    companion object {
        private const val USE_ULTRAFAST_GRID = true
        private const val USE_EXECUTION_CLOUD = false
        private var applitoolsApiKey: String? = null
        private var headless = false
        private var batch: BatchInfo? = null
        private lateinit var config: Configuration
        private var runner: EyesRunner? = null

        @BeforeAll
        @JvmStatic
        fun setUpConfigAndRunner() {
            applitoolsApiKey = System.getenv("APPLITOOLS_API_KEY")
            headless = Boolean.parseBoolean(System.getenv().getOrDefault("HEADLESS", "false"))
            runner = if (USE_ULTRAFAST_GRID) {
                VisualGridRunner(RunnerOptions().testConcurrency(5))
            } else {
                ClassicRunner()
            }
            val runnerName = if (USE_ULTRAFAST_GRID) "Ultrafast Grid" else "Classic runner"
            batch = BatchInfo("Example: Selenium Java JUnit with the $runnerName")
            config = Configuration()
            config.apiKey = applitoolsApiKey
            config.batch = batch
            if (USE_ULTRAFAST_GRID) {
                config.addBrowser(800, 600, BrowserType.CHROME)
                config.addBrowser(1600, 1200, BrowserType.FIREFOX)
                config.addBrowser(1024, 768, BrowserType.SAFARI)
                config.addDeviceEmulation(DeviceName.Pixel_2, ScreenOrientation.PORTRAIT)
                config.addDeviceEmulation(DeviceName.Nexus_10, ScreenOrientation.LANDSCAPE)
            }
        }

        @AfterAll
        @JvmStatic
        fun printResults() {
            val allTestResults = runner!!.allTestResults
            println(allTestResults)
        }
    }
}