package dev.ambitionsoftware.tymeboxed.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.ambitionsoftware.tymeboxed.data.db.ProfileWithApps
import dev.ambitionsoftware.tymeboxed.data.db.entities.BlockedAppEntity
import dev.ambitionsoftware.tymeboxed.data.db.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Transaction
    @Query("SELECT * FROM profiles ORDER BY `order` ASC, createdAt DESC")
    fun observeAllWithApps(): Flow<List<ProfileWithApps>>

    @Transaction
    @Query("SELECT * FROM profiles ORDER BY `order` ASC, createdAt DESC")
    suspend fun getAllWithAppsSnapshot(): List<ProfileWithApps>

    @Transaction
    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun observeByIdWithApps(id: String): Flow<ProfileWithApps?>

    @Transaction
    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getByIdWithApps(id: String): ProfileWithApps?

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProfileEntity?

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApps(apps: List<BlockedAppEntity>)

    @Query("DELETE FROM blocked_apps WHERE profileId = :profileId")
    suspend fun clearBlockedApps(profileId: String)
}
