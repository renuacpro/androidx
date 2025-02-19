/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.wear.watchface.style

import androidx.wear.watchface.style.UserStyleSetting.BooleanUserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.CustomValueUserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.CustomValueUserStyleSetting.CustomValueOption
import androidx.wear.watchface.style.UserStyleSetting.DoubleRangeUserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.LongRangeUserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.Option
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(StyleTestRunner::class)
public class CurrentUserStyleRepositoryTest {
    private val redStyleOption =
        ListUserStyleSetting.ListOption(Option.Id("red_style"), "Red", icon = null)

    private val greenStyleOption =
        ListUserStyleSetting.ListOption(Option.Id("green_style"), "Green", icon = null)

    private val blueStyleOption =
        ListUserStyleSetting.ListOption(Option.Id("bluestyle"), "Blue", icon = null)

    private val colorStyleList = listOf(redStyleOption, greenStyleOption, blueStyleOption)

    private val colorStyleSetting = ListUserStyleSetting(
        UserStyleSetting.Id("color_style_setting"),
        "Colors",
        "Watchface colorization", /* icon = */
        null,
        colorStyleList,
        listOf(WatchFaceLayer.BASE)
    )

    private val classicStyleOption =
        ListUserStyleSetting.ListOption(Option.Id("classic_style"), "Classic", icon = null)

    private val modernStyleOption =
        ListUserStyleSetting.ListOption(Option.Id("modern_style"), "Modern", icon = null)

    private val gothicStyleOption =
        ListUserStyleSetting.ListOption(Option.Id("gothic_style"), "Gothic", icon = null)

    private val watchHandStyleList =
        listOf(classicStyleOption, modernStyleOption, gothicStyleOption)

    private val watchHandStyleSetting = ListUserStyleSetting(
        UserStyleSetting.Id("hand_style_setting"),
        "Hand Style",
        "Hand visual look", /* icon = */
        null,
        watchHandStyleList,
        listOf(WatchFaceLayer.COMPLICATIONS_OVERLAY)
    )
    private val watchHandLengthStyleSetting =
        DoubleRangeUserStyleSetting(
            UserStyleSetting.Id("watch_hand_length_style_setting"),
            "Hand length",
            "Scale of watch hands",
            null,
            0.25,
            1.0,
            listOf(WatchFaceLayer.COMPLICATIONS_OVERLAY),
            0.75
        )

    private val mockListener1 =
        Mockito.mock(CurrentUserStyleRepository.UserStyleChangeListener::class.java)
    private val mockListener2 =
        Mockito.mock(CurrentUserStyleRepository.UserStyleChangeListener::class.java)
    private val mockListener3 =
        Mockito.mock(CurrentUserStyleRepository.UserStyleChangeListener::class.java)

    private val userStyleRepository =
        CurrentUserStyleRepository(
            UserStyleSchema(
                listOf(colorStyleSetting, watchHandStyleSetting, watchHandLengthStyleSetting)
            )
        )

    @Test
    public fun addUserStyleListener_firesImmediately() {
        userStyleRepository.addUserStyleChangeListener(mockListener1)
        Mockito.verify(mockListener1).onUserStyleChanged(userStyleRepository.userStyle)
    }

    @Test
    public fun assigning_userStyle_firesListeners() {
        userStyleRepository.addUserStyleChangeListener(mockListener1)
        userStyleRepository.addUserStyleChangeListener(mockListener2)
        userStyleRepository.addUserStyleChangeListener(mockListener3)

        Mockito.verify(mockListener1).onUserStyleChanged(userStyleRepository.userStyle)
        Mockito.verify(mockListener2).onUserStyleChanged(userStyleRepository.userStyle)
        Mockito.verify(mockListener3).onUserStyleChanged(userStyleRepository.userStyle)

        val newStyle = UserStyle(
            hashMapOf(
                colorStyleSetting to greenStyleOption,
                watchHandStyleSetting to gothicStyleOption
            )
        )

        Mockito.reset(mockListener1)
        Mockito.reset(mockListener2)
        Mockito.reset(mockListener3)

        userStyleRepository.userStyle = newStyle

        Mockito.verify(mockListener1).onUserStyleChanged(userStyleRepository.userStyle)
        Mockito.verify(mockListener2).onUserStyleChanged(userStyleRepository.userStyle)
        Mockito.verify(mockListener3).onUserStyleChanged(userStyleRepository.userStyle)
    }

    @Test
    public fun assigning_userStyle() {
        val newStyle = UserStyle(
            hashMapOf(
                colorStyleSetting to greenStyleOption,
                watchHandStyleSetting to gothicStyleOption
            )
        )

        userStyleRepository.userStyle = newStyle

        assertThat(userStyleRepository.userStyle.selectedOptions[colorStyleSetting])
            .isEqualTo(greenStyleOption)
        assertThat(userStyleRepository.userStyle.selectedOptions[watchHandStyleSetting])
            .isEqualTo(gothicStyleOption)
    }

