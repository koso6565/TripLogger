package com.koso.triploggerimport android.content.Contextimport com.koso.triplogger.db.*import com.koso.triplogger.ext.containsObsDataimport com.koso.triplogger.io.TripFileHelperimport com.koso.triplogger.model.DataSetimport com.koso.triplogger.model.Tripimport kotlinx.coroutines.GlobalScopeimport kotlinx.coroutines.launchimport java.util.*class TripLogger private constructor(    val context: Context,    val obsDataList: List<ObservableData> = listOf(),    val intervalSec: Int,    val delaySec: Int) {    /**     * The default subname of the stored trip file     */    private val fileSubname = ".trip"    /**     * The timer for retrieving data periodically     */    private var timer: Timer = Timer()    /**     * The time millisecond when start the log     */    private var startTime = 0L    /**     * The time millisecond when end the log     */    private var endTime = 0L    /**     * Declare the pause status     */    private var pauseFlag = false    /**     * Repository to help to temporary store the dataset into database, until the trip end     */    private lateinit var datasetRepository: DataSetRepository    /**     * Repository to help to store the complete trip data     */    private lateinit var tripRepository: TripRepository    /**     * The TimerTask that called periodically to process the data observe and log     */    private var worker = object : TimerTask() {        override fun run() {            if (!pauseFlag) {                val datas = HashMap<String, Any>()                for (data in obsDataList) {                    datas[data.name()] = data.value()                }                val dataset = DataSet(elapsedTime(), datas)                GlobalScope.launch {                    datasetRepository.insert(DataSetRaw.createFromDataSet(dataset))                }            }        }    }    init {        val db = TripLoggerRoomDatabase.getDatabase(context)        val dataSetDao = db.datasetDao()        datasetRepository = DataSetRepository(dataSetDao)        val tripDao = db.tripDao()        tripRepository = TripRepository(tripDao)    }    /**     * Start to log     */    fun start() {        timer = Timer()        timer.schedule(worker, delaySec * 1000L, intervalSec * 1000L)        startTime = System.currentTimeMillis()    }    /**     * To check if the log is started     */    fun isStarted(): Boolean {        return startTime > 0 && endTime == 0L    }    /**     * Pause the log, during the pause period, no data will be log but the time elapse is continuing     */    fun pause() {        pauseFlag = true    }    /**     * Used to check if it's on pause state     */    fun isPause(): Boolean {        return pauseFlag    }    /**     * To resume from the pause state     */    fun resume() {        pauseFlag = false    }    /**     * Complete the whole log     */    fun end() {        endTime = System.currentTimeMillis()        timer.cancel()        timer.purge()        GlobalScope.launch {            val allDataSetRaw = datasetRepository.getAllSuspend()            val trip = Trip(                starttime = startTime,                endtime = endTime,                nodes = allDataSetRaw.map { d -> d.toDataSet() })            val filename = "$endTime$fileSubname"            if(TripFileHelper.writeToLocal(context, filename, trip)){                tripRepository.insert(TripRaw.fromTrip(filename, trip))            }        }    }    /**     * Elapsed time from start to now in millisecond     */    private fun elapsedTime(): Long {        return System.currentTimeMillis() - startTime    }    /**     * The builder for creating TripLogger based on Builder pattern     */    inner class Builder(val context: Context) {        /**         * The observable data list         */        private val obsDataList: ArrayList<ObservableData> = arrayListOf()        /**         * The data sampling time interval in second         */        private var interval: Int = 5        /**         * The delay seconds after logger start to work         */        private var delay: Int = 0        /**         * Register a observable data         */        fun addObservableData(obsData: ObservableData): Builder {            if (!obsDataList.containsObsData(obsData)) {                obsDataList.add(obsData)            }            return this        }        /**         * Provide the sampling rate, the default value is 5 second         */        fun interval(sec: Int): Builder {            interval = sec            return this        }        /**         * Provide the delay seconds after logger start         */        fun delay(sec: Int): Builder {            delay = sec            return this        }        /**         * Create a trip logger instance         */        fun build(): TripLogger {            return TripLogger(context, obsDataList, interval, delay)        }    }}