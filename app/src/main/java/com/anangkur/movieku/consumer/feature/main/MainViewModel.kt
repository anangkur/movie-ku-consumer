package com.anangkur.movieku.consumer.feature.main

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.anangkur.movieku.consumer.data.DataSource
import com.anangkur.movieku.consumer.data.Repository
import com.anangkur.movieku.consumer.data.model.Result
import com.anangkur.movieku.consumer.utils.Const
import com.anangkur.movieku.consumer.utils.Utils

class MainViewModel(application: Application, private val repository: Repository): AndroidViewModel(application){

    val movieLive = MutableLiveData<List<Result>>()
    val showProgressGetMovie = MutableLiveData<Boolean>()
    val showErrorGetMovie = MutableLiveData<String>()

    val tvLive = MutableLiveData<List<Result>>()
    val showProgressGetTv = MutableLiveData<Boolean>()
    val showErrorGetTv = MutableLiveData<String>()

    fun getAllData(){
        repository.getAllResult(object : DataSource.ProviderCallback{
            override fun onPostExcecute() {
                showProgressGetTv.value = false
                showProgressGetMovie.value = false
            }

            override fun onPreExcecute() {
                showProgressGetTv.value = true
                showProgressGetMovie.value = true
            }
            override fun onPostExcecute(data: Cursor?) {
                showProgressGetTv.value = false
                showProgressGetMovie.value = false
                val movieDatas = ArrayList<Result>()
                val tvDatas = ArrayList<Result>()
                data?.let {
                    val listResult = Utils.convertCursorIntoList(it)
                    for (result in listResult){
                        if (result.type == Const.TYPE_MOVIE){
                            movieDatas.add(result)
                        }else{
                            tvDatas.add(result)
                        }
                    }
                    movieLive.value = movieDatas
                    tvLive.value = tvDatas
                }
            }
        })
    }
}