package com.koso.triplogger.dbimport android.content.Contextimport androidx.room.Databaseimport androidx.room.Roomimport androidx.room.RoomDatabaseimport androidx.room.TypeConvertersimport androidx.room.migration.Migrationimport androidx.sqlite.db.SupportSQLiteDatabase@Database(entities = arrayOf(TripRaw::class, DataSetRaw::class), version = 2, exportSchema = false)@TypeConverters(TripTypeConverter::class)abstract class TripLoggerRoomDatabase : RoomDatabase() {    abstract fun tripDao(): TripRawDao    abstract fun datasetDao(): DataSetRawDao    companion object {        @Volatile        private var INSTANCE: TripLoggerRoomDatabase? = null        fun getDatabase(context: Context): TripLoggerRoomDatabase {            val tempInstance = INSTANCE            if (tempInstance != null) {                return tempInstance            }            synchronized(this) {                val instance = Room.databaseBuilder(                    context.applicationContext,                    TripLoggerRoomDatabase::class.java,                    "triplogger_database"                ).addMigrations(MIGRATION_1_2).build()                INSTANCE = instance                return instance            }        }        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {            override fun migrate(database: SupportSQLiteDatabase) {                database.execSQL(                    "ALTER TABLE triplogger_trip_table ADD COLUMN remoteid TEXT"                )                database.execSQL(                    "ALTER TABLE triplogger_trip_table ADD COLUMN user TEXT"                )                database.execSQL(                    "ALTER TABLE triplogger_trip_table ADD COLUMN reference TEXT"                )            }        }    }}