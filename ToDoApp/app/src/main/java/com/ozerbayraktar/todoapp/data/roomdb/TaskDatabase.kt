package com.ozerbayraktar.todoapp.data.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ozerbayraktar.todoapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities =  [Task::class], version = 1)
abstract class TaskDatabase :RoomDatabase(){

    abstract fun taskDao(): TaskDao


    class Callback @Inject constructor(
        private val database:Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope:CoroutineScope
    ) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao=database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Break The Dishes","Bulaşıkları yıkamak için öncelikle su ve deterjanın olmalı vs vs."))
                dao.insert(Task("Alala The car","Arabayı yıkamak için öncelikle su ve deterjanın olmalı vs vs."))

            }
        }
    }
}