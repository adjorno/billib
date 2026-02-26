package com.ifochka.m14n.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    private val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else if (route is RouteTrackDetails) {
            val stack = state.backStacks[state.topLevelRoute] ?: return
            stack.removeAll { it is RouteTrackDetails }
            stack.add(route)
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        if (currentStack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
