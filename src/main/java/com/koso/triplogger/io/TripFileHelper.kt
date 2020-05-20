package com.koso.triplogger.ioimport android.content.Contextimport com.koso.triplogger.model.Tripimport com.koso.triplogger.model.TripJsonAdapterimport com.squareup.moshi.Moshiimport kotlinx.coroutines.Dispatchersimport kotlinx.coroutines.withContextimport java.io.BufferedInputStreamimport java.io.Fileimport java.io.FileInputStreamclass TripFileHelper {    companion object {        /**         * To write trip into local persist storage with json format         */        suspend fun writeToLocal(context: Context, filename: String, trip: Trip): Boolean {            return withContext(Dispatchers.IO) {                val adapter = TripJsonAdapter(Moshi.Builder().build())                val contentText = adapter.toJson(trip)                val file = File(context.filesDir, filename)                file.writeText(contentText)                true            }        }        suspend fun readFromLocal(context: Context, filename: String): Trip? {            return withContext(Dispatchers.IO) {                val adapter = TripJsonAdapter(Moshi.Builder().build())                val file = File(context.filesDir, filename)                if(file.exists()) {                    val content = file.readText()                    adapter.fromJson(content)                }else{                    null                }            }        }        suspend fun deleteFile(context: Context, filename: String): Boolean {            return withContext(Dispatchers.IO){                val file = File(context.filesDir, filename)                try {                    file.delete()                }catch (e: Exception){                    false                }            }        }        suspend fun getFileByte(context: Context, filename: String): ByteArray?{            val file = File(context.filesDir, filename)            val size: Int = file.length().toInt()            val bytes = ByteArray(size)            return withContext(Dispatchers.IO) {                try {                    val buf = BufferedInputStream(FileInputStream(file))                    buf.read(bytes, 0, bytes.size)                    buf.close()                    bytes                } catch (e: java.lang.Exception) {                    null                }            }        }    }}