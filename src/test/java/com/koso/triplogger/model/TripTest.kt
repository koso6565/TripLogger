package com.koso.triplogger.modelimport com.google.gson.GsonBuilderimport org.junit.Testclass TripTest{    @Test    fun testTripDataSet(){        val gson = GsonBuilder().serializeNulls().create()        val dataset1 = DataSet(3, HashMap<String, Any>().apply{            put("lat", 12.3645)            put("lon", 123.456)            put("rpm", 12236)            put("word", "test word")        })        val dataset2 = DataSet(888, HashMap<String, Any>().apply{            put("lat", 12.3645)            put("lon", 123.456)            put("rpm", 12236)            put("word", "test word")        })        val trip = Trip(0,123456,789456, listOf(dataset1, dataset2), distance = 1, note = "", filename = "abc.trip")        println(gson.toJson(trip))    }}