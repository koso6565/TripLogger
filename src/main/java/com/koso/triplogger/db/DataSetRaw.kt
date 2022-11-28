package com.koso.triplogger.dbimport androidx.room.Entityimport androidx.room.PrimaryKeyimport com.koso.triplogger.model.DataSetimport com.squareup.moshi.JsonAdapterimport com.squareup.moshi.Moshiimport com.squareup.moshi.Typesimport java.lang.reflect.Type@Entity(tableName = "triplogger_dataset_table")class DataSetRaw(    @PrimaryKey    var items: String = "",    val timestamp: Long){    companion object{        /**         * Create DataSetRaw helper from dataset         */        fun createFromDataSet(dataset: DataSet): DataSetRaw{            val raw = DataSetRaw(timestamp = dataset.timestamp)            raw.setItemsMap(dataset.items)            return raw        }    }    fun setItemsMap(map: Map<String, Any?>){        val moshi = Moshi.Builder().build()        val listMyData: Type = Types.newParameterizedType(            MutableMap::class.java,            String::class.java,            Any::class.java        )        val adapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(listMyData)//        Log.e("error", "setItemsMap: ${map}")        items = adapter.toJson(map)    }    fun getItemsMap(): Map<String, Any>{        val moshi = Moshi.Builder().build()        val listMyData: Type = Types.newParameterizedType(            MutableMap::class.java,            String::class.java,            Any::class.java        )        val adapter: JsonAdapter<Map<String, Any>> = moshi.adapter(listMyData)        return adapter.fromJson(items)?: HashMap()    }    fun toDataSet(): DataSet {        return DataSet(getItemsMap().toMutableMap(), timestamp)    }}