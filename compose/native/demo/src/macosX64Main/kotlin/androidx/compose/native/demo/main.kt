package androidx.compose.native.demo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.native.Window
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import platform.AppKit.*

fun main() {
    NSApplication.sharedApplication()
    createWindow("Compose/Native sample")
    NSApp?.run()
}

fun createWindow(title: String) {
    println("createWindow()")
    Window(title) {
        var tick by remember { mutableStateOf(false) }
        Column {
            Box(modifier = Modifier
                .padding(16.dp)
                .background(color = Color.Red)
                .width(100.dp).height(100.dp)
                .clickable {
                    println("Red box: clicked")
                }.pointerMoveFilter(
                    onMove = {
                        println("Red box: onMove")
                        true
                    },
                    onEnter = {
                        println("Red box: onEnter")
                        true
                    },
                    onExit = {
                        println("Red box: onExit")
                        true
                    }
                )
            ) {
                Box(modifier = Modifier
                    .padding(16.dp)
                    .background(color = if (tick) Color.Green else Color.Blue)
                    .width(20.dp).height(20.dp)
                    .clickable {
                        println("Small box: clicked")
                    }
                    .pointerMoveFilter(
                        onMove = {
                            println("Small box: onMove")
                            true
                        },
                        onEnter = {
                            println("Small box: onEnter")
                            true
                        },
                        onExit = {
                            println("Small box: onExit")
                            true
                        }
                    )
                )
            }
            Spacer(
                Modifier.width(200.dp).height(4.dp).background(color = Color.DarkGray)
            )
            Button(
                modifier = Modifier
                    .padding(16.dp),
                onClick = {
                    println("Button clicked!")
                    tick = !tick
                }
            ) {
            }
        }
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos {
                    println("NANO: $it, tick: $tick")
                }
            }
        }
    }
    println("end createWindow()")
}
