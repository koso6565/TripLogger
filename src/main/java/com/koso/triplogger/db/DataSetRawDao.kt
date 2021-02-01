package com.koso.triplogger.dbimport androidx.lifecycle.LiveDataimport androidx.room.Daoimport androidx.room.Insertimport androidx.room.OnConflictStrategyimport androidx.room.Query@Daointerface DataSetRawDao{    @Query("SELECT * from triplogger_dataset_table ORDER BY timestamp ASC")    fun getAll(): LiveData<List<DataSetRaw>>    @Query("SELECT * from triplogger_dataset_table ORDER BY timestamp ASC")    suspend fun getAllSuspend(): List<DataSetRaw>    @Query("SELECT * from triplogger_dataset_table where timestamp >= :from and timestamp <= :to ORDER BY timestamp ASC")    suspend fun getPeriodSuspend(from: Long, to: Long): List<DataSetRaw>    @Insert(onConflict = OnConflictStrategy.REPLACE)    suspend fun insert(dataSetRaw: DataSetRaw)    @Query("DELETE FROM triplogger_dataset_table")    suspend fun deleteAll()}