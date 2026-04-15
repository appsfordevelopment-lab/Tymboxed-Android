package dev.ambitionsoftware.tymeboxed.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A tymeboxed tag that has been registered on-device. Phase 1 policy:
 * any NFC tag can start or stop a session — so this table is not
 * required for unlock, only for the "physical unblock" opt-in on a
 * profile (see [ProfileEntity.physicalUnblockNfcTagId]).
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val tagUid: String,
    val registeredAt: Long,
    val label: String? = null,
)
