package com.ozerbayraktar.todoapp.di

import android.app.Application
import androidx.room.Room
import com.ozerbayraktar.todoapp.data.roomdb.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides    //sağlayıcı.
    @Singleton   //injecteron yalnızca bir kere başlattığı bir türü tanımlar.
    fun provideDatabase(
        application:Application,
        callback: TaskDatabase.Callback
    )= Room.databaseBuilder(application, TaskDatabase::class.java,"task_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    fun providesTaskDao(db: TaskDatabase)=db.taskDao()


    //supervisoerjob=>bir scope içinde aynı anda gerçekleşen 2 operationdan bi chield fail olursa diğeri de cancel olur.bunu yapınca diğer child göreve devam eder.

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope()= CoroutineScope(SupervisorJob())

    }
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
