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

@file:OptIn(ExperimentalComposeUiApi::class)
package androidx.compose.ui.window

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.asAwtImage
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.toSwingKeyStroke
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.util.AddRemoveMutableList
import java.awt.CheckboxMenuItem
import java.awt.Menu
import java.awt.MenuItem
import java.awt.event.KeyEvent
import java.lang.UnsupportedOperationException
import javax.swing.AbstractButton
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JCheckBoxMenuItem
import javax.swing.JComponent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JRadioButtonMenuItem

private val DefaultIconSize = Size(16f, 16f)

/**
 * Composes the given composable into the MenuBar.
 *
 * The new composition can be logically "linked" to an existing one, by providing a
 * [parentComposition]. This will ensure that invalidations and CompositionLocals will flow
 * through the two compositions as if they were not separate.
 *
 * @param parentComposition The parent composition reference to coordinate
 *        scheduling of composition updates.
 *        If null then default root composition will be used.
 * @param content Composable content of the MenuBar.
 */
fun JMenuBar.setContent(
    parentComposition: CompositionContext,
    content: @Composable (MenuBarScope.() -> Unit)
): Composition {
    val applier = MutableListApplier(asMutableList())
    val composition = Composition(applier, parentComposition)
    val scope = MenuBarScope()
    composition.setContent {
        scope.content()
    }
    return composition
}

/**
 * Composes the given composable into the Menu.
 *
 * The new composition can be logically "linked" to an existing one, by providing a
 * [parentComposition]. This will ensure that invalidations and CompositionLocals will flow
 * through the two compositions as if they were not separate.
 *
 * @param parentComposition The parent composition reference to coordinate
 *        scheduling of composition updates.
 *        If null then default root composition will be used.
 * @param content Composable content of the Menu.
 */
fun Menu.setContent(
    parentComposition: CompositionContext,
    content: @Composable (MenuScope.() -> Unit)
): Composition {
    val applier = MutableListApplier(asMutableList())
    val composition = Composition(applier, parentComposition)
    val scope = MenuScope(AwtMenuScope())
    composition.setContent {
        scope.content()
    }
    return composition
}

/**
 * Composes the given composable into the Menu.
 *
 * The new composition can be logically "linked" to an existing one, by providing a
 * [parentComposition]. This will ensure that invalidations and CompositionLocals will flow
 * through the two compositions as if they were not separate.
 *
 * @param parentComposition The parent composition reference to coordinate
 *        scheduling of composition updates.
 *        If null then default root composition will be used.
 * @param content Composable content of the Menu.
 */
fun JMenu.setContent(
    parentComposition: CompositionContext,
    content: @Composable (MenuScope.() -> Unit)
): Composition {
    val applier = MutableListApplier(asMutableList())
    val composition = Composition(applier, parentComposition)
    val scope = MenuScope(SwingMenuScope())
    composition.setContent {
        scope.content()
    }
    return composition
}

// This menu is used by Tray
@Composable
private fun AwtMenu(
    text: String,
    enabled: Boolean,
    content: @Composable MenuScope.() -> Unit
) {
    val menu = remember(::Menu)
    val compositionContext = rememberCompositionContext()

    DisposableEffect(Unit) {
        val composition = menu.setContent(compositionContext, content)
        onDispose {
            composition.dispose()
        }
    }

    ComposeNode<Menu, MutableListApplier<MenuItem>>(
        factory = { menu },
        update = {
            set(text, Menu::setLabel)
            set(enabled, Menu::setEnabled)
        }
    )
}

@Composable
private fun SwingMenu(
    text: String,
    enabled: Boolean,
    mnemonic: Char?,
    content: @Composable MenuScope.() -> Unit
) {
    val menu = remember(::JMenu)
    val compositionContext = rememberCompositionContext()

    DisposableEffect(Unit) {
        val composition = menu.setContent(compositionContext, content)
        onDispose {
            composition.dispose()
        }
    }

    ComposeNode<JMenu, MutableListApplier<JComponent>>(
        factory = { menu },
        update = {
            set(text, JMenu::setText)
            set(enabled, JMenu::setEnabled)
            set(mnemonic, JMenu::setMnemonic)
        }
    )
}

