package com.koso.triplogger.converterimport androidx.room.TypeConverterimport com.google.gson.GsonBuilderimport com.koso.triplogger.model.TripFileReferenceclass TripReferenceConverter {    val gson = GsonBuilder().serializeNulls().create()    @TypeConverter    fun fromJson(value: String?): TripFileReference? {        return if(value == null) null else gson.fromJson(value, TripFileReference::class.java)    }    @TypeConverter    fun toJson(ref: TripFileReference?): String? {        return if(ref == null) null else gson.toJson(ref, TripFileReference::class.java)    }}