    @Test
    public fun assign_userStyle_with_distinctButMatchingRefs() {
        val colorStyleSetting2 = ListUserStyleSetting(
            UserStyleSetting.Id("color_style_setting"),
            "Colors",
            "Watchface colorization", /* icon = */
            null,
            colorStyleList,
            listOf(WatchFaceLayer.BASE)
        )
        val watchHandStyleSetting2 = ListUserStyleSetting(
            UserStyleSetting.Id("hand_style_setting"),
            "Hand Style",
            "Hand visual look", /* icon = */
            null,
            watchHandStyleList,
            listOf(WatchFaceLayer.COMPLICATIONS_OVERLAY)
        )

        val newStyle = UserStyle(
            hashMapOf(
                colorStyleSetting2 to greenStyleOption,
                watchHandStyleSetting2 to gothicStyleOption
            )
        )

        userStyleRepository.userStyle = newStyle

        assertThat(userStyleRepository.userStyle.selectedOptions[colorStyleSetting])
            .isEqualTo(greenStyleOption)
        assertThat(userStyleRepository.userStyle.selectedOptions[watchHandStyleSetting])
            .isEqualTo(gothicStyleOption)
    }

    @Test
    public fun defaultValues() {
        val watchHandLengthOption =
            userStyleRepository.userStyle.selectedOptions[watchHandLengthStyleSetting]!! as
                DoubleRangeUserStyleSetting.DoubleRangeOption
        assertThat(watchHandLengthOption.value).isEqualTo(0.75)
    }

    @Test
    public fun userStyle_mapConstructor() {
        val userStyle = UserStyle(
            UserStyleData(
                mapOf(
                    "color_style_setting" to "bluestyle".encodeToByteArray(),
                    "hand_style_setting" to "gothic_style".encodeToByteArray()
                )
            ),
            userStyleRepository.schema
        )

        assertThat(userStyle.selectedOptions[colorStyleSetting]!!.id.value.decodeToString())
            .isEqualTo("bluestyle")
        assertThat(userStyle.selectedOptions[watchHandStyleSetting]!!.id.value.decodeToString())
            .isEqualTo("gothic_style")
    }

    @Test
    public fun userStyle_mapConstructor_badColorStyle() {
        val userStyle = UserStyle(
            UserStyleData(
                mapOf(
                    "color_style_setting" to "I DO NOT EXIST".encodeToByteArray(),
                    "hand_style_setting" to "gothic_style".encodeToByteArray()
                )
            ),
            userStyleRepository.schema
        )

        assertThat(userStyle.selectedOptions[colorStyleSetting]!!.id.value.decodeToString())
            .isEqualTo("red_style")
        assertThat(userStyle.selectedOptions[watchHandStyleSetting]!!.id.value.decodeToString())
            .isEqualTo("gothic_style")
    }

    @Test
    public fun userStyle_mapConstructor_missingColorStyle() {
        val userStyle = UserStyle(
            UserStyleData(
                mapOf("hand_style_setting" to "gothic_style".encodeToByteArray())
            ),
            userStyleRepository.schema
        )

        assertThat(userStyle.selectedOptions[colorStyleSetting]!!.id.value.decodeToString())
            .isEqualTo("red_style")
        assertThat(userStyle.selectedOptions[watchHandStyleSetting]!!.id.value.decodeToString())
            .isEqualTo("gothic_style")
    }

    @Test
    public fun userStyle_mapConstructor_customValueUserStyleSetting() {
        val customStyleSetting = CustomValueUserStyleSetting(
            listOf(WatchFaceLayer.BASE),
            "default".encodeToByteArray()
        )

        val userStyleRepository = CurrentUserStyleRepository(
            UserStyleSchema(
                listOf(customStyleSetting)
            )
        )

        val userStyle = UserStyle(
            UserStyleData(
                mapOf(
                    CustomValueUserStyleSetting.CUSTOM_VALUE_USER_STYLE_SETTING_ID
                        to "TEST 123".encodeToByteArray()
                )
            ),
            userStyleRepository.schema
        )

        val customValue = userStyle.selectedOptions[customStyleSetting]!! as CustomValueOption
        assertThat(customValue.customValue.decodeToString()).isEqualTo("TEST 123")
    }

