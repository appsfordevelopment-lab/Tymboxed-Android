package dev.ambitionsoftware.tymeboxed.service.inapp

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque
import java.util.Locale

/**
 * View-tree walks adapted from Switchly
 * [SwitchlyAccessibilityService](https://gitlab.com/Saltyy/switchly-public).
 */
internal object InAppA11yNodes {
    private const val MAX_NODE_SCAN_COUNT = 120
    private const val MAX_NODE_SCAN_DEPTH = 12

    private data class WorkItem(val node: AccessibilityNodeInfo, val depth: Int, val owned: Boolean)

    fun findAnyNode(root: AccessibilityNodeInfo, pred: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        val stack = ArrayDeque<WorkItem>()
        stack.addLast(WorkItem(root, 0, false))
        var visited = 0
        while (stack.isNotEmpty() && visited < MAX_NODE_SCAN_COUNT) {
            val item = stack.removeLast()
            val current = item.node
            try {
                visited++
                if (pred(current)) return AccessibilityNodeInfo.obtain(current)
                if (item.depth >= MAX_NODE_SCAN_DEPTH) continue
                val childCount = runCatching { current.childCount }.getOrDefault(0)
                for (i in childCount - 1 downTo 0) {
                    if (visited + stack.size >= MAX_NODE_SCAN_COUNT) break
                    val child = runCatching { current.getChild(i) }.getOrNull() ?: continue
                    stack.addLast(WorkItem(child, item.depth + 1, true))
                }
            } finally {
                if (item.owned) runCatching { current.recycle() }
            }
        }
        return null
    }

    fun nodeTextOrDesc(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val t = node.text?.toString().orEmpty()
        val cd = node.contentDescription?.toString().orEmpty()
        return listOf(t, cd).filter { it.isNotBlank() }
            .joinToString(" ")
            .lowercase(Locale.getDefault())
    }

    fun nodeTextMatches(root: AccessibilityNodeInfo, needles: List<String>): Boolean {
        val n = needles.map { it.lowercase(Locale.getDefault()) }
        val found = findAnyNode(root) { node ->
            val t = node.text?.toString()?.lowercase(Locale.getDefault())
            val cd = node.contentDescription?.toString()?.lowercase(Locale.getDefault())
            (t != null && n.any { t.contains(it) }) || (cd != null && n.any { cd.contains(it) })
        }
        return try {
            found != null
        } finally {
            if (found != null) runCatching { found.recycle() }
        }
    }

    fun eventTextMatches(event: AccessibilityEvent?, needles: List<String>): Boolean {
        if (event == null) return false
        val n = needles.map { it.lowercase(Locale.getDefault()) }
        val cd = event.contentDescription?.toString()?.lowercase(Locale.getDefault())
        if (cd != null && n.any { cd.contains(it) }) return true
        for (cs in event.text.orEmpty()) {
            val t = cs?.toString()?.lowercase(Locale.getDefault()) ?: continue
            if (n.any { t.contains(it) }) return true
        }
        return false
    }

    fun hasSelectedLabel(root: AccessibilityNodeInfo, needles: List<String>): Boolean {
        val n = needles.map { it.lowercase(Locale.getDefault()) }
        val found = findAnyNode(root) { node ->
            if (!node.isSelected) return@findAnyNode false
            val t = node.text?.toString()?.lowercase(Locale.getDefault())
            val cd = node.contentDescription?.toString()?.lowercase(Locale.getDefault())
            (t != null && n.any { t.contains(it) }) || (cd != null && n.any { cd.contains(it) })
        }
        return try {
            found != null
        } finally {
            if (found != null) runCatching { found.recycle() }
        }
    }

    /**
     * Like [hasSelectedLabel] but also matches checked nav items (some apps use
     * [AccessibilityNodeInfo.isChecked] for the active bottom tab, not isSelected).
     */
    fun hasSelectedOrCheckedLabel(root: AccessibilityNodeInfo, needles: List<String>): Boolean {
        val n = needles.map { it.lowercase(Locale.getDefault()) }
        val found = findAnyNode(root) { node ->
            if (!node.isSelected && !node.isChecked) return@findAnyNode false
            val t = node.text?.toString()?.lowercase(Locale.getDefault())
            val cd = node.contentDescription?.toString()?.lowercase(Locale.getDefault())
            (t != null && n.any { t.contains(it) }) || (cd != null && n.any { cd.contains(it) })
        }
        return try {
            found != null
        } finally {
            if (found != null) runCatching { found.recycle() }
        }
    }

    fun hasSelectedLabelInPackage(
        root: AccessibilityNodeInfo,
        needles: List<String>,
        pkg: String,
    ): Boolean {
        val n = needles.map { it.lowercase(Locale.getDefault()) }
        val targetPkg = pkg.lowercase(Locale.getDefault())
        val found = findAnyNode(root) { node ->
            if (!node.isSelected) return@findAnyNode false
            val nodePkg = node.packageName?.toString()?.lowercase(Locale.getDefault()).orEmpty()
            if (nodePkg != targetPkg) return@findAnyNode false
            val t = node.text?.toString()?.lowercase(Locale.getDefault())
            val cd = node.contentDescription?.toString()?.lowercase(Locale.getDefault())
            (t != null && n.any { t.contains(it) }) || (cd != null && n.any { cd.contains(it) })
        }
        return try {
            found != null
        } finally {
            if (found != null) runCatching { found.recycle() }
        }
    }
}
