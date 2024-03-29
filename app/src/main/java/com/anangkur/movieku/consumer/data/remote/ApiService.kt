package com.anangkur.movieku.consumer.data.remote

import com.anangkur.movieku.consumer.BuildConfig.baseUrl
import com.anangkur.movieku.consumer.data.model.Response
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService{

    @GET("{urlType}/{urlFilter}")
    fun getData(@Path("urlType") urlType: String,
                @Path("urlFilter") urlFilter: String,
                @Query("api_key") apiKey: String,
                @Query("page") page: Int): Observable<Response>

    @GET("search/{urlType}")
    fun getSearchData(@Path("urlType") urlType: String,
                      @Query("api_key") apiKey: String,
                      @Query("query") query: String): Observable<Response>

    companion object Factory{
        val getApiService: ApiService by lazy {

            val mClient =
                OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()

            val mRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mClient)
                .build()

            mRetrofit.create(ApiService::class.java)
        }
    }
}