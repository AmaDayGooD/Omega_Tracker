package com.example.omega_tracker.di

import com.example.omega_tracker.utils.FormatTime
import dagger.Module
import dagger.Provides

@Module
class UtilsModules {
    @Provides
    fun providesFormatTime():FormatTime{
        return FormatTime
    }
}