package com.koso.triplogger.dbimport android.content.Contextimport androidx.room.Entityimport androidx.room.PrimaryKeyimport com.koso.triplogger.io.TripFileHelperimport com.koso.triplogger.model.DataSetimport com.koso.triplogger.model.Tripimport com.squareup.moshi.JsonAdapterimport com.squareup.moshi.Moshiimport com.squareup.moshi.Typesimport java.lang.reflect.Type@Entity(tableName = "triplogger_trip_table")class TripRaw(    @PrimaryKey(autoGenerate = true)    val id: Long,    val starttime: Long,    val endtime: Long,    val filename: String) {    suspend fun toTrip(context: Context): Trip {        val trip = TripFileHelper.readFromLocal(context, filename)        return Trip(id, starttime, endtime, trip?.nodes ?: listOf())    }    companion object {        fun fromTrip(filename: String, trip: Trip): TripRaw {            val moshi = Moshi.Builder().build()            val listMyData: Type = Types.newParameterizedType(                MutableList::class.java,                DataSet::class.java            )            val adapter: JsonAdapter<List<DataSet>> = moshi.adapter(listMyData)            return TripRaw(                starttime = trip.starttime,                endtime = trip.endtime,                filename = filename,                id = trip.id            )        }    }}