// TODO(demin): consider making MenuBarScope/MenuScope as an interface
//  after b/165812010 will be fixed
/**
 * Receiver scope which is used by [JMenuBar.setContent] and [FrameWindowScope.MenuBar].
 */
class MenuBarScope internal constructor() {
    /**
     * Adds menu to the menu bar
     *
     * @param text text of the menu that will be shown on the menu bar
     * @param enabled is this menu item can be chosen
     * @param mnemonic character that corresponds to some key on the keyboard.
     * When this key and Alt modifier will be pressed - menu will be open.
     * If the character is found within the item's text, the first occurrence
     * of it will be underlined.
     * @param content content of the menu (sub menus, items, separators, etc)
     */
    @Composable
    fun Menu(
        text: String,
        @ExperimentalComposeUiApi
        mnemonic: Char? = null,
        enabled: Boolean = true,
        content: @Composable MenuScope.() -> Unit
    ): Unit = SwingMenu(
        text,
        enabled,
        mnemonic,
        content
    )
}

internal interface MenuScopeImpl {
    @Composable
    fun Menu(
        text: String,
        enabled: Boolean,
        mnemonic: Char?,
        content: @Composable MenuScope.() -> Unit
    )

    @Composable
    fun Separator()

    @Composable
    fun Item(
        text: String,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onClick: () -> Unit
    )

    @Composable
    fun CheckboxItem(
        text: String,
        checked: Boolean,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onCheckedChange: (Boolean) -> Unit
    )

    @Composable
    fun RadioButtonItem(
        text: String,
        selected: Boolean,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onClick: () -> Unit
    )
}

private class AwtMenuScope : MenuScopeImpl {
    /**
     * Adds sub menu to the menu
     *
     * @param text text of the menu that will be shown in the menu
     * @param enabled is this menu item can be chosen
     * @param content content of the menu (sub menus, items, separators, etc)
     */
    @Composable
    override fun Menu(
        text: String,
        enabled: Boolean,
        mnemonic: Char?,
        content: @Composable MenuScope.() -> Unit
    ) {
        if (mnemonic != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support mnemonic")
        }

        AwtMenu(
            text,
            enabled,
            content
        )
    }

    @Composable
    override fun Separator() {
        ComposeNode<MenuItem, MutableListApplier<MenuItem>>(
            // item with name "-" has different look
            factory = { MenuItem("-") },
            update = {}
        )
    }

    @Composable
    override fun Item(
        text: String,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onClick: () -> Unit
    ) {
        if (icon != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support icon")
        }
        if (mnemonic != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support mnemonic")
        }
        if (shortcut != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support shortcut")
        }

        val currentOnClick by rememberUpdatedState(onClick)

        ComposeNode<MenuItem, MutableListApplier<MenuItem>>(
            factory = {
                MenuItem().apply {
                    addActionListener {
                        currentOnClick()
                    }
                }
            },
            update = {
                set(text, MenuItem::setLabel)
                set(enabled, MenuItem::setEnabled)
            }
        )
    }

    @Composable
    override fun CheckboxItem(
        text: String,
        checked: Boolean,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onCheckedChange: (Boolean) -> Unit
    ) {
        if (icon != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support icon")
        }
        if (mnemonic != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support mnemonic")
        }
        if (shortcut != null) {
            throw UnsupportedOperationException("java.awt.Menu doesn't support shortcut")
        }

        val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)

        val checkedState = rememberStateChanger(
            CheckboxMenuItem::setState,
            CheckboxMenuItem::getState
        )

