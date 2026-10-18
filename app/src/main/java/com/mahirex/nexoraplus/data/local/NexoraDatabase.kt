package com.mahirex.nexoraplus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahirex.nexoraplus.data.local.dao.AccountDao
import com.mahirex.nexoraplus.data.local.dao.CalendarDao
import com.mahirex.nexoraplus.data.local.dao.FocusDao
import com.mahirex.nexoraplus.data.local.dao.FortuneDao
import com.mahirex.nexoraplus.data.local.dao.GoalDao
import com.mahirex.nexoraplus.data.local.dao.HabitDao
import com.mahirex.nexoraplus.data.local.dao.ListsDao
import com.mahirex.nexoraplus.data.local.dao.NoteDao
import com.mahirex.nexoraplus.data.local.dao.OccasionDao
import com.mahirex.nexoraplus.data.local.dao.ScheduleDao
import com.mahirex.nexoraplus.data.local.dao.TaskDao
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.data.local.entity.ChecklistItemEntity
import com.mahirex.nexoraplus.data.local.entity.EventCategoryEntity
import com.mahirex.nexoraplus.data.local.entity.FocusSessionEntity
import com.mahirex.nexoraplus.data.local.entity.FortuneHistoryEntity
import com.mahirex.nexoraplus.data.local.entity.GoalEntity
import com.mahirex.nexoraplus.data.local.entity.HabitEntity
import com.mahirex.nexoraplus.data.local.entity.HabitLogEntity
import com.mahirex.nexoraplus.data.local.entity.ListItemEntity
import com.mahirex.nexoraplus.data.local.entity.NoteEntity
import com.mahirex.nexoraplus.data.local.entity.OccasionEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity
import com.mahirex.nexoraplus.data.local.entity.SchedulePresetEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleSlotEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity

@Database(
    entities = [
        AccountEntity::class,
        TaskEntity::class,
        GoalEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        ListItemEntity::class,
        ChecklistItemEntity::class,
        CalEventEntity::class,
        EventCategoryEntity::class,
        ScheduleSlotEntity::class,
        ScheduleEntryEntity::class,
        SchedulePresetEntity::class,
        FortuneHistoryEntity::class,
        OccasionEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexoraDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun listsDao(): ListsDao
    abstract fun calendarDao(): CalendarDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun fortuneDao(): FortuneDao
    abstract fun focusDao(): FocusDao
    abstract fun occasionDao(): OccasionDao

    companion object {
        @Volatile
        private var instance: NexoraDatabase? = null

        fun get(context: Context): NexoraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NexoraDatabase::class.java,
                    "nexora_plus.db"
                )
                    // v1 schema: until real Migration objects exist, a future schema bump must not
                    // crash the app at launch. Destructive fallback (factory-reset) is the safe default.
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
    }
}
