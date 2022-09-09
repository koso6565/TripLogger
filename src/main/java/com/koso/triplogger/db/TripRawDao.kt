package com.koso.triplogger.dbimport androidx.lifecycle.LiveDataimport androidx.room.*@Daointerface TripRawDao {    @Query("SELECT * from triplogger_trip_table ORDER BY id DESC")    fun getAllLiveData(): LiveData<List<TripRaw>>    @Query("SELECT * from triplogger_trip_table ORDER BY id DESC")    fun getAllSuspend(): List<TripRaw>    @Query("DELETE from triplogger_trip_table WHERE id = :id")    fun deleteById(id: Long)    @Insert(onConflict = OnConflictStrategy.REPLACE)    fun insert(trip: TripRaw): Long    @Query("DELETE FROM triplogger_trip_table")    fun deleteAll()    @Delete    fun delete(trip: TripRaw)    @Update    fun update(trip: TripRaw): Int    @Query("UPDATE triplogger_trip_table SET remoteid = :remoteId, user = :user WHERE id = :id")    fun updateRemoteData(id: Long, remoteId: String?, user: String?): Int    @Query("SELECT * FROM triplogger_trip_table WHERE starttime = :start AND endtime = :end")    fun findByTime(start: Long, end: Long): List<TripRaw>    @Query("SELECT * FROM triplogger_trip_table WHERE remoteid = :id")    fun findByRemoteId(id: String) : List<TripRaw>}