    @Test
    public fun userStyle_multiple_ComplicationsUserStyleSetting_notAllowed() {
        val customStyleSetting1 = CustomValueUserStyleSetting(
            listOf(WatchFaceLayer.BASE),
            "default".encodeToByteArray()
        )
        val customStyleSetting2 = CustomValueUserStyleSetting(
            listOf(WatchFaceLayer.BASE),
            "default".encodeToByteArray()
        )

        try {
            UserStyleSchema(
                listOf(customStyleSetting1, customStyleSetting2)
            )
            fail(
                "Constructing a UserStyleSchema with more than one " +
                    "ComplicationSlotsUserStyleSetting should fail"
            )
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    public fun userStyle_multiple_CustomValueUserStyleSettings_notAllowed() {
        val customStyleSetting1 = CustomValueUserStyleSetting(
            listOf(WatchFaceLayer.BASE),
            "default".encodeToByteArray()
        )
        val customStyleSetting2 = CustomValueUserStyleSetting(
            listOf(WatchFaceLayer.BASE),
            "default".encodeToByteArray()
        )

        try {
            UserStyleSchema(
                listOf(customStyleSetting1, customStyleSetting2)
            )
            fail(
                "Constructing a UserStyleSchema with more than one CustomValueUserStyleSetting " +
                    "should fail"
            )
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    public fun userStyle_get() {
        val userStyle = UserStyle(
            hashMapOf(
                colorStyleSetting to greenStyleOption,
                watchHandStyleSetting to gothicStyleOption
            )
        )

        assertThat(userStyle[colorStyleSetting]).isEqualTo(greenStyleOption)
        assertThat(userStyle[watchHandStyleSetting]).isEqualTo(gothicStyleOption)
    }

    @Test
    public fun userStyle_getById() {
        val userStyle = UserStyle(
            hashMapOf(
                colorStyleSetting to greenStyleOption,
                watchHandStyleSetting to gothicStyleOption
            )
        )

        assertThat(userStyle[colorStyleSetting.id]).isEqualTo(greenStyleOption)
        assertThat(userStyle[watchHandStyleSetting.id]).isEqualTo(gothicStyleOption)
    }

    @Test
    public fun userStyle_getAndSetDifferentOptionInstances() {
        val option0 = ListUserStyleSetting.ListOption(
            Option.Id("0"),
            "option 0",
            icon = null
        )
        val option1 = ListUserStyleSetting.ListOption(
            Option.Id("1"),
            "option 1",
            icon = null
        )
        val option0Copy = ListUserStyleSetting.ListOption(
            Option.Id("0"),
            "option #0",
            icon = null
        )
        val option1Copy = ListUserStyleSetting.ListOption(
            Option.Id("1"),
            "option #1",
            icon = null
        )
        val setting = ListUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting",
            "setting description",
            icon = null,
            listOf(option0, option1),
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS
        )

        val userStyle = UserStyle(mapOf(setting to option0))
        assertThat(userStyle.selectedOptions[setting]).isEqualTo(option0)
        assertThat(userStyle.selectedOptions[setting]).isEqualTo(option0Copy)

        val userStyleMap = userStyle.selectedOptions.toMutableMap()
        userStyleMap[setting] = option1
        val newUserStyle = UserStyle(userStyleMap)

        assertThat(userStyle.selectedOptions[setting]).isEqualTo(option0)
        assertThat(newUserStyle.selectedOptions[setting]).isEqualTo(option1)
        assertThat(newUserStyle.selectedOptions[setting]).isEqualTo(option1Copy)
    }

    @Test
    public fun userStyle_getDifferentSettingInstances() {
        val setting = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting",
            "setting description",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )
        val settingCopy = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting",
            "setting description",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )
        val settingModifiedInfo = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting modified",
            "setting description modified",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            false
        )
        val settingModifiedId = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting_modified"),
            "setting",
            "setting description",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )

        val optionTrue = BooleanUserStyleSetting.BooleanOption.TRUE

