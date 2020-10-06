package com.example.hayvaclient.Callback

import com.example.hayvaclient.Model.CategoryModel
import com.example.hayvaclient.Model.PopularCategoryModel

interface ICategoryCallbackListener {
    fun onCategoryLoadSuccess(categoriesList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}