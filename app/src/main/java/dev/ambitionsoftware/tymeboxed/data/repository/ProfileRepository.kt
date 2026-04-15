package dev.ambitionsoftware.tymeboxed.data.repository

import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao
import dev.ambitionsoftware.tymeboxed.data.db.entities.BlockedAppEntity
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dev.ambitionsoftware.tymeboxed.domain.model.toDomain
import dev.ambitionsoftware.tymeboxed.domain.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for [Profile] + its blocked-apps rows.
 *
 * All writes go through [save], which handles insert-or-update and rewrites
 * the child `blocked_apps` rows atomically on the Room side (via the DAO's
 * cascade delete + re-insert).
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
) {
    fun observeAll(): Flow<List<Profile>> =
        profileDao.observeAllWithApps().map { list -> list.map { it.toDomain() } }

    fun observeById(id: String): Flow<Profile?> =
        profileDao.observeByIdWithApps(id).map { it?.toDomain() }

    suspend fun findById(id: String): Profile? =
        profileDao.getByIdWithApps(id)?.toDomain()

    suspend fun count(): Int = profileDao.count()

    suspend fun save(profile: Profile) {
        val existing = profileDao.findById(profile.id)
        val entity = profile.toEntity().copy(
            updatedAt = System.currentTimeMillis(),
            createdAt = existing?.createdAt ?: profile.createdAt,
        )
        if (existing == null) {
            profileDao.insertProfile(entity)
        } else {
            profileDao.updateProfile(entity)
        }
        profileDao.clearBlockedApps(profile.id)
        if (profile.blockedPackages.isNotEmpty()) {
            profileDao.insertBlockedApps(
                profile.blockedPackages.map { pkg ->
                    BlockedAppEntity(profileId = profile.id, packageName = pkg)
                },
            )
        }
    }

    suspend fun delete(id: String) {
        val entity = profileDao.findById(id) ?: return
        profileDao.deleteProfile(entity)
    }

    suspend fun deleteAll() {
        profileDao.deleteAll()
    }
}
