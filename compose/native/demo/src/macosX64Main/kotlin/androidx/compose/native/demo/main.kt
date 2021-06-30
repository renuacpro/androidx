package androidx.compose.native.demo

import platform.AppKit.*

import androidx.compose.native.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.*
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

fun main() {
    NSApplication.sharedApplication()
    createWindow("Compose/Native sample")
    NSApp?.run()
}

fun createWindow(title: String) {
    println("createWindow()")
    Window(title) {
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
                    .background(color = Color.Blue)
                    .width(20.dp).height(20.dp)
                    .clickable {
                        println("Blue box: clicked")
                    }
                    .pointerMoveFilter(
                        onMove = {
                            println("Blue box: onMove")
                            true
                        },
                        onEnter = {
                            println("Blue box: onEnter")
                            true
                        },
                        onExit = {
                            println("Blue box: onExit")
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
                    println("HELLO again!")
                }
            ) {
            }
        }
    }
    println("end createWindow()")
}
