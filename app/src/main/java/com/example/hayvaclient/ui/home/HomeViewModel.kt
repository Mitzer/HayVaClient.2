package com.example.hayvaclient.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hayvaclient.Callback.IBestDealLoadCallback
import com.example.hayvaclient.Callback.IPopularLoadCallback
import com.example.hayvaclient.Common.Common
import com.example.hayvaclient.Model.BestDealModel
import com.example.hayvaclient.Model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealLoadCallback {

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>) {
        popularListMutableLiveData!!.value = popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
       messageError.value = message
    }

    private  var popularListMutableLiveData:MutableLiveData<List<PopularCategoryModel>>?=null
    private  var bestDealListMutableLiveData:MutableLiveData<List<BestDealModel>>?=null
    private lateinit var messageError: MutableLiveData<String>
    private  var popularLoadCallbackListener:IPopularLoadCallback
    private var bestDealCallBackListener:IBestDealLoadCallback


    val bestDealList:LiveData<List<BestDealModel>>
    get() {
        if (bestDealListMutableLiveData == null)
        {
            bestDealListMutableLiveData = MutableLiveData()
            messageError = MutableLiveData()
            loadBestDealist()
        }
        return bestDealListMutableLiveData!!
    }

    private fun loadBestDealist() {
        val tempList = ArrayList<BestDealModel>()
        val bestDealRef = FirebaseDatabase.getInstance() .getReference(Common.BEST_DEAL_REF)
        bestDealRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                bestDealCallBackListener.onBestDealLoadFailed((p0.message!!))
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0!!.children)
                {
                    val model = itemSnapshot.getValue<BestDealModel>(BestDealModel::class.java)
                    tempList.add(model!!)
                }
                bestDealCallBackListener.onBestDealLoadSuccess(tempList)

            }

        })
    }


    val popularList:LiveData<List<PopularCategoryModel>>
    get() {
        if(popularListMutableLiveData == null)
        {
            popularListMutableLiveData = MutableLiveData()
            messageError = MutableLiveData()
            loadPopularList()
        }
        return popularListMutableLiveData!!
    }

    private fun loadPopularList() {
        val tempList = ArrayList<PopularCategoryModel>()
        val popularRef = FirebaseDatabase.getInstance() .getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                popularLoadCallbackListener.onPopularLoadFailed((p0.message!!))
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0!!.children)
                {
                    val model = itemSnapshot.getValue<PopularCategoryModel>(PopularCategoryModel::class.java)
                    tempList.add(model!!)
                }
                popularLoadCallbackListener.onPopularLoadSuccess(tempList)

            }

        })
    }

    init {
        popularLoadCallbackListener = this
        bestDealCallBackListener = this
    }
    override fun onBestDealLoadFailed(message: String) {
       messageError.value = message
    }

    override fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>) {
       bestDealListMutableLiveData!!.value = bestDealList
    }



}