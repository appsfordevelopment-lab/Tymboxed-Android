package dev.ambitionsoftware.tymeboxed.data.repository

import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao
import dev.ambitionsoftware.tymeboxed.data.db.entities.BlockedAppEntity
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dev.ambitionsoftware.tymeboxed.domain.model.normalizedForBreaks
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
    private val sessionRepository: SessionRepository,
) {
    fun observeAll(): Flow<List<Profile>> =
        profileDao.observeAllWithApps().map { list ->
            list.map { it.toDomain().normalizedForBreaks() }
        }

    fun observeById(id: String): Flow<Profile?> =
        profileDao.observeByIdWithApps(id).map { it?.toDomain()?.normalizedForBreaks() }

    suspend fun findById(id: String): Profile? =
        profileDao.getByIdWithApps(id)?.toDomain()?.normalizedForBreaks()

    suspend fun count(): Int = profileDao.count()

    suspend fun save(profile: Profile) {
        val normalized = profile.normalizedForBreaks()
        val existing = profileDao.findById(normalized.id)
        val entity = normalized.toEntity().copy(
            updatedAt = System.currentTimeMillis(),
            createdAt = existing?.createdAt ?: normalized.createdAt,
        )
        if (existing == null) {
            profileDao.insertProfile(entity)
        } else {
            profileDao.updateProfile(entity)
        }
        profileDao.clearBlockedApps(normalized.id)
        if (normalized.blockedPackages.isNotEmpty()) {
            profileDao.insertBlockedApps(
                normalized.blockedPackages.map { pkg ->
                    BlockedAppEntity(profileId = normalized.id, packageName = pkg)
                },
            )
        }
    }

    suspend fun delete(id: String) {
        val active = sessionRepository.findActive()
        if (active != null && active.profileId == id) {
            error(
                "Cannot delete this profile while its focus session is active. End the session first.",
            )
        }
        val entity = profileDao.findById(id) ?: return
        profileDao.deleteProfile(entity)
    }

    suspend fun deleteAll() {
        profileDao.deleteAll()
    }
}
