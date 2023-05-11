package com.rbarlow.csc306

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val items: MutableLiveData<List<Item>> = MutableLiveData()
}
