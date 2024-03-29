package com.anangkur.movieku.consumer.feature.detail

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anangkur.movieku.consumer.BuildConfig.baseImageUrl
import com.anangkur.movieku.consumer.R
import com.anangkur.movieku.consumer.data.Repository
import com.anangkur.movieku.consumer.data.local.LocalDataSource
import com.anangkur.movieku.consumer.data.local.SharedPreferenceHelper
import com.anangkur.movieku.consumer.data.remote.RemoteDataSource
import com.anangkur.movieku.consumer.utils.Const
import com.anangkur.movieku.consumer.utils.ViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_detail.*
import com.anangkur.movieku.consumer.data.model.Result
import com.anangkur.movieku.consumer.utils.Utils

class DetailActivity: AppCompatActivity(), DetailActionListener {

    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
    }

    override fun onStart() {
        super.onStart()
        setupToolbar()
        setupViewModel()
        detailViewModel.getDataFromIntent(intent.getParcelableExtra(Const.EXTRA_DETAIL), intent.getIntExtra(Const.EXTRA_TYPE, 1))
    }

    override fun onBackPressed() {
        setResult(Const.resultCodeDetail)
        super.onBackPressed()
    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViewModel(){
        detailViewModel = ViewModelProviders.of(this, ViewModelFactory(application, Repository(
            LocalDataSource(SharedPreferenceHelper(this), this), RemoteDataSource
        )
        )
        )
            .get(DetailViewModel::class.java)
        detailViewModel.apply {
            resultLive.observe(this@DetailActivity, Observer {
                result = it
                getDataById(result.id)
            })
            successGetData.observe(this@DetailActivity, Observer {
                if (it != null){
                    Log.d("GET_DATA", "it Id: ${it.id}, favourite: ${it.favourite}")
                    setupDataToView(it)
                }else{
                    Log.d("GET_DATA", "result Id: ${result.id}, favourite: ${result.favourite}")
                    setupDataToView(result)
                }
            })
            successInsertResult.observe(this@DetailActivity, Observer {
                if (it){
                    Snackbar.make(findViewById(android.R.id.content), "${result.title?:result.name} ${resources.getString(R.string.message_success_insert)}", Snackbar.LENGTH_SHORT).show()
                    getDataById(result.id)
                }
            })
            successDeleteResult.observe(this@DetailActivity, Observer {
                if (it){
                    Snackbar.make(findViewById(android.R.id.content), "${result.title?:result.name} ${resources.getString(R.string.message_success_delete)}", Snackbar.LENGTH_SHORT).show()
                    getDataById(result.id)
                }
            })
        }
    }

    fun startActivity(context: Activity, data: Result, type: Int, requestCode: Int){
        val intent = Intent(context, DetailActivity::class.java)
            .putExtra(Const.EXTRA_DETAIL, data)
            .putExtra(Const.EXTRA_TYPE, type)
        context.startActivityForResult(intent, requestCode)
    }

    private fun setupDataToView(data: Result){
        Glide.with(this)
            .load("$baseImageUrl${data.backdrop_path?:data.poster_path}")
            .apply(RequestOptions().centerCrop())
            .apply(RequestOptions().placeholder(Utils.createCircularProgressDrawable(this)))
            .apply(RequestOptions().error(R.drawable.ic_broken_image))
            .into(iv_movie)
        tv_title.text = data.original_title?:data.original_name
        rating.rating = Utils.nomalizeRating(data.vote_average)
        tv_release_date.text = data.release_date?:"-"
        tv_language.text = data.original_language
        tv_popularity.text = data.popularity.toString()
        tv_overview.text = data.overview
        setupFavourite(data)
    }

    private fun setupFavourite(data: Result){
        if (data.favourite == Const.favouriteStateTrue){
            fab_fav.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            fab_fav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_favourite_red_24dp))
            fab_fav.setOnClickListener {this.onRemoveFavourite(data)}
        }else{
            fab_fav.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            fab_fav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_favourite_gray_24dp))
            fab_fav.setOnClickListener {this.onAddFavourite(data)}
        }
    }

    override fun onAddFavourite(data: Result) {
        Log.d("DETAIL_ACTIVITY", "add favourite type: ${data.type}")
        Log.d("DETAIL_ACTIVITY", "add favourite title: ${data.title}")
        detailViewModel.bulkInsertData(data.copy(favourite = Const.favouriteStateTrue))
    }

    override fun onRemoveFavourite(data: Result) {
        Log.d("DETAIL_ACTIVITY", "delete favourite type: ${data.type}")
        Log.d("DETAIL_ACTIVITY", "delete favourite title: ${data.title}")
        detailViewModel.deleteData(data.copy(favourite = Const.favouriteStateFalse))
    }
}