package com.koso.triplogger.dbimport androidx.lifecycle.LiveDataimport androidx.room.*@Daointerface TripRawDao {    @Query("SELECT * from triplogger_trip_table ORDER BY id DESC")    fun getAllLiveData(): LiveData<List<TripRaw>>    @Query("SELECT * from triplogger_trip_table ORDER BY id DESC")    suspend fun getAllSuspend(): List<TripRaw>    @Query("DELETE from triplogger_trip_table WHERE id = :id")    suspend fun deleteById(id: Long)    @Insert(onConflict = OnConflictStrategy.REPLACE)    suspend fun insert(trip: TripRaw)    @Query("DELETE FROM triplogger_trip_table")    suspend fun deleteAll()    @Delete    suspend fun delete(trip: TripRaw)    @Update    suspend fun update(trip: TripRaw)    @Query("SELECT * FROM triplogger_trip_table WHERE starttime = :start AND endtime = :end")    suspend fun findByTime(start: Long, end: Long): List<TripRaw>    @Query("SELECT * FROM triplogger_trip_table WHERE remoteid = :id")    fun findByRemoteId(id: String) : List<TripRaw>}