package com.koso.triplogger.modelimport androidx.annotation.Keepimport androidx.room.Entityimport androidx.room.PrimaryKeyimport com.squareup.moshi.JsonClass@Keep@Entity(tableName = "triplogger_trip_table")@JsonClass(generateAdapter = true)data class Trip(    @PrimaryKey(autoGenerate = true)    var id: Long = 0,    val starttime: Long,    val endtime: Long,    val nodes: List<DataSet>,    val distance: Int,    val filename: String?,    var note: String,    //remote properties    var remoteid: String? = null,    var user: String? = null,    var reference: TripFileReference? = null)@Keep@JsonClass(generateAdapter = true)data class TripFileReference(    val bucket: String,    val path: String)