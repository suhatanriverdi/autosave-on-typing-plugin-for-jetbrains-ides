package com.genesiscorp.autosaveontyping

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class AutoSaveOnTypingListener : EditorFactoryListener {

    private val autoSaveConfig = AutoSaveConfig()
    private val processingDocuments = mutableSetOf<com.intellij.openapi.editor.Document>()
    private val registeredDocuments = mutableSetOf<com.intellij.openapi.editor.Document>()
    private val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    init {
        initializeListenersForExistingEditors()
    }

    override fun editorCreated(event: EditorFactoryEvent) {
        addListenerToDocument(event.editor.document)
    }

    private fun initializeListenersForExistingEditors() {
        val editorFactory = EditorFactory.getInstance()
        val editors = editorFactory.allEditors

        for (editor in editors) {
            addListenerToDocument(editor.document)
        }
    }

    private fun addListenerToDocument(document: com.intellij.openapi.editor.Document) {
        // Do not add listener if already added before
        if (registeredDocuments.contains(document)) {
            return
        }

        // Add document to registeredDocuments set
        registeredDocuments.add(document)

        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                // If processing, do not repeat, kind of debounce mechanism
                if (processingDocuments.contains(event.document)) {
                    return
                }

                // Add to processing set
                processingDocuments.add(event.document)

                // Start a coroutine to handle saving
                coroutineScope.launch {
                    try {
                        autoSaveDocument(document)
                    } catch (e: Exception) {
                        showNotification("Error during document change processing: ${e.message}")
                    } finally {
                        // Remove from the set after processing
                        processingDocuments.remove(event.document)
                    }
                }
            }
        })
    }

    // Use suspend to allow asynchronous behavior
    private suspend fun autoSaveDocument(document: com.intellij.openapi.editor.Document) {
        try {
            val virtualFile: VirtualFile? = FileDocumentManager.getInstance().getFile(document)

            if (virtualFile != null && virtualFile.isValid) {
                // Get the delay in milliseconds (converted from the delay setting)
                val delayInMillis = autoSaveConfig.getDelayLong()

                // Perform the delay asynchronously (non-blocking, IO optimized)
                delay(delayInMillis)

                // Save the document in the UI thread (must use runWriteAction)
                ApplicationManager.getApplication().invokeLater {
                    WriteAction.run<Throwable> {
                        FileDocumentManager.getInstance().saveDocument(document)
                    }
                }
            }
        } catch (e: Exception) {
            showNotification("Error saving document: ${e.message}")
        }
    }

    private fun showNotification(content: String) {
        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Auto Save Notifications")
        val notification = notificationGroup.createNotification(content, NotificationType.ERROR)
        notification.notify(null)
    }
}