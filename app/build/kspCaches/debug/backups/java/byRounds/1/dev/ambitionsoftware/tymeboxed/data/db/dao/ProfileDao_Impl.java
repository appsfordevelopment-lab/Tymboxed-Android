package dev.ambitionsoftware.tymeboxed.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import dev.ambitionsoftware.tymeboxed.data.db.ProfileWithApps;
import dev.ambitionsoftware.tymeboxed.data.db.entities.BlockedAppEntity;
import dev.ambitionsoftware.tymeboxed.data.db.entities.ProfileEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProfileDao_Impl implements ProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProfileEntity> __insertionAdapterOfProfileEntity;

  private final EntityInsertionAdapter<BlockedAppEntity> __insertionAdapterOfBlockedAppEntity;

  private final EntityDeletionOrUpdateAdapter<ProfileEntity> __deletionAdapterOfProfileEntity;

  private final EntityDeletionOrUpdateAdapter<ProfileEntity> __updateAdapterOfProfileEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final SharedSQLiteStatement __preparedStmtOfClearBlockedApps;

  public ProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProfileEntity = new EntityInsertionAdapter<ProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `profiles` (`id`,`name`,`createdAt`,`updatedAt`,`strategyId`,`strategyData`,`enableStrictMode`,`enableLiveActivity`,`enableBreaks`,`breakTimeInMinutes`,`reminderTimeSeconds`,`customReminderMessage`,`physicalUnblockNfcTagId`,`isAllowMode`,`isAllowModeDomains`,`domains`,`order`,`accentColorHex`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getUpdatedAt());
        statement.bindString(5, entity.getStrategyId());
        if (entity.getStrategyData() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStrategyData());
        }
        final int _tmp = entity.getEnableStrictMode() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getEnableLiveActivity() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.getEnableBreaks() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getBreakTimeInMinutes());
        if (entity.getReminderTimeSeconds() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getReminderTimeSeconds());
        }
        if (entity.getCustomReminderMessage() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getCustomReminderMessage());
        }
        if (entity.getPhysicalUnblockNfcTagId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getPhysicalUnblockNfcTagId());
        }
        final int _tmp_3 = entity.isAllowMode() ? 1 : 0;
        statement.bindLong(14, _tmp_3);
        final int _tmp_4 = entity.isAllowModeDomains() ? 1 : 0;
        statement.bindLong(15, _tmp_4);
        if (entity.getDomains() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getDomains());
        }
        statement.bindLong(17, entity.getOrder());
        if (entity.getAccentColorHex() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getAccentColorHex());
        }
      }
    };
    this.__insertionAdapterOfBlockedAppEntity = new EntityInsertionAdapter<BlockedAppEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `blocked_apps` (`id`,`profileId`,`packageName`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockedAppEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getProfileId());
        statement.bindString(3, entity.getPackageName());
      }
    };
    this.__deletionAdapterOfProfileEntity = new EntityDeletionOrUpdateAdapter<ProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `profiles` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfProfileEntity = new EntityDeletionOrUpdateAdapter<ProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `profiles` SET `id` = ?,`name` = ?,`createdAt` = ?,`updatedAt` = ?,`strategyId` = ?,`strategyData` = ?,`enableStrictMode` = ?,`enableLiveActivity` = ?,`enableBreaks` = ?,`breakTimeInMinutes` = ?,`reminderTimeSeconds` = ?,`customReminderMessage` = ?,`physicalUnblockNfcTagId` = ?,`isAllowMode` = ?,`isAllowModeDomains` = ?,`domains` = ?,`order` = ?,`accentColorHex` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getUpdatedAt());
        statement.bindString(5, entity.getStrategyId());
        if (entity.getStrategyData() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStrategyData());
        }
        final int _tmp = entity.getEnableStrictMode() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getEnableLiveActivity() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.getEnableBreaks() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getBreakTimeInMinutes());
        if (entity.getReminderTimeSeconds() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getReminderTimeSeconds());
        }
        if (entity.getCustomReminderMessage() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getCustomReminderMessage());
        }
        if (entity.getPhysicalUnblockNfcTagId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getPhysicalUnblockNfcTagId());
        }
        final int _tmp_3 = entity.isAllowMode() ? 1 : 0;
        statement.bindLong(14, _tmp_3);
        final int _tmp_4 = entity.isAllowModeDomains() ? 1 : 0;
        statement.bindLong(15, _tmp_4);
        if (entity.getDomains() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getDomains());
        }
        statement.bindLong(17, entity.getOrder());
        if (entity.getAccentColorHex() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getAccentColorHex());
        }
        statement.bindString(19, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM profiles";
        return _query;
      }
    };
    this.__preparedStmtOfClearBlockedApps = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM blocked_apps WHERE profileId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProfile(final ProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertBlockedApps(final List<BlockedAppEntity> apps,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBlockedAppEntity.insert(apps);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProfile(final ProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProfileEntity.handle(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProfile(final ProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProfileEntity.handle(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearBlockedApps(final String profileId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearBlockedApps.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, profileId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearBlockedApps.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ProfileWithApps>> observeAllWithApps() {
    final String _sql = "SELECT * FROM profiles ORDER BY `order` ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"blocked_apps",
        "profiles"}, new Callable<List<ProfileWithApps>>() {
      @Override
      @NonNull
      public List<ProfileWithApps> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final int _cursorIndexOfStrategyId = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyId");
            final int _cursorIndexOfStrategyData = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyData");
            final int _cursorIndexOfEnableStrictMode = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStrictMode");
            final int _cursorIndexOfEnableLiveActivity = CursorUtil.getColumnIndexOrThrow(_cursor, "enableLiveActivity");
            final int _cursorIndexOfEnableBreaks = CursorUtil.getColumnIndexOrThrow(_cursor, "enableBreaks");
            final int _cursorIndexOfBreakTimeInMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "breakTimeInMinutes");
            final int _cursorIndexOfReminderTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTimeSeconds");
            final int _cursorIndexOfCustomReminderMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "customReminderMessage");
            final int _cursorIndexOfPhysicalUnblockNfcTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "physicalUnblockNfcTagId");
            final int _cursorIndexOfIsAllowMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowMode");
            final int _cursorIndexOfIsAllowModeDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowModeDomains");
            final int _cursorIndexOfDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "domains");
            final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
            final int _cursorIndexOfAccentColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "accentColorHex");
            final ArrayMap<String, ArrayList<BlockedAppEntity>> _collectionBlockedApps = new ArrayMap<String, ArrayList<BlockedAppEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionBlockedApps.containsKey(_tmpKey)) {
                _collectionBlockedApps.put(_tmpKey, new ArrayList<BlockedAppEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipblockedAppsAsdevAmbitionsoftwareTymeboxedDataDbEntitiesBlockedAppEntity(_collectionBlockedApps);
            final List<ProfileWithApps> _result = new ArrayList<ProfileWithApps>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final ProfileWithApps _item;
              final ProfileEntity _tmpProfile;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              final String _tmpStrategyId;
              _tmpStrategyId = _cursor.getString(_cursorIndexOfStrategyId);
              final String _tmpStrategyData;
              if (_cursor.isNull(_cursorIndexOfStrategyData)) {
                _tmpStrategyData = null;
              } else {
                _tmpStrategyData = _cursor.getString(_cursorIndexOfStrategyData);
              }
              final boolean _tmpEnableStrictMode;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfEnableStrictMode);
              _tmpEnableStrictMode = _tmp != 0;
              final boolean _tmpEnableLiveActivity;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfEnableLiveActivity);
              _tmpEnableLiveActivity = _tmp_1 != 0;
              final boolean _tmpEnableBreaks;
              final int _tmp_2;
              _tmp_2 = _cursor.getInt(_cursorIndexOfEnableBreaks);
              _tmpEnableBreaks = _tmp_2 != 0;
              final int _tmpBreakTimeInMinutes;
              _tmpBreakTimeInMinutes = _cursor.getInt(_cursorIndexOfBreakTimeInMinutes);
              final Integer _tmpReminderTimeSeconds;
              if (_cursor.isNull(_cursorIndexOfReminderTimeSeconds)) {
                _tmpReminderTimeSeconds = null;
              } else {
                _tmpReminderTimeSeconds = _cursor.getInt(_cursorIndexOfReminderTimeSeconds);
              }
              final String _tmpCustomReminderMessage;
              if (_cursor.isNull(_cursorIndexOfCustomReminderMessage)) {
                _tmpCustomReminderMessage = null;
              } else {
                _tmpCustomReminderMessage = _cursor.getString(_cursorIndexOfCustomReminderMessage);
              }
              final String _tmpPhysicalUnblockNfcTagId;
              if (_cursor.isNull(_cursorIndexOfPhysicalUnblockNfcTagId)) {
                _tmpPhysicalUnblockNfcTagId = null;
              } else {
                _tmpPhysicalUnblockNfcTagId = _cursor.getString(_cursorIndexOfPhysicalUnblockNfcTagId);
              }
              final boolean _tmpIsAllowMode;
              final int _tmp_3;
              _tmp_3 = _cursor.getInt(_cursorIndexOfIsAllowMode);
              _tmpIsAllowMode = _tmp_3 != 0;
              final boolean _tmpIsAllowModeDomains;
              final int _tmp_4;
              _tmp_4 = _cursor.getInt(_cursorIndexOfIsAllowModeDomains);
              _tmpIsAllowModeDomains = _tmp_4 != 0;
              final String _tmpDomains;
              if (_cursor.isNull(_cursorIndexOfDomains)) {
                _tmpDomains = null;
              } else {
                _tmpDomains = _cursor.getString(_cursorIndexOfDomains);
              }
              final int _tmpOrder;
              _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
              final String _tmpAccentColorHex;
              if (_cursor.isNull(_cursorIndexOfAccentColorHex)) {
                _tmpAccentColorHex = null;
              } else {
                _tmpAccentColorHex = _cursor.getString(_cursorIndexOfAccentColorHex);
              }
              _tmpProfile = new ProfileEntity(_tmpId,_tmpName,_tmpCreatedAt,_tmpUpdatedAt,_tmpStrategyId,_tmpStrategyData,_tmpEnableStrictMode,_tmpEnableLiveActivity,_tmpEnableBreaks,_tmpBreakTimeInMinutes,_tmpReminderTimeSeconds,_tmpCustomReminderMessage,_tmpPhysicalUnblockNfcTagId,_tmpIsAllowMode,_tmpIsAllowModeDomains,_tmpDomains,_tmpOrder,_tmpAccentColorHex);
              final ArrayList<BlockedAppEntity> _tmpBlockedAppsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpBlockedAppsCollection = _collectionBlockedApps.get(_tmpKey_1);
              _item = new ProfileWithApps(_tmpProfile,_tmpBlockedAppsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<ProfileWithApps> observeByIdWithApps(final String id) {
    final String _sql = "SELECT * FROM profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"blocked_apps",
        "profiles"}, new Callable<ProfileWithApps>() {
      @Override
      @Nullable
      public ProfileWithApps call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final int _cursorIndexOfStrategyId = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyId");
            final int _cursorIndexOfStrategyData = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyData");
            final int _cursorIndexOfEnableStrictMode = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStrictMode");
            final int _cursorIndexOfEnableLiveActivity = CursorUtil.getColumnIndexOrThrow(_cursor, "enableLiveActivity");
            final int _cursorIndexOfEnableBreaks = CursorUtil.getColumnIndexOrThrow(_cursor, "enableBreaks");
            final int _cursorIndexOfBreakTimeInMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "breakTimeInMinutes");
            final int _cursorIndexOfReminderTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTimeSeconds");
            final int _cursorIndexOfCustomReminderMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "customReminderMessage");
            final int _cursorIndexOfPhysicalUnblockNfcTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "physicalUnblockNfcTagId");
            final int _cursorIndexOfIsAllowMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowMode");
            final int _cursorIndexOfIsAllowModeDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowModeDomains");
            final int _cursorIndexOfDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "domains");
            final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
            final int _cursorIndexOfAccentColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "accentColorHex");
            final ArrayMap<String, ArrayList<BlockedAppEntity>> _collectionBlockedApps = new ArrayMap<String, ArrayList<BlockedAppEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionBlockedApps.containsKey(_tmpKey)) {
                _collectionBlockedApps.put(_tmpKey, new ArrayList<BlockedAppEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipblockedAppsAsdevAmbitionsoftwareTymeboxedDataDbEntitiesBlockedAppEntity(_collectionBlockedApps);
            final ProfileWithApps _result;
            if (_cursor.moveToFirst()) {
              final ProfileEntity _tmpProfile;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              final String _tmpStrategyId;
              _tmpStrategyId = _cursor.getString(_cursorIndexOfStrategyId);
              final String _tmpStrategyData;
              if (_cursor.isNull(_cursorIndexOfStrategyData)) {
                _tmpStrategyData = null;
              } else {
                _tmpStrategyData = _cursor.getString(_cursorIndexOfStrategyData);
              }
              final boolean _tmpEnableStrictMode;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfEnableStrictMode);
              _tmpEnableStrictMode = _tmp != 0;
              final boolean _tmpEnableLiveActivity;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfEnableLiveActivity);
              _tmpEnableLiveActivity = _tmp_1 != 0;
              final boolean _tmpEnableBreaks;
              final int _tmp_2;
              _tmp_2 = _cursor.getInt(_cursorIndexOfEnableBreaks);
              _tmpEnableBreaks = _tmp_2 != 0;
              final int _tmpBreakTimeInMinutes;
              _tmpBreakTimeInMinutes = _cursor.getInt(_cursorIndexOfBreakTimeInMinutes);
              final Integer _tmpReminderTimeSeconds;
              if (_cursor.isNull(_cursorIndexOfReminderTimeSeconds)) {
                _tmpReminderTimeSeconds = null;
              } else {
                _tmpReminderTimeSeconds = _cursor.getInt(_cursorIndexOfReminderTimeSeconds);
              }
              final String _tmpCustomReminderMessage;
              if (_cursor.isNull(_cursorIndexOfCustomReminderMessage)) {
                _tmpCustomReminderMessage = null;
              } else {
                _tmpCustomReminderMessage = _cursor.getString(_cursorIndexOfCustomReminderMessage);
              }
              final String _tmpPhysicalUnblockNfcTagId;
              if (_cursor.isNull(_cursorIndexOfPhysicalUnblockNfcTagId)) {
                _tmpPhysicalUnblockNfcTagId = null;
              } else {
                _tmpPhysicalUnblockNfcTagId = _cursor.getString(_cursorIndexOfPhysicalUnblockNfcTagId);
              }
              final boolean _tmpIsAllowMode;
              final int _tmp_3;
              _tmp_3 = _cursor.getInt(_cursorIndexOfIsAllowMode);
              _tmpIsAllowMode = _tmp_3 != 0;
              final boolean _tmpIsAllowModeDomains;
              final int _tmp_4;
              _tmp_4 = _cursor.getInt(_cursorIndexOfIsAllowModeDomains);
              _tmpIsAllowModeDomains = _tmp_4 != 0;
              final String _tmpDomains;
              if (_cursor.isNull(_cursorIndexOfDomains)) {
                _tmpDomains = null;
              } else {
                _tmpDomains = _cursor.getString(_cursorIndexOfDomains);
              }
              final int _tmpOrder;
              _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
              final String _tmpAccentColorHex;
              if (_cursor.isNull(_cursorIndexOfAccentColorHex)) {
                _tmpAccentColorHex = null;
              } else {
                _tmpAccentColorHex = _cursor.getString(_cursorIndexOfAccentColorHex);
              }
              _tmpProfile = new ProfileEntity(_tmpId,_tmpName,_tmpCreatedAt,_tmpUpdatedAt,_tmpStrategyId,_tmpStrategyData,_tmpEnableStrictMode,_tmpEnableLiveActivity,_tmpEnableBreaks,_tmpBreakTimeInMinutes,_tmpReminderTimeSeconds,_tmpCustomReminderMessage,_tmpPhysicalUnblockNfcTagId,_tmpIsAllowMode,_tmpIsAllowModeDomains,_tmpDomains,_tmpOrder,_tmpAccentColorHex);
              final ArrayList<BlockedAppEntity> _tmpBlockedAppsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpBlockedAppsCollection = _collectionBlockedApps.get(_tmpKey_1);
              _result = new ProfileWithApps(_tmpProfile,_tmpBlockedAppsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByIdWithApps(final String id,
      final Continuation<? super ProfileWithApps> $completion) {
    final String _sql = "SELECT * FROM profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<ProfileWithApps>() {
      @Override
      @Nullable
      public ProfileWithApps call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final int _cursorIndexOfStrategyId = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyId");
            final int _cursorIndexOfStrategyData = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyData");
            final int _cursorIndexOfEnableStrictMode = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStrictMode");
            final int _cursorIndexOfEnableLiveActivity = CursorUtil.getColumnIndexOrThrow(_cursor, "enableLiveActivity");
            final int _cursorIndexOfEnableBreaks = CursorUtil.getColumnIndexOrThrow(_cursor, "enableBreaks");
            final int _cursorIndexOfBreakTimeInMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "breakTimeInMinutes");
            final int _cursorIndexOfReminderTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTimeSeconds");
            final int _cursorIndexOfCustomReminderMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "customReminderMessage");
            final int _cursorIndexOfPhysicalUnblockNfcTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "physicalUnblockNfcTagId");
            final int _cursorIndexOfIsAllowMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowMode");
            final int _cursorIndexOfIsAllowModeDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowModeDomains");
            final int _cursorIndexOfDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "domains");
            final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
            final int _cursorIndexOfAccentColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "accentColorHex");
            final ArrayMap<String, ArrayList<BlockedAppEntity>> _collectionBlockedApps = new ArrayMap<String, ArrayList<BlockedAppEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionBlockedApps.containsKey(_tmpKey)) {
                _collectionBlockedApps.put(_tmpKey, new ArrayList<BlockedAppEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipblockedAppsAsdevAmbitionsoftwareTymeboxedDataDbEntitiesBlockedAppEntity(_collectionBlockedApps);
            final ProfileWithApps _result;
            if (_cursor.moveToFirst()) {
              final ProfileEntity _tmpProfile;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              final String _tmpStrategyId;
              _tmpStrategyId = _cursor.getString(_cursorIndexOfStrategyId);
              final String _tmpStrategyData;
              if (_cursor.isNull(_cursorIndexOfStrategyData)) {
                _tmpStrategyData = null;
              } else {
                _tmpStrategyData = _cursor.getString(_cursorIndexOfStrategyData);
              }
              final boolean _tmpEnableStrictMode;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfEnableStrictMode);
              _tmpEnableStrictMode = _tmp != 0;
              final boolean _tmpEnableLiveActivity;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfEnableLiveActivity);
              _tmpEnableLiveActivity = _tmp_1 != 0;
              final boolean _tmpEnableBreaks;
              final int _tmp_2;
              _tmp_2 = _cursor.getInt(_cursorIndexOfEnableBreaks);
              _tmpEnableBreaks = _tmp_2 != 0;
              final int _tmpBreakTimeInMinutes;
              _tmpBreakTimeInMinutes = _cursor.getInt(_cursorIndexOfBreakTimeInMinutes);
              final Integer _tmpReminderTimeSeconds;
              if (_cursor.isNull(_cursorIndexOfReminderTimeSeconds)) {
                _tmpReminderTimeSeconds = null;
              } else {
                _tmpReminderTimeSeconds = _cursor.getInt(_cursorIndexOfReminderTimeSeconds);
              }
              final String _tmpCustomReminderMessage;
              if (_cursor.isNull(_cursorIndexOfCustomReminderMessage)) {
                _tmpCustomReminderMessage = null;
              } else {
                _tmpCustomReminderMessage = _cursor.getString(_cursorIndexOfCustomReminderMessage);
              }
              final String _tmpPhysicalUnblockNfcTagId;
              if (_cursor.isNull(_cursorIndexOfPhysicalUnblockNfcTagId)) {
                _tmpPhysicalUnblockNfcTagId = null;
              } else {
                _tmpPhysicalUnblockNfcTagId = _cursor.getString(_cursorIndexOfPhysicalUnblockNfcTagId);
              }
              final boolean _tmpIsAllowMode;
              final int _tmp_3;
              _tmp_3 = _cursor.getInt(_cursorIndexOfIsAllowMode);
              _tmpIsAllowMode = _tmp_3 != 0;
              final boolean _tmpIsAllowModeDomains;
              final int _tmp_4;
              _tmp_4 = _cursor.getInt(_cursorIndexOfIsAllowModeDomains);
              _tmpIsAllowModeDomains = _tmp_4 != 0;
              final String _tmpDomains;
              if (_cursor.isNull(_cursorIndexOfDomains)) {
                _tmpDomains = null;
              } else {
                _tmpDomains = _cursor.getString(_cursorIndexOfDomains);
              }
              final int _tmpOrder;
              _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
              final String _tmpAccentColorHex;
              if (_cursor.isNull(_cursorIndexOfAccentColorHex)) {
                _tmpAccentColorHex = null;
              } else {
                _tmpAccentColorHex = _cursor.getString(_cursorIndexOfAccentColorHex);
              }
              _tmpProfile = new ProfileEntity(_tmpId,_tmpName,_tmpCreatedAt,_tmpUpdatedAt,_tmpStrategyId,_tmpStrategyData,_tmpEnableStrictMode,_tmpEnableLiveActivity,_tmpEnableBreaks,_tmpBreakTimeInMinutes,_tmpReminderTimeSeconds,_tmpCustomReminderMessage,_tmpPhysicalUnblockNfcTagId,_tmpIsAllowMode,_tmpIsAllowModeDomains,_tmpDomains,_tmpOrder,_tmpAccentColorHex);
              final ArrayList<BlockedAppEntity> _tmpBlockedAppsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpBlockedAppsCollection = _collectionBlockedApps.get(_tmpKey_1);
              _result = new ProfileWithApps(_tmpProfile,_tmpBlockedAppsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object findById(final String id, final Continuation<? super ProfileEntity> $completion) {
    final String _sql = "SELECT * FROM profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProfileEntity>() {
      @Override
      @Nullable
      public ProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfStrategyId = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyId");
          final int _cursorIndexOfStrategyData = CursorUtil.getColumnIndexOrThrow(_cursor, "strategyData");
          final int _cursorIndexOfEnableStrictMode = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStrictMode");
          final int _cursorIndexOfEnableLiveActivity = CursorUtil.getColumnIndexOrThrow(_cursor, "enableLiveActivity");
          final int _cursorIndexOfEnableBreaks = CursorUtil.getColumnIndexOrThrow(_cursor, "enableBreaks");
          final int _cursorIndexOfBreakTimeInMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "breakTimeInMinutes");
          final int _cursorIndexOfReminderTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTimeSeconds");
          final int _cursorIndexOfCustomReminderMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "customReminderMessage");
          final int _cursorIndexOfPhysicalUnblockNfcTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "physicalUnblockNfcTagId");
          final int _cursorIndexOfIsAllowMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowMode");
          final int _cursorIndexOfIsAllowModeDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowModeDomains");
          final int _cursorIndexOfDomains = CursorUtil.getColumnIndexOrThrow(_cursor, "domains");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfAccentColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "accentColorHex");
          final ProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpStrategyId;
            _tmpStrategyId = _cursor.getString(_cursorIndexOfStrategyId);
            final String _tmpStrategyData;
            if (_cursor.isNull(_cursorIndexOfStrategyData)) {
              _tmpStrategyData = null;
            } else {
              _tmpStrategyData = _cursor.getString(_cursorIndexOfStrategyData);
            }
            final boolean _tmpEnableStrictMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnableStrictMode);
            _tmpEnableStrictMode = _tmp != 0;
            final boolean _tmpEnableLiveActivity;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEnableLiveActivity);
            _tmpEnableLiveActivity = _tmp_1 != 0;
            final boolean _tmpEnableBreaks;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnableBreaks);
            _tmpEnableBreaks = _tmp_2 != 0;
            final int _tmpBreakTimeInMinutes;
            _tmpBreakTimeInMinutes = _cursor.getInt(_cursorIndexOfBreakTimeInMinutes);
            final Integer _tmpReminderTimeSeconds;
            if (_cursor.isNull(_cursorIndexOfReminderTimeSeconds)) {
              _tmpReminderTimeSeconds = null;
            } else {
              _tmpReminderTimeSeconds = _cursor.getInt(_cursorIndexOfReminderTimeSeconds);
            }
            final String _tmpCustomReminderMessage;
            if (_cursor.isNull(_cursorIndexOfCustomReminderMessage)) {
              _tmpCustomReminderMessage = null;
            } else {
              _tmpCustomReminderMessage = _cursor.getString(_cursorIndexOfCustomReminderMessage);
            }
            final String _tmpPhysicalUnblockNfcTagId;
            if (_cursor.isNull(_cursorIndexOfPhysicalUnblockNfcTagId)) {
              _tmpPhysicalUnblockNfcTagId = null;
            } else {
              _tmpPhysicalUnblockNfcTagId = _cursor.getString(_cursorIndexOfPhysicalUnblockNfcTagId);
            }
            final boolean _tmpIsAllowMode;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsAllowMode);
            _tmpIsAllowMode = _tmp_3 != 0;
            final boolean _tmpIsAllowModeDomains;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsAllowModeDomains);
            _tmpIsAllowModeDomains = _tmp_4 != 0;
            final String _tmpDomains;
            if (_cursor.isNull(_cursorIndexOfDomains)) {
              _tmpDomains = null;
            } else {
              _tmpDomains = _cursor.getString(_cursorIndexOfDomains);
            }
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpAccentColorHex;
            if (_cursor.isNull(_cursorIndexOfAccentColorHex)) {
              _tmpAccentColorHex = null;
            } else {
              _tmpAccentColorHex = _cursor.getString(_cursorIndexOfAccentColorHex);
            }
            _result = new ProfileEntity(_tmpId,_tmpName,_tmpCreatedAt,_tmpUpdatedAt,_tmpStrategyId,_tmpStrategyData,_tmpEnableStrictMode,_tmpEnableLiveActivity,_tmpEnableBreaks,_tmpBreakTimeInMinutes,_tmpReminderTimeSeconds,_tmpCustomReminderMessage,_tmpPhysicalUnblockNfcTagId,_tmpIsAllowMode,_tmpIsAllowModeDomains,_tmpDomains,_tmpOrder,_tmpAccentColorHex);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM profiles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipblockedAppsAsdevAmbitionsoftwareTymeboxedDataDbEntitiesBlockedAppEntity(
      @NonNull final ArrayMap<String, ArrayList<BlockedAppEntity>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchArrayMap(_map, true, (map) -> {
        __fetchRelationshipblockedAppsAsdevAmbitionsoftwareTymeboxedDataDbEntitiesBlockedAppEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`profileId`,`packageName` FROM `blocked_apps` WHERE `profileId` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      _stmt.bindString(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "profileId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfProfileId = 1;
      final int _cursorIndexOfPackageName = 2;
      while (_cursor.moveToNext()) {
        final String _tmpKey;
        _tmpKey = _cursor.getString(_itemKeyIndex);
        final ArrayList<BlockedAppEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final BlockedAppEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final String _tmpProfileId;
          _tmpProfileId = _cursor.getString(_cursorIndexOfProfileId);
          final String _tmpPackageName;
          _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
          _item_1 = new BlockedAppEntity(_tmpId,_tmpProfileId,_tmpPackageName);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
