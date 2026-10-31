package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NetPulseDao {
    @Query("SELECT * FROM network_devices ORDER BY isGateway DESC, isCurrentDevice DESC, isOnline DESC, ipAddress ASC")
    fun observeAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM network_devices WHERE macAddress = :mac LIMIT 1")
    suspend fun getDeviceByMac(mac: String): DeviceEntity?

    @Query("SELECT * FROM network_devices")
    suspend fun getAllDevicesSnapshot(): List<DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("DELETE FROM network_devices WHERE macAddress = :mac")
    suspend fun deleteDeviceByMac(mac: String)

    // Speed Test History
    @Query("SELECT * FROM speed_test_records ORDER BY timestamp DESC LIMIT 20")
    fun observeSpeedHistory(): Flow<List<SpeedTestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedRecord(record: SpeedTestRecordEntity)

    // Security Events & Intruder Alerts
    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT 50")
    fun observeSecurityEvents(): Flow<List<SecurityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityEvent(event: SecurityEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityEvents(events: List<SecurityEventEntity>)

    @Query("UPDATE security_events SET isResolved = 1 WHERE id = :eventId")
    suspend fun markSecurityEventResolved(eventId: Long)

    @Query("DELETE FROM security_events")
    suspend fun clearAllSecurityEvents()
}

@Database(
    entities = [
        DeviceEntity::class,
        SpeedTestRecordEntity::class,
        SecurityEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NetPulseDatabase : RoomDatabase() {
    abstract fun dao(): NetPulseDao

    companion object {
        @Volatile
        private var INSTANCE: NetPulseDatabase? = null

        fun getDatabase(context: Context): NetPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetPulseDatabase::class.java,
                    "netpulse_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
