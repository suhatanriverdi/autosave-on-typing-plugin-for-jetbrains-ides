package com.genesiscorp.autosaveontyping

import com.intellij.openapi.options.Configurable
import javax.swing.JPanel
import com.intellij.ui.components.JBSlider
import javax.swing.JLabel
import javax.swing.BoxLayout
import com.intellij.openapi.application.ApplicationManager

class AutoSaveConfig : Configurable {

    // Slider from 0 to 10, with increments of 1 second (1000ms)
    private val delaySlider = JBSlider(0, 10, getDelayInt()).apply {
        // Set slider to move in steps of 1 second
        paintTicks = true
        paintLabels = true
        majorTickSpacing = 1  // Every 1 second for major tick
        minorTickSpacing = 1  // No minor ticks (since it's 1 second step)
        snapToTicks = true     // Ensure the slider snaps to ticks
    }

    private val delayLabel = JLabel("Delay (in seconds): ${delaySlider.value}")

    override fun getDisplayName(): String {
        return "AutoSave on Typing"
    }

    override fun createComponent(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

        // Slider and label for delay
        delaySlider.addChangeListener {
            delayLabel.text = "Delay (in seconds): ${delaySlider.value}"
        }

        panel.add(delayLabel)
        panel.add(delaySlider)

        return panel
    }

    override fun isModified(): Boolean {
        return delaySlider.value != getDelayInt()
    }

    override fun apply() {
        val delay = delaySlider.value
        setDelay(delay)
    }

    override fun reset() {
        delaySlider.value = getDelayInt()
        delayLabel.text = "Delay (in seconds): ${delaySlider.value}"
    }

    private fun getDelayInt(): Int {
        val app = ApplicationManager.getApplication()
        val settings = app.getService(AutoSaveSettings::class.java)
        val originalDelay = settings.getDelaySetting()

        return originalDelay
    }

    internal fun getDelayLong(): Long {
        val app = ApplicationManager.getApplication()
        val settings = app.getService(AutoSaveSettings::class.java)

        return when (val originalDelay = settings.getDelaySetting()) {
            0 -> 0L
            1 -> 500L
            else -> (originalDelay - 1).toLong() * 1000L
        }
    }

    private fun setDelay(delay: Int) {
        val app = ApplicationManager.getApplication()
        val settings = app.getService(AutoSaveSettings::class.java)
        settings.setDelaySetting(delay)
    }
}