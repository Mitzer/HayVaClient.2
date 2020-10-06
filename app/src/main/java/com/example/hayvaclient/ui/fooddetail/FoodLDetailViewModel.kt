package com.example.hayvaclient.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hayvaclient.Model.FoodModel
import com.google.android.gms.common.internal.service.Common

class FoodLDetailViewModel : ViewModel() {

    private var mutableLiveDataFood:MutableLiveData<FoodModel>?=null

    fun getMutableLiveDataFood() :MutableLiveData<FoodModel>{
        if (mutableLiveDataFood ==null)
            mutableLiveDataFood = MutableLiveData()
        mutableLiveDataFood!!.value = com.example.hayvaclient.Common.Common.foodSelected
        return mutableLiveDataFood!!
    }


}