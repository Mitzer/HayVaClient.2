package com.example.hayvaclient.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hayvaclient.Model.FoodModel
import com.google.android.gms.common.internal.service.Common

class FoodListViewModel : ViewModel () {

    private var mutableFoodModelListData : MutableLiveData<List<FoodModel>>?=null

    fun getMutableFoodModelListData() :MutableLiveData<List<FoodModel>>{
        if (mutableFoodModelListData ==null)
             mutableFoodModelListData = MutableLiveData()
        mutableFoodModelListData!!.value = com.example.hayvaclient.Common.Common.categorySelected!!.foods
        return mutableFoodModelListData!!
    }
}