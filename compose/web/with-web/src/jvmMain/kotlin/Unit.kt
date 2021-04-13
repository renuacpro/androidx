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

package androidx.compose.ui.unit.ww

import androidx.compose.ui.unit.Dp as JDp

val Dp.implementation: JDp
    get() = JDp(value)

// TODO: This has to be reduplicated here - otherwise we'll end with exception which looks like a JVM-target bug to me
val Int.dp: Dp
    get() = Dp(this.toFloat())