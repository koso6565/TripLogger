package com.koso.triplogger.dbimport androidx.room.TypeConverterimport com.koso.triplogger.model.TripFileReferenceimport com.koso.triplogger.model.TripFileReferenceJsonAdapterimport com.squareup.moshi.Moshiclass TripTypeConverter {    val adapter: TripFileReferenceJsonAdapter =        TripFileReferenceJsonAdapter(Moshi.Builder().build())    @TypeConverter    fun fromJson(value: String): TripFileReference? {        return adapter.fromJson(value)    }    @TypeConverter    fun tripFileReferenceToString(reference: TripFileReference): String{        return adapter.toJson(reference)    }}