        val userStyle = UserStyle(mapOf(setting to optionTrue))
        assertThat(userStyle.selectedOptions[setting]).isEqualTo(optionTrue)
        assertThat(userStyle.selectedOptions[settingCopy]).isEqualTo(optionTrue)
        assertThat(userStyle.selectedOptions[settingModifiedInfo]).isEqualTo(optionTrue)
        assertThat(userStyle.selectedOptions[settingModifiedId]).isEqualTo(null)
    }

    @Test
    public fun userStyle_setDifferentSettingInstances() {
        val setting = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting",
            "setting description",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )
        val settingCopy = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting",
            "setting description",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )
        val settingModifiedInfo = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting"),
            "setting modified",
            "setting description modified",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            false
        )
        val settingModifiedId = BooleanUserStyleSetting(
            UserStyleSetting.Id("setting_modified"),
            "setting",
            "setting description",
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )

        val optionTrue = BooleanUserStyleSetting.BooleanOption.TRUE
        val optionFalse = BooleanUserStyleSetting.BooleanOption.FALSE

        val userStyle = UserStyle(mapOf(setting to optionTrue))
        assertThat(userStyle.selectedOptions[setting]).isEqualTo(optionTrue)

        UserStyle(
            userStyle.selectedOptions.toMutableMap().apply { this[setting] = optionFalse }
        ).let { newUserStyle ->
            assertThat(userStyle.selectedOptions[setting]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[setting]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingCopy]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingModifiedInfo]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingModifiedId]).isEqualTo(null)
        }

        UserStyle(
            userStyle.selectedOptions.toMutableMap().apply { this[settingCopy] = optionFalse }
        ).let { newUserStyle ->
            assertThat(userStyle.selectedOptions[setting]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[setting]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingCopy]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingModifiedInfo]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingModifiedId]).isEqualTo(null)
        }

        UserStyle(
            userStyle.selectedOptions.toMutableMap()
                .apply { this[settingModifiedInfo] = optionFalse }
        ).let { newUserStyle ->
            assertThat(userStyle.selectedOptions[setting]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[setting]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingCopy]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingModifiedInfo]).isEqualTo(optionFalse)
            assertThat(newUserStyle.selectedOptions[settingModifiedId]).isEqualTo(null)
        }

        UserStyle(
            userStyle.selectedOptions.toMutableMap().apply { this[settingModifiedId] = optionFalse }
        ).let { newUserStyle ->
            assertThat(userStyle.selectedOptions[setting]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[setting]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[settingCopy]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[settingModifiedInfo]).isEqualTo(optionTrue)
            assertThat(newUserStyle.selectedOptions[settingModifiedId]).isEqualTo(optionFalse)
        }
    }

    @Test
    public fun setAndGetCustomStyleSetting() {
        val customStyleSetting = CustomValueUserStyleSetting(
            listOf(WatchFaceLayer.BASE),
            "default".encodeToByteArray()
        )

        val userStyleRepository = CurrentUserStyleRepository(
            UserStyleSchema(
                listOf(customStyleSetting)
            )
        )

        userStyleRepository.userStyle = UserStyle(
            mapOf(
                customStyleSetting to CustomValueOption("test".encodeToByteArray())
            )
        )

        assertThat(
            (userStyleRepository.userStyle[customStyleSetting]!! as CustomValueOption)
                .customValue.decodeToString()
        ).isEqualTo("test")
    }

    @Test
    public fun userStyleData_equals() {
        assertThat(UserStyleData(mapOf("A" to "a".encodeToByteArray()))).isEqualTo(
            UserStyleData(mapOf("A" to "a".encodeToByteArray()))
        )

        assertThat(
            UserStyleData(mapOf("A" to "a".encodeToByteArray(), "B" to "b".encodeToByteArray()))
        ).isEqualTo(
            UserStyleData(mapOf("A" to "a".encodeToByteArray(), "B" to "b".encodeToByteArray()))
        )

        assertThat(UserStyleData(mapOf("A" to "a".encodeToByteArray()))).isNotEqualTo(
            UserStyleData(mapOf("A" to "b".encodeToByteArray()))
        )

        assertThat(UserStyleData(mapOf("A" to "a".encodeToByteArray()))).isNotEqualTo(
            UserStyleData(mapOf("B" to "a".encodeToByteArray()))
        )

        assertThat(UserStyleData(mapOf("A" to "a".encodeToByteArray()))).isNotEqualTo(
            UserStyleData(mapOf("A" to "a".encodeToByteArray(), "B" to "b".encodeToByteArray()))
        )

        assertThat(
            UserStyleData(mapOf("A" to "a".encodeToByteArray(), "B" to "b".encodeToByteArray()))
        ).isNotEqualTo(UserStyleData(mapOf("A" to "a".encodeToByteArray())))
    }

    @Test
    public fun userStyleData_toString() {
        val userStyleData = UserStyleData(
            mapOf(
                "A" to "a".encodeToByteArray(),
                "B" to "b".encodeToByteArray()
            )
        )

        assertThat(userStyleData.toString()).contains("A=a")
        assertThat(userStyleData.toString()).contains("B=b")
    }

    @Test
    public fun optionIdToStringTest() {
        assertThat(BooleanUserStyleSetting.BooleanOption.TRUE.toString()).isEqualTo("true")
        assertThat(BooleanUserStyleSetting.BooleanOption.FALSE.toString()).isEqualTo("false")
        assertThat(gothicStyleOption.toString()).isEqualTo("gothic_style")
        assertThat(DoubleRangeUserStyleSetting.DoubleRangeOption(12.3).toString())
            .isEqualTo("12.3")
        assertThat(LongRangeUserStyleSetting.LongRangeOption(123).toString())
            .isEqualTo("123")
        assertThat(CustomValueOption("test".encodeToByteArray()).toString()).isEqualTo("test")
    }
}
