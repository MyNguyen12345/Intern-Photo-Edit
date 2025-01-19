package com.example.photoedit.iu.album

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photoedit.utils.getAllImages
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private var subscription: CompositeDisposable = CompositeDisposable()
    private val images: MutableLiveData<List<String>> = MutableLiveData()


    private fun addDisposable(vararg ds: Disposable) {
        ds.forEach { subscription.add(it) }
    }

    fun getData(): LiveData<List<String>> {
        return images
    }


    fun handlerLoadData() {
        addDisposable(
            loadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { list ->
                        images.value = list
                    },
                    {

                    }
                )
        )
    }

    private fun loadData(): Observable<List<String>> {
        return Observable.create { emitter ->
            try {
                val list = getAllImages(getApplication())
                emitter.onNext(list)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }


    override fun onCleared() {
        subscription.clear()
        super.onCleared()
    }
}
