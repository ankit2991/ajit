package com.messaging.textrasms.manager.common.base

import androidx.annotation.CallSuper
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class QkPresenter<View : QkViewContract<State>, State>(initialState: State) {

    protected val disposables = CompositeDisposable()
    protected val state: Subject<State> = BehaviorSubject.createDefault(initialState)

    private val stateReducer: Subject<State.() -> State> = PublishSubject.create()

    init {
        // If we accidentally push a realm object into the state on the wrong thread, switching
        // to mainThread right here should immediately alert us of the issue
        disposables += stateReducer
            .observeOn(AndroidSchedulers.mainThread())
            .scan(initialState) { state, reducer -> reducer(state) }
            .subscribe(state::onNext)
    }

    @CallSuper
    open fun bindIntents(view: View) {
        state
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(view.scope())
            .subscribe(view::render)
    }

    protected fun newState(reducer: State.() -> State) = stateReducer.onNext(reducer)

    open fun onCleared() = disposables.dispose()

}
