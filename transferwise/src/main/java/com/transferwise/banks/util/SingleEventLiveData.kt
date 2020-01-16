/*
 * Copyright 2019 TransferWise Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.transferwise.banks.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A modified version of LiveData that only delivers each value once to it's observer. This is
 * important for navigation events so that they don't get redelivered after an orientation change.
 *
 * E.g. with a LiveData, the observer would be notified again of a new value after an orientation
 * change, which would cause the navigation event to be triggered on the wrong screen.
 *
 * Inspired by: https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java
 */
internal class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val notifyObserver = AtomicBoolean(false)

    override fun setValue(value: T) {
        notifyObserver.set(true)
        super.setValue(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) throw RuntimeException("Only one observer supported")

        super.observe(owner, Observer<T> {
            if (notifyObserver.get()) {
                observer.onChanged(it)
            }
        })
    }
}
