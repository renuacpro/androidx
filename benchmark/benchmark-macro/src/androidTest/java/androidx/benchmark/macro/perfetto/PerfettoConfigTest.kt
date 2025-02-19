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

package androidx.benchmark.macro.perfetto

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@SmallTest
class PerfettoConfigTest {
    @Test
    public fun ftraceBasics() {
        val ftraceDataSource =
            PERFETTO_CONFIG.data_sources.first { it.config?.name == "linux.ftrace" }

        assertNotNull(ftraceDataSource)

        val ftraceConfig = ftraceDataSource.config?.ftrace_config
        assertNotNull(ftraceConfig)

        assertEquals(listOf("*"), ftraceConfig.atrace_apps)

        assertTrue(ftraceConfig.atrace_categories.contains("view"))
        assertFalse(ftraceConfig.atrace_categories.contains("webview"))
        assertFalse(ftraceConfig.atrace_categories.contains("memory"))
    }
}
