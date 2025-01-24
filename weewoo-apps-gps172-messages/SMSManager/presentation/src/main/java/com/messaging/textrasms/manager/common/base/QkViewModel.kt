package com.messaging.textrasms.manager.common.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class QkViewModel<in View : QkView<State>, State>(initialState: State) : ViewModel() {

    protected val disposables = CompositeDisposable()
    protected val state: Subject<State> = BehaviorSubject.createDefault(initialState)

    private val stateReducer: Subject<State.() -> State> = PublishSubject.create()

    init {
        // If we accidentally push a realm object into the state on the wrong thread, switching
        // to mainThread right here should immediately alert us of the issue
        try {


            disposables += stateReducer
                .observeOn(AndroidSchedulers.mainThread())
                .scan(initialState) { state, reducer -> reducer(state) }
                .subscribe(state::onNext)
        } catch (e: IllegalStateException) {

        }
    }

    @CallSuper
    open fun bindView(view: View) {
        state
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(view.scope())
            .subscribe(view::render)
    }

    fun ondestroy() {
        disposables.dispose()
    }

    open fun moveCompose(){

    }
    protected fun newState(reducer: State.() -> State) = stateReducer.onNext(reducer)

    override fun onCleared() = disposables.dispose()

}