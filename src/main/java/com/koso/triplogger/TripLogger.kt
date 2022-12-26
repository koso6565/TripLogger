package com.koso.triploggerimport android.content.Contextimport android.location.Locationimport android.util.Logimport androidx.lifecycle.LiveDataimport androidx.lifecycle.MutableLiveDataimport com.google.firebase.crashlytics.ktx.crashlyticsimport com.google.firebase.ktx.Firebaseimport com.koso.triplogger.db.*import com.koso.triplogger.io.TripFileHelperimport com.koso.triplogger.model.DataSetimport com.koso.triplogger.model.Tripimport kotlinx.coroutines.Dispatchersimport kotlinx.coroutines.GlobalScopeimport kotlinx.coroutines.launchclass TripLogger private constructor(    val context: Context,    val intervalMilli: Long,    val delaySec: Int) {    /**     * The default subname of the stored trip file     */    private val fileSubname = ".trip"    /**     * The time millisecond when start the log     */    private var startTime = 0L    /**     * The time millisecond when end the log     */    private var endTime = 0L    /**     * Declare the pause status     */    private var pauseFlag = false    /**     * Repository to help to temporary store the dataset into database, until the trip end     */    private lateinit var datasetRepository: DataSetRepository    /**     * Repository to help to store the complete trip data     */    private lateinit var tripRepository: TripRepository    /**     * LiveData of logging state     */    private val _isLoggingLive: MutableLiveData<Boolean> = MutableLiveData<Boolean>()    fun isLogging(): LiveData<Boolean> = _isLoggingLive    val crashlytics = Firebase.crashlytics    init {        val db = TripLoggerRoomDatabase.getDatabase(context)        val dataSetDao = db.datasetDao()        datasetRepository = DataSetRepository(dataSetDao)        tripRepository = TripRepository.getInstance(context)    }    fun startWithoutTimer() {        GlobalScope.launch(Dispatchers.IO) {            datasetRepository.clear()        }        startTime = System.currentTimeMillis()        _isLoggingLive.value = true    }    fun insertData(datas: HashMap<String, Any?>) {        insertData(datas, System.currentTimeMillis())    }    fun insertData(datas: HashMap<String, Any?>, timestamp: Long) {        val dataset = DataSet(datas, timestamp)        GlobalScope.launch {            datasetRepository.insert(DataSetRaw.createFromDataSet(dataset))        }    }    /**     * Pause the log, during the pause period, no data will be log but the time elapse is continuing     */    fun pause() {        pauseFlag = true    }    /**     * Used to check if it's on pause state     */    fun isPause(): Boolean {        _isLoggingLive.value = false        return pauseFlag    }    /**     * To resume from the pause state     */    fun resume() {        _isLoggingLive.value = true        pauseFlag = false    }    suspend fun endToCreateLogWithCondition(        topic: String,        start: Float,        end: Float,        dataRefine: ((Trip) -> Unit)? = null    ): Trip? {        _isLoggingLive.postValue(false)        val allDataSetRaw = datasetRepository.getAllSuspend()        var firstIndex = 0        loop@for(index in allDataSetRaw.indices){            when{                end > start -> { // acceleration & distance                    if (allDataSetRaw[index].getItemsMap()[topic].toString().toFloat() > start) {                        firstIndex = Math.max(index - 1, 0)                        break@loop                    }                }                else -> { // deceleration                    if (allDataSetRaw[index].getItemsMap()[topic].toString().toFloat() < start) {                        firstIndex = Math.max(index - 1, 0)                        break@loop                    }                }            }        }//        if(firstIndex > 0) firstIndex -= 1   <--- dont know why        var lastIndex = 0        loop@for(index in allDataSetRaw.indices){            if (index < firstIndex) continue@loop            when{                end > start -> { // acceleration & distance                    if (allDataSetRaw[index].getItemsMap()[topic].toString().toFloat() >= end) {                        lastIndex = index                        break@loop                    }                }                else -> { // deceleration                    if (allDataSetRaw[index].getItemsMap()[topic].toString().toFloat() <= end) {                        lastIndex = index                        break@loop                    }                }            }        }        if (lastIndex <= firstIndex) {            lastIndex = allDataSetRaw.size - 1        }        val distance =            allDataSetRaw[lastIndex].getItemsMap()["powertry_distance"].toString().toFloat()        val filename = "${allDataSetRaw[firstIndex].timestamp}$fileSubname"        val trip = Trip(            starttime = allDataSetRaw[firstIndex].timestamp,            endtime = allDataSetRaw[lastIndex].timestamp,            nodes = allDataSetRaw.filter { d ->                d.timestamp >= allDataSetRaw[firstIndex].timestamp.toString().toLong() &&                        d.timestamp <= allDataSetRaw[lastIndex].timestamp.toString().toLong()            }.map { d -> d.toDataSet() },            distance = distance.toInt(),            filename = filename,            note = ""        )        if (trip.nodes.size < 5) {            throw RuntimeException("Node size should more than 5, it is ${trip.nodes.size}")        }        dataRefine?.invoke(trip)        val tripRaw = TripRaw.fromTrip(filename, trip)        if (TripFileHelper.writeToLocal(context, filename, trip)) {            val id = tripRepository.insert(tripRaw)            trip.id = id            Log.d("TripLogger", "write to local file successfully")        }        datasetRepository.clear()        return trip    }    suspend fun endToCreateLog(from: Long, to: Long, distance: Int): Trip {        endTime = System.currentTimeMillis()        _isLoggingLive.postValue(false)        val allDataSetRaw = datasetRepository.getPeriodSuspend(from - 50, to)        val filename = "$from$fileSubname"        val trip = Trip(            starttime = from,            endtime = to,            nodes = allDataSetRaw.map { d -> d.toDataSet() },            distance = distance,            filename = filename,            note = ""        )        val tripRaw = TripRaw.fromTrip(filename, trip)        if (TripFileHelper.writeToLocal(context, filename, trip)) {            val id = tripRepository.insert(tripRaw)            trip.id = id            Log.d("TripLogger", "write to local file successfully")        }        datasetRepository.clear()        return trip    }    /**     * Complete the whole log procedure and create a trip record with its log file     */    suspend fun endToCreateLog(): Trip {        endTime = System.currentTimeMillis()        _isLoggingLive.postValue(false)        val allDataSetRaw = datasetRepository.getAllSuspend()        val filename = "$startTime$fileSubname"        val trip = Trip(            starttime = startTime,            endtime = endTime,            nodes = allDataSetRaw.map { d -> d.toDataSet() },            distance = getDistance(allDataSetRaw),            filename = filename,            note = ""        )        val tripRaw = TripRaw.fromTrip(filename, trip)        if (TripFileHelper.writeToLocal(context, filename, trip)) {            val id = tripRepository.insert(tripRaw)            trip.id = id            Log.d("TripLogger", "write to local file successfully")        }        datasetRepository.clear()        return trip    }    /**     * Calculate the distance in meter     */    private fun getDistance(allDataSetRaw: List<DataSetRaw>): Int {        var preLatlng: DoubleArray? = null        var sumOfDistance = 0f        for (node in allDataSetRaw) {            val loc = node.getItemsMap()["location"]            loc?.let {                val latlng =                    (loc as String).split(",").map { s -> s.toDouble() }.toDoubleArray()                if (preLatlng != null) {                    val results = FloatArray(3)                    Location.distanceBetween(                        preLatlng!![0],                        preLatlng!![1],                        latlng[0],                        latlng[1],                        results                    )                    sumOfDistance += results[0]                }                preLatlng = latlng            }        }        return sumOfDistance.toInt()    }    /**     * End the log procrdure, but no record the trip     */    suspend fun endAndCancel() {        endTime = System.currentTimeMillis()        _isLoggingLive.postValue(false)        GlobalScope.launch {            datasetRepository.clear()        }    }    /**     * The builder for creating TripLogger based on Builder pattern     */    class Builder(val context: Context) {        /**         * The data sampling time interval in second         */        private var interval: Long = 5000        /**         * The delay seconds after logger start to work         */        private var delay: Int = 0        /**         * Provide the sampling rate, the default value is 5 second         */        fun interval(millisec: Long): Builder {            interval = millisec            return this        }        /**         * Provide the delay seconds after logger start         */        fun delay(sec: Int): Builder {            delay = sec            return this        }        /**         * Create a trip logger instance         */        fun build(): TripLogger {            return TripLogger(context, interval, delay)        }    }}