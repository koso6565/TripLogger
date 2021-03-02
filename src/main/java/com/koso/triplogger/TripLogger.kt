package com.koso.triploggerimport android.content.Contextimport android.location.Locationimport android.util.Logimport androidx.lifecycle.LiveDataimport androidx.lifecycle.MutableLiveDataimport com.koso.triplogger.db.*import com.koso.triplogger.ext.containsObsDataimport com.koso.triplogger.io.TripFileHelperimport com.koso.triplogger.model.DataSetimport com.koso.triplogger.model.Tripimport kotlinx.coroutines.Dispatchersimport kotlinx.coroutines.GlobalScopeimport kotlinx.coroutines.launchimport kotlinx.coroutines.withContextimport java.util.*class TripLogger private constructor(    val context: Context,    val obsTripDataList: List<ObservableTripData> = listOf(),    val intervalMilli: Long,    val delaySec: Int) {    /**     * The default subname of the stored trip file     */    private val fileSubname = ".trip"    /**     * The timer for retrieving data periodically     */    private var timer: Timer = Timer()    /**     * The time millisecond when start the log     */    private var startTime = 0L    /**     * The time millisecond when end the log     */    private var endTime = 0L    /**     * Declare the pause status     */    private var pauseFlag = false    /**     * Repository to help to temporary store the dataset into database, until the trip end     */    private lateinit var datasetRepository: DataSetRepository    /**     * Repository to help to store the complete trip data     */    private lateinit var tripRepository: TripRepository    /**     * LiveData of logging state     */    private val _isLoggingLive: MutableLiveData<Boolean> = MutableLiveData<Boolean>()    fun isLogging(): LiveData<Boolean> = _isLoggingLive    /**     * The TimerTask that called periodically to process the data observe and log     */    private var worker = object : TimerTask() {        override fun run() {            if (!pauseFlag) {                val datas = HashMap<String, Any?>()                for (data in obsTripDataList) {                    datas[data.tag()] = data.value()                }                val dataset = DataSet(datas, System.currentTimeMillis())                GlobalScope.launch {                    datasetRepository.insert(DataSetRaw.createFromDataSet(dataset))                }            }        }    }    init {        val db = TripLoggerRoomDatabase.getDatabase(context)        val dataSetDao = db.datasetDao()        datasetRepository = DataSetRepository(dataSetDao)        tripRepository = TripRepository.getInstance(context)    }    /**     * Start to log     */    fun start() {        GlobalScope.launch(Dispatchers.IO) {            datasetRepository.clear()        }        timer = Timer()        timer.schedule(worker, delaySec * 1000L, intervalMilli)        startTime = System.currentTimeMillis()        _isLoggingLive.value = true    }    /**     * Pause the log, during the pause period, no data will be log but the time elapse is continuing     */    fun pause() {        pauseFlag = true    }    /**     * Used to check if it's on pause state     */    fun isPause(): Boolean {        _isLoggingLive.value = false        return pauseFlag    }    /**     * To resume from the pause state     */    fun resume() {        _isLoggingLive.value = true        pauseFlag = false    }    suspend fun endToCreateLog(from: Long, to: Long): Trip {        withContext(Dispatchers.Main) {            detachObs()        }        endTime = System.currentTimeMillis()        timer.cancel()        timer.purge()        _isLoggingLive.postValue(false)        val allDataSetRaw = datasetRepository.getPeriodSuspend(from, to)        val filename = "$startTime$fileSubname"        val trip = Trip(            starttime = startTime,            endtime = endTime,            nodes = allDataSetRaw.map { d -> d.toDataSet() },            distance = getDistance(allDataSetRaw),            filename = filename,            note = ""        )        val tripRaw = TripRaw.fromTrip(filename, trip)        if (TripFileHelper.writeToLocal(context, filename, trip)) {            val id = tripRepository.insert(tripRaw)            trip.id = id            Log.d("TripLogger", "write to local file successfully")        }        datasetRepository.clear()        return trip    }    /**     * Complete the whole log procedure and create a trip record with its log file     */    suspend fun endToCreateLog(): Trip {        withContext(Dispatchers.Main) {            detachObs()        }        endTime = System.currentTimeMillis()        timer.cancel()        timer.purge()        _isLoggingLive.postValue(false)        val allDataSetRaw = datasetRepository.getAllSuspend()        val filename = "$startTime$fileSubname"        val trip = Trip(            starttime = startTime,            endtime = endTime,            nodes = allDataSetRaw.map { d -> d.toDataSet() },            distance = getDistance(allDataSetRaw),            filename = filename,            note = ""        )        val tripRaw = TripRaw.fromTrip(filename, trip)        if (TripFileHelper.writeToLocal(context, filename, trip)) {            val id = tripRepository.insert(tripRaw)            trip.id = id            Log.d("TripLogger", "write to local file successfully")        }        datasetRepository.clear()        return trip    }    private fun detachObs() {        for (obs in obsTripDataList) {            obs.detach()        }    }    /**     * Calculate the distance in meter     */    private fun getDistance(allDataSetRaw: List<DataSetRaw>): Int {        var preLatlng: DoubleArray? = null        var sumOfDistance = 0f        for (node in allDataSetRaw) {            val loc = node.getItemsMap()["location"]            loc?.let {                val latlng =                    (loc as String).split(",").map { s -> s.toDouble() }.toDoubleArray()                if (preLatlng != null) {                    val results = FloatArray(3)                    Location.distanceBetween(                        preLatlng!![0],                        preLatlng!![1],                        latlng[0],                        latlng[1],                        results                    )                    sumOfDistance += results[0]                }                preLatlng = latlng            }        }        return sumOfDistance.toInt()    }    /**     * End the log procrdure, but no record the trip     */    suspend fun endAndCancel() {        withContext(Dispatchers.Main) {            detachObs()        }        endTime = System.currentTimeMillis()        timer.cancel()        timer.purge()        _isLoggingLive.postValue(false)        GlobalScope.launch {            datasetRepository.clear()        }    }    /**     * Elapsed time from start to now in millisecond     */    private fun elapsedTime(): Long {        return System.currentTimeMillis() - startTime    }    /**     * The builder for creating TripLogger based on Builder pattern     */    class Builder(val context: Context) {        /**         * The observable data list         */        private val obsTripDataList: ArrayList<ObservableTripData> = arrayListOf()        /**         * The data sampling time interval in second         */        private var interval: Long = 5000        /**         * The delay seconds after logger start to work         */        private var delay: Int = 0        /**         * Register a observable data         */        fun addObservableData(obsTripData: ObservableTripData): Builder {            if (!obsTripDataList.containsObsData(obsTripData)) {                obsTripDataList.add(obsTripData)            }            return this        }        /**         * Provide the sampling rate, the default value is 5 second         */        fun interval(millisec: Long): Builder {            interval = millisec            return this        }        /**         * Provide the delay seconds after logger start         */        fun delay(sec: Int): Builder {            delay = sec            return this        }        /**         * Create a trip logger instance         */        fun build(): TripLogger {            return TripLogger(context, obsTripDataList, interval, delay)        }    }}