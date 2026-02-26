package com.ifochka.m14n.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>?,
) : Scene<T> {
    override val entries: List<NavEntry<T>> =
        if (detailEntry != null) listOf(listEntry, detailEntry) else listOf(listEntry)
    override val content: @Composable (() -> Unit) = {
        // 1. Detect if we are in "Wide" mode inside the scene
        // Note: 840.dp matches your App.kt threshold
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 840.dp

            if (!isWide) {
                // MOBILE LOGIC: Standard Stack
                // Show detail if it exists, otherwise list
                detailEntry?.Content() ?: listEntry.Content()
            } else {
                // WIDE WEB LOGIC: The "Smart" Split
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            // This weight animate makes the list "shrink" to the left
                            .weight(if (detailEntry != null) 0.35f else 1f)
                            .animateContentSize(),
                    ) {
                        listEntry.Content()
                    }

                    // Internal animation for the detail pane only
                    AnimatedVisibility(
                        visible = detailEntry != null,
                        enter = slideInHorizontally(initialOffsetX = { it }),
                        exit = slideOutHorizontally(targetOffsetX = { it }),
                        modifier = Modifier.fillMaxHeight().weight(0.65f),
                    ) {
                        detailEntry?.Content()
                    }
                }
            }
        }
    }

    companion object {
        internal const val LIST_KEY = "ListDetailScene-List"
        internal const val DETAIL_KEY = "ListDetailScene-Detail"

        fun listPane() = mapOf(LIST_KEY to true)

        fun detailPane() = mapOf(DETAIL_KEY to true)
    }
}

class ListDetailSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val listEntry = entries.findLast { it.metadata.containsKey(ListDetailScene.LIST_KEY) }
            ?: return null
        val detailEntry = entries.lastOrNull()
            ?.takeIf { it.metadata.containsKey(ListDetailScene.DETAIL_KEY) }
        return ListDetailScene(
            key = listEntry.contentKey,
            previousEntries = if (detailEntry != null) entries.dropLast(1) else emptyList(),
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}

@Composable
fun <T : Any> rememberListDetailSceneStrategy(): ListDetailSceneStrategy<T> = remember { ListDetailSceneStrategy() }