        ComposeNode<CheckboxMenuItem, MutableListApplier<JComponent>>(
            factory = {
                CheckboxMenuItem().apply {
                    addItemListener {
                        checkedState.fireChange(this, currentOnCheckedChange)
                    }
                }
            },
            update = {
                set(text, CheckboxMenuItem::setLabel)
                set(checked, checkedState::set)
                set(enabled, CheckboxMenuItem::setEnabled)
            }
        )
    }

    @Composable
    override fun RadioButtonItem(
        text: String,
        selected: Boolean,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onClick: () -> Unit
    ) {
        throw UnsupportedOperationException("java.awt.Menu doesn't support RadioButtonItem")
    }
}

private class SwingMenuScope : MenuScopeImpl {
    /**
     * Adds sub menu to the menu
     *
     * @param text text of the menu that will be shown in the menu
     * @param enabled is this menu item can be chosen
     * @param content content of the menu (sub menus, items, separators, etc)
     */
    @Composable
    override fun Menu(
        text: String,
        enabled: Boolean,
        mnemonic: Char?,
        content: @Composable MenuScope.() -> Unit
    ): Unit = SwingMenu(
        text,
        enabled,
        mnemonic,
        content
    )

    @Composable
    override fun Separator() {
        ComposeNode<JComponent, MutableListApplier<JComponent>>(
            // item with name "-" has different look
            factory = { JPopupMenu.Separator() },
            update = {}
        )
    }

    @Composable
    override fun Item(
        text: String,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onClick: () -> Unit
    ) {
        val currentOnClick by rememberUpdatedState(onClick)
        val awtIcon = rememberAwtIcon(icon)

        ComposeNode<JMenuItem, MutableListApplier<JComponent>>(
            factory = {
                JMenuItem().apply {
                    addActionListener {
                        currentOnClick()
                    }
                }
            },
            update = {
                set(text, JMenuItem::setText)
                set(awtIcon, JMenuItem::setIcon)
                set(enabled, JMenuItem::setEnabled)
            }
        )
    }

    @Composable
    override fun CheckboxItem(
        text: String,
        checked: Boolean,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
        val awtIcon = rememberAwtIcon(icon)

        val checkedState = rememberStateChanger(
            JCheckBoxMenuItem::setState,
            JCheckBoxMenuItem::getState
        )

        ComposeNode<JCheckBoxMenuItem, MutableListApplier<JComponent>>(
            factory = {
                JCheckBoxMenuItem().apply {
                    addItemListener {
                        checkedState.fireChange(this, currentOnCheckedChange)
                    }
                }
            },
            update = {
                set(text, JCheckBoxMenuItem::setText)
                set(checked, checkedState::set)
                set(awtIcon, JCheckBoxMenuItem::setIcon)
                set(enabled, JCheckBoxMenuItem::setEnabled)
                set(mnemonic, JCheckBoxMenuItem::setMnemonic)
                set(shortcut, JCheckBoxMenuItem::setShortcut)
            }
        )
    }

    @Composable
    override fun RadioButtonItem(
        text: String,
        selected: Boolean,
        icon: Painter?,
        enabled: Boolean,
        mnemonic: Char?,
        shortcut: KeyShortcut?,
        onClick: () -> Unit,
    ) {
        val currentOnClick by rememberUpdatedState(onClick)
        val awtIcon = rememberAwtIcon(icon)

        val selectedState = rememberStateChanger(
            JRadioButtonMenuItem::setSelected,
            JRadioButtonMenuItem::isSelected
        )

        ComposeNode<JRadioButtonMenuItem, MutableListApplier<JComponent>>(
            factory = {
                JRadioButtonMenuItem().apply {
                    addItemListener {
                        selectedState.fireChange(this) { currentOnClick() }
                    }
                }
            },
            update = {
                set(text, JRadioButtonMenuItem::setText)
                set(selected, selectedState::set)
                set(awtIcon, JRadioButtonMenuItem::setIcon)
                set(enabled, JRadioButtonMenuItem::setEnabled)
                set(mnemonic, JRadioButtonMenuItem::setMnemonic)
                set(shortcut, JRadioButtonMenuItem::setShortcut)
            }
        )
    }
}

