package com.example.hayvaclient.Callback

import com.example.hayvaclient.Model.PopularCategoryModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularModelList:List<PopularCategoryModel>)
    fun onPopularLoadFailed(message:String)
}