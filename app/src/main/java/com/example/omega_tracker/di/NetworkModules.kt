package com.example.omega_tracker.di

import com.example.omega_tracker.data.MoshiAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
class NetWorkModules {
    @Provides
    fun provideRetrofit(): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val moshi = Moshi.Builder()
            .add(MoshiAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud")
            .addConverterFactory(MoshiConverterFactory.create(moshi)).client(client).build()
    }
}