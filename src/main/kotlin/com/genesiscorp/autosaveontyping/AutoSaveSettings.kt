package com.genesiscorp.autosaveontyping

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.PersistentStateComponent

@State(name = "AutoSaveOnTypingSettings", storages = [Storage("AutoSaveOnTypingSettings.xml")])
@Service
class AutoSaveSettings : PersistentStateComponent<AutoSaveSettings.State> {

    // State class to hold the settings
    data class State(var delay: Int = 0)

    private var state: State = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    internal fun getDelaySetting(): Int {
        return state.delay
    }

    internal fun setDelaySetting(newDelay: Int) {
        state.delay = newDelay
    }
}