// we use `class MenuScope` and `interface MenuScopeImpl` instead of just `interface MenuScope`
// because of b/165812010
/**
 * Receiver scope which is used by [Menu.setContent], [MenuBarScope.Menu], [Tray]
 */
class MenuScope internal constructor(private val impl: MenuScopeImpl) {
    /**
     * Adds sub menu to the menu
     *
     * @param text text of the menu that will be shown in the menu
     * @param enabled is this menu item can be chosen
     * @param mnemonic character that corresponds to some key on the keyboard.
     * When this key will be pressed - menu will be open.
     * If the character is found within the item's text, the first occurrence
     * of it will be underlined.
     * @param content content of the menu (sub menus, items, separators, etc)
     */
    @Composable
    fun Menu(
        text: String,
        enabled: Boolean = true,
        @ExperimentalComposeUiApi
        mnemonic: Char? = null,
        content: @Composable MenuScope.() -> Unit
    ): Unit = impl.Menu(
        text,
        enabled,
        mnemonic,
        content
    )

    /**
     * Adds separator to the menu
     */
    @Composable
    fun Separator() = impl.Separator()

    /**
     * Adds item to the menu
     *
     * @param text text of the item that will be shown in the menu
     * @param icon icon of the item
     * @param enabled is this item item can be chosen
     * @param mnemonic character that corresponds to some key on the keyboard.
     * When this key will be pressed - [onClick] will be triggered.
     * If the character is found within the item's text, the first occurrence
     * of it will be underlined.
     * @param shortcut key combination which triggers [onClick] action without
     * navigating the menu hierarchy.
     * @param onClick action that should be performed when the user clicks on the item
     */
    @Composable
    fun Item(
        text: String,
        icon: Painter? = null,
        enabled: Boolean = true,
        @ExperimentalComposeUiApi
        mnemonic: Char? = null,
        @ExperimentalComposeUiApi
        shortcut: KeyShortcut? = null,
        onClick: () -> Unit
    ): Unit = impl.Item(text, icon, enabled, mnemonic, shortcut, onClick)

    /**
     * Adds item with checkbox to the menu
     *
     * @param text text of the item that will be shown in the menu
     * @param checked whether checkbox is checked or unchecked
     * @param icon icon of the item
     * @param enabled is this item item can be chosen
     * @param mnemonic character that corresponds to some key on the keyboard.
     * When this key will be pressed - [onCheckedChange] will be triggered.
     * If the character is found within the item's text, the first occurrence
     * of it will be underlined.
     * @param shortcut key combination which triggers [onCheckedChange] action without
     * navigating the menu hierarchy.
     * @param onCheckedChange callback to be invoked when checkbox is being clicked,
     * therefore the change of checked state in requested
     */
    @ExperimentalComposeUiApi
    @Composable
    fun CheckboxItem(
        text: String,
        checked: Boolean,
        icon: Painter? = null,
        enabled: Boolean = true,
        mnemonic: Char? = null,
        shortcut: KeyShortcut? = null,
        onCheckedChange: (Boolean) -> Unit
    ): Unit = impl.CheckboxItem(
        text, checked, icon, enabled, mnemonic, shortcut, onCheckedChange
    )

    /**
     * Adds item with radio button to the menu
     *
     * @param text text of the item that will be shown in the menu
     * @param selected boolean state for this button: either it is selected or not
     * @param icon icon of the item
     * @param enabled is this item item can be chosen
     * @param mnemonic character that corresponds to some key on the keyboard.
     * When this key will be pressed - [onClick] will be triggered.
     * If the character is found within the item's text, the first occurrence
     * of it will be underlined.
     * @param shortcut key combination which triggers [onClick] action without
     * navigating the menu hierarchy.
     * @param onClick callback to be invoked when the radio button is being clicked
     */
    @Composable
    @ExperimentalComposeUiApi
    fun RadioButtonItem(
        text: String,
        selected: Boolean,
        icon: Painter? = null,
        enabled: Boolean = true,
        mnemonic: Char? = null,
        shortcut: KeyShortcut? = null,
        onClick: () -> Unit
    ): Unit = impl.RadioButtonItem(
        text, selected, icon, enabled, mnemonic, shortcut, onClick
    )
}

