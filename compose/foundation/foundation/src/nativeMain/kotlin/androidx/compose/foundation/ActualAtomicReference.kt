// ktlint-disable filename

/*
 * Copyright 2021 The Android Open Source Project
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
@file:Suppress("UNUSED_PARAMETER")
package androidx.compose.foundation

//class NativeAtomicReference<V>(value: V) {
//    fun get(): V =
//        TODO("implement native atomic reference get")
//    fun set(value: V): Unit =
//        TODO("implement native atomic reference set for $value")
//    fun getAndSet(value: V): V =
//        TODO("implement native atomic reference getAndSet for $value")
//    fun compareAndSet(expect: V, newValue: V): Boolean =
//        TODO("implement native atomic reference compareAndSet for $expect, $newValue")
//}

class NativeAtomicReference<V>(value: V) {
    private var v = value
    fun get(): V = v
    fun set(value: V): Unit {
        v = value
    }
    fun getAndSet(value: V): V {
        val _v = v
        v = value
        return _v
    }
    fun compareAndSet(expect: V, newValue: V): Boolean {
        if (expect == v) {
            v = newValue
            return true
        }
        return false
    }
}

internal actual typealias AtomicReference<V> = NativeAtomicReference<V>
