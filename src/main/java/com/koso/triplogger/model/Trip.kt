package com.koso.triplogger.modelimport androidx.room.Entityimport androidx.room.PrimaryKeyimport com.squareup.moshi.JsonClass@Entity(tableName = "triplogger_trip_table")@JsonClass(generateAdapter = true)data class Trip(    @PrimaryKey(autoGenerate = true)    val id: Long = 0,    val starttime: Long,    val endtime: Long,    val nodes: List<DataSet>,    val distance: Int)