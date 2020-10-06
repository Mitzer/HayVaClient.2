package com.example.hayvaclient.Common

import com.example.hayvaclient.Model.CategoryModel
import com.example.hayvaclient.Model.FoodModel
import com.example.hayvaclient.Model.UserModel

object Common {
    var foodSelected: FoodModel?=null
    var categorySelected: CategoryModel?=null
    val CATEGORY_REF: String ="Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int=0
    val BEST_DEAL_REF: String="BestDeals"
    val POPULAR_REF: String="MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser:UserModel? = null
}