/*
 * Copyright (C) 2016 The Android Open Source Project
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
package android.support.v7.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CacheUtils {
    static void verifyPositionsPrefetched(RecyclerView view, int dx, int dy,
            Integer[]... positionData) {
        RecyclerView.PrefetchRegistry prefetchRegistry = mock(RecyclerView.PrefetchRegistry.class);
        view.mLayout.collectPrefetchPositions(
                dx, dy, view.mState, prefetchRegistry);

        verify(prefetchRegistry, times(positionData.length)).addPosition(anyInt(), anyInt());
        for (Integer[] aPositionData : positionData) {
            verify(prefetchRegistry).addPosition(aPositionData[0], aPositionData[1]);
        }
    }


    private static void verifyCacheContainsPosition(RecyclerView view, int position) {
        for (int i = 0; i < view.mRecycler.mCachedViews.size(); i++) {
            if (view.mRecycler.mCachedViews.get(i).mPosition == position) return;
        }
        fail("Cache does not contain position " + position);
    }

    /**
     * Asserts that the position passed is resident in the view's cache.
     */
    static void verifyCacheContainsPositions(RecyclerView view, Integer... positions) {
        for (Integer position : positions) {
            verifyCacheContainsPosition(view, position);
        }
    }

    private static void verifyCacheContainsPrefetchedPosition(RecyclerView view, int position) {
        verifyCacheContainsPosition(view, position);
        assertTrue(view.mPrefetchRegistry.lastPrefetchIncludedPosition(position));
    }

    /**
     * Asserts that the position passed is resident in the view's cache, similar to
     * {@link #verifyCacheContainsPositions}, but additionally requires presence in
     * PrefetchRegistry.
     */
    static void verifyCacheContainsPrefetchedPositions(RecyclerView view, Integer... positions) {
        for (Integer position : positions) {
            verifyCacheContainsPrefetchedPosition(view, position);
        }
        assertEquals(positions.length, view.mRecycler.mCachedViews.size());
    }


}
