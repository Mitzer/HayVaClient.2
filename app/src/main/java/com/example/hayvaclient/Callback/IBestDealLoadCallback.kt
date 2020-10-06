package com.example.hayvaclient.Callback

import com.example.hayvaclient.Model.BestDealModel
import com.example.hayvaclient.Model.PopularCategoryModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList:List<BestDealModel>)
    fun onBestDealLoadFailed(message:String)
}