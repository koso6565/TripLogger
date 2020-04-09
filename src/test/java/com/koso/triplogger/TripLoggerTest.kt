package com.koso.triploggerimport android.content.Contextimport androidx.test.core.app.ApplicationProviderimport androidx.test.ext.junit.runners.AndroidJUnit4import kotlinx.coroutines.Dispatchersimport kotlinx.coroutines.delayimport kotlinx.coroutines.newSingleThreadContextimport kotlinx.coroutines.runBlockingimport kotlinx.coroutines.test.setMainimport org.junit.Afterimport org.junit.Beforeimport org.junit.Testimport org.junit.runner.RunWith@RunWith(AndroidJUnit4::class)class TripLoggerTest {    private lateinit var context: Context    private val mainThreadSurrogate = newSingleThreadContext("UI thread")    val speedData = object: ObservableData{        override fun name(): String {            return "speed"        }        override fun value(): Any {            return (Math.random() * 100).toInt()        }    }    val locData = object : ObservableData{        override fun name(): String {            return "location"        }        override fun value(): Any {            return "12.12345,56.12345"        }    }    @Before    fun setUp() {        context = ApplicationProvider.getApplicationContext<Context>()        Dispatchers.setMain(mainThreadSurrogate)    }    @After    fun tearDown() {    }    @Test    fun test(){        val logger = TripLogger.Builder(context)            .addObservableData(locData)            .addObservableData(speedData)            .interval(2)            .build()        logger.start()        runBlocking(Dispatchers.Main) {            delay(12000)            val trip = logger.end()            println(trip.toString())        }    }}