private class MutableListApplier<T>(
    private val list: MutableList<T>
) : AbstractApplier<T?>(null) {
    override fun insertTopDown(index: Int, instance: T?) {
        list.add(index, instance!!)
    }

    override fun insertBottomUp(index: Int, instance: T?) {
        // Ignore, we have plain list
    }

    override fun remove(index: Int, count: Int) {
        for (i in index + count - 1 downTo index) {
            list.removeAt(i)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun move(from: Int, to: Int, count: Int) {
        (list as MutableList<T?>).move(from, to, count)
    }

    override fun onClear() {
        list.clear()
    }
}

private fun JMenuBar.asMutableList(): MutableList<JComponent> {
    return object : AddRemoveMutableList<JComponent>() {
        override val size: Int get() = this@asMutableList.menuCount
        override fun get(index: Int) = this@asMutableList.getMenu(index)

        override fun performAdd(element: JComponent) {
            this@asMutableList.add(element)
        }

        override fun performRemove(index: Int) {
            this@asMutableList.remove(index)
        }
    }
}

private fun JMenu.asMutableList(): MutableList<JComponent> {
    return object : AddRemoveMutableList<JComponent>() {
        override val size: Int get() = this@asMutableList.itemCount
        override fun get(index: Int) = this@asMutableList.getMenuComponent(index) as JComponent

        override fun performAdd(element: JComponent) {
            this@asMutableList.add(element)
        }

        override fun performRemove(index: Int) {
            this@asMutableList.remove(index)
        }
    }
}

private fun Menu.asMutableList(): MutableList<MenuItem> {
    return object : AddRemoveMutableList<MenuItem>() {
        override val size: Int get() = this@asMutableList.itemCount
        override fun get(index: Int) = this@asMutableList.getItem(index)

        override fun performAdd(element: MenuItem) {
            this@asMutableList.add(element)
        }

        override fun performRemove(index: Int) {
            this@asMutableList.remove(index)
        }
    }
}

private fun AbstractButton.setMnemonic(char: Char?) {
    mnemonic = KeyEvent.getExtendedKeyCodeForChar(char?.code ?: 0)
}

private fun JMenuItem.setShortcut(shortcut: KeyShortcut?) {
    accelerator = shortcut?.toSwingKeyStroke()
}

@Composable
private fun rememberAwtIcon(painter: Painter?): Icon? {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    return remember(painter, density, layoutDirection) {
        painter
            ?.asAwtImage(density, layoutDirection, DefaultIconSize)
            ?.let(::ImageIcon)
    }
}

@Composable
private fun <R, V> rememberStateChanger(
    set: R.(V) -> Unit,
    get: R.() -> V
): ComposeState<R, V> = remember {
    ComposeState(set, get)
}

/**
 * Helper class to change state without firing a listener, and fire a listener without state change
 *
 * The purpose is to make Swing's state behave as it was attribute in stateless Compose widget.
 * For example, ComposeState don't fire `onCheckedChange` if we change `checkbox.checked`,
 * and don't change `checkbox.checked` if user clicks on checkbox.
 */
private class ComposeState<R, V>(
    private val set: R.(V) -> Unit,
    private val get: R.() -> V,
) {
    private var needEatEvent = false
    private val ref = Ref<V>()

    fun set(receiver: R, value: V) {
        try {
            needEatEvent = true
            receiver.set(value)
            ref.value = value
        } finally {
            needEatEvent = false
        }
    }

    fun fireChange(receiver: R, onChange: (V) -> Unit) {
        if (!needEatEvent) {
            onChange(receiver.get())
            set(receiver, ref.value!!) // prevent internal state change
        }
    }
}