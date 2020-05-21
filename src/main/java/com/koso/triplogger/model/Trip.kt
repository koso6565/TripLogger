package com.koso.triplogger.modelimport androidx.room.Entityimport androidx.room.PrimaryKeyimport com.squareup.moshi.JsonClass@Entity(tableName = "triplogger_trip_table")@JsonClass(generateAdapter = true)data class Trip(    @PrimaryKey(autoGenerate = true)    val id: Long = 0,    val starttime: Long,    val endtime: Long,    val nodes: List<DataSet>,    val distance: Int,    val filename: String?,    var note: String,    //remote properties    var remoteid: String? = null,    var user: String? = null,    var reference: TripFileReference? = null)@JsonClass(generateAdapter = true)data class TripFileReference(    val bucket: String,    val path: String)