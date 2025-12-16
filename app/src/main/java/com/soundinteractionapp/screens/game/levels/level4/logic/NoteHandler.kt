package com.soundinteractionapp.screens.game.levels.level4.logic

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.HitResult
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.models.ActiveNote
import kotlin.math.*

/**
 * NoteHandler - 處理音符點擊、滑條邏輯
 *
 * ✅ 此文件不需要修改，音效延遲問題已在 Level4Screen 和 GameLoopHelper 中解決
 */
object NoteHandler {

    private const val TAG = "NoteHandler"

    private const val PLAY_FIELD_SCALE = 0.85f
    private const val ORIGINAL_WIDTH = 512f
    private const val ORIGINAL_HEIGHT = 384f

    // 緩存：每個滑條的弧長表
    private val arcLengthCache = mutableMapOf<Note, ArcLengthTable>()

    private data class ArcLengthTable(
        val points: List<Offset>,
        val cumulativeLengths: List<Float>,
        val totalLength: Float
    )

    private fun calculateScaleAndOffset(screenWidth: Float, screenHeight: Float): Pair<Pair<Float, Float>, Pair<Float, Float>> {
        val scaledWidth = ORIGINAL_WIDTH * PLAY_FIELD_SCALE
        val scaledHeight = ORIGINAL_HEIGHT * PLAY_FIELD_SCALE

        val offsetX = (ORIGINAL_WIDTH - scaledWidth) / 2f
        val offsetY = (ORIGINAL_HEIGHT - scaledHeight) / 2f

        val scaleX = (screenWidth / ORIGINAL_WIDTH) * PLAY_FIELD_SCALE
        val scaleY = (screenHeight / ORIGINAL_HEIGHT) * PLAY_FIELD_SCALE

        val screenOffsetX = offsetX * (screenWidth / ORIGINAL_WIDTH)
        val screenOffsetY = offsetY * (screenHeight / ORIGINAL_HEIGHT)

        return Pair(scaleX to scaleY, screenOffsetX to screenOffsetY)
    }

    fun handleTap(
        offset: Offset,
        activeNotes: List<ActiveNote>,
        currentTime: Long,
        screenWidth: Float,
        screenHeight: Float,
        beatmap: Beatmap,
        onHit: (ActiveNote, HitResult) -> Unit
    ) {
        val (scale, screenOffset) = calculateScaleAndOffset(screenWidth, screenHeight)
        val (scaleX, scaleY) = scale
        val (screenOffsetX, screenOffsetY) = screenOffset

        val tappedNote = activeNotes.find { activeNote ->
            if (activeNote.isHit || activeNote.note.type != NoteType.CIRCLE) return@find false

            val noteScreenX = activeNote.note.x * scaleX + screenOffsetX
            val noteScreenY = activeNote.note.y * scaleY + screenOffsetY

            val dx = offset.x - noteScreenX
            val dy = offset.y - noteScreenY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= 150f) {
                val timeDiff = abs(currentTime - activeNote.note.time)
                val inWindow = timeDiff <= beatmap.hitWindowGood

                Log.d(TAG, "Note hit check: timeDiff=$timeDiff, " +
                        "PERFECT=${beatmap.hitWindowPerfect}, " +
                        "GREAT=${beatmap.hitWindowGreat}, " +
                        "GOOD=${beatmap.hitWindowGood}, " +
                        "inWindow=$inWindow")

                inWindow
            } else {
                false
            }
        }

        tappedNote?.let { activeNote ->
            val timeDiff = abs(currentTime - activeNote.note.time)
            val hitResult = when {
                timeDiff <= beatmap.hitWindowPerfect -> HitResult.PERFECT
                timeDiff <= beatmap.hitWindowGreat -> HitResult.GREAT
                else -> HitResult.GOOD
            }

            Log.d(TAG, "Hit result: $hitResult (timeDiff=$timeDiff)")

            onHit(activeNote, hitResult)
        }
    }

    fun getSliderPositionAtProgress(note: Note, progress: Float): Offset {
        if (note.curvePoints.isEmpty()) {
            return Offset(note.x, note.y)
        }

        val arcTable = arcLengthCache.getOrPut(note) {
            buildArcLengthTable(note)
        }

        val totalSlides = note.slides
        val progressPerSlide = 1f / totalSlides
        val currentSlide = (progress / progressPerSlide).toInt().coerceIn(0, totalSlides - 1)
        val slideProgress = (progress - currentSlide * progressPerSlide) / progressPerSlide

        val actualProgress = if (currentSlide % 2 == 1) {
            1f - slideProgress
        } else {
            slideProgress
        }

        return getPointAtArcLength(arcTable, actualProgress)
    }

    private fun buildArcLengthTable(note: Note): ArcLengthTable {
        val pathPoints = generateSliderPathPoints(note)

        if (pathPoints.isEmpty()) {
            return ArcLengthTable(listOf(Offset(note.x, note.y)), listOf(0f), 0f)
        }

        val cumulativeLengths = mutableListOf(0f)
        var totalLength = 0f

        for (i in 1 until pathPoints.size) {
            val dx = pathPoints[i].x - pathPoints[i - 1].x
            val dy = pathPoints[i].y - pathPoints[i - 1].y
            val segmentLength = sqrt(dx * dx + dy * dy)
            totalLength += segmentLength
            cumulativeLengths.add(totalLength)
        }

        return ArcLengthTable(pathPoints, cumulativeLengths, totalLength)
    }

    private fun getPointAtArcLength(table: ArcLengthTable, progress: Float): Offset {
        if (progress <= 0f) return table.points.first()
        if (progress >= 1f) return table.points.last()

        val targetLength = progress * table.totalLength

        var left = 0
        var right = table.cumulativeLengths.size - 1

        while (left < right - 1) {
            val mid = (left + right) / 2
            if (table.cumulativeLengths[mid] < targetLength) {
                left = mid
            } else {
                right = mid
            }
        }

        val l1 = table.cumulativeLengths[left]
        val l2 = table.cumulativeLengths[right]
        val localProgress = if (l2 - l1 > 0.001f) {
            (targetLength - l1) / (l2 - l1)
        } else {
            0f
        }

        val p1 = table.points[left]
        val p2 = table.points[right]

        return Offset(
            p1.x + (p2.x - p1.x) * localProgress,
            p1.y + (p2.y - p1.y) * localProgress
        )
    }

    private fun generateSliderPathPoints(note: Note): List<Offset> {
        val controlPoints = mutableListOf(Offset(note.x, note.y))
        note.curvePoints.forEach { pair ->
            controlPoints.add(Offset(pair.first, pair.second))
        }

        if (controlPoints.size < 2) return controlPoints

        return when (note.curveType) {
            CurveType.LINEAR -> interpolateLinear(controlPoints)
            CurveType.PERFECT -> interpolatePerfectCircle(controlPoints)
            CurveType.BEZIER -> interpolateBezier(controlPoints)
            CurveType.CATMULL -> interpolateCatmullRom(controlPoints)
        }
    }

    private fun interpolateLinear(points: List<Offset>): List<Offset> {
        val result = mutableListOf<Offset>()
        val segments = 100

        for (i in 0 until points.size - 1) {
            for (t in 0..segments) {
                val progress = t.toFloat() / segments
                result.add(Offset(
                    points[i].x + (points[i + 1].x - points[i].x) * progress,
                    points[i].y + (points[i + 1].y - points[i].y) * progress
                ))
            }
        }

        return result
    }

    private fun interpolatePerfectCircle(points: List<Offset>): List<Offset> {
        if (points.size < 3) return interpolateLinear(points)

        val p1 = points[0]
        val p2 = points[1]
        val p3 = points[2]

        val circle = calculateCircleFrom3Points(p1, p2, p3)
        if (circle == null) {
            return interpolateLinear(points)
        }

        val (center, radius) = circle

        val startAngle = atan2(p1.y - center.y, p1.x - center.x)
        val endAngle = atan2(p3.y - center.y, p3.x - center.x)

        val midAngle = atan2(p2.y - center.y, p2.x - center.x)
        val isClockwise = isAngleBetween(midAngle, startAngle, endAngle)

        var angleSpan = if (isClockwise) {
            if (endAngle > startAngle) endAngle - startAngle else (2 * PI + endAngle - startAngle).toFloat()
        } else {
            if (startAngle > endAngle) startAngle - endAngle else (2 * PI + startAngle - endAngle).toFloat()
        }

        if (angleSpan > 2 * PI) angleSpan = (2 * PI).toFloat()

        val result = mutableListOf<Offset>()
        val segments = max(100, (angleSpan * 100).toInt())

        for (i in 0..segments) {
            val t = i.toFloat() / segments
            val angle = if (isClockwise) {
                startAngle + angleSpan * t
            } else {
                startAngle - angleSpan * t
            }

            result.add(Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            ))
        }

        return result
    }

    private fun calculateCircleFrom3Points(p1: Offset, p2: Offset, p3: Offset): Pair<Offset, Float>? {
        val ax = p1.x
        val ay = p1.y
        val bx = p2.x
        val by = p2.y
        val cx = p3.x
        val cy = p3.y

        val d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by))
        if (abs(d) < 0.001f) return null

        val ux = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d
        val uy = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d

        val radius = sqrt((ax - ux) * (ax - ux) + (ay - uy) * (ay - uy))

        return Pair(Offset(ux, uy), radius)
    }

    private fun isAngleBetween(angle: Float, start: Float, end: Float): Boolean {
        val normalizedAngle = normalizeAngle(angle)
        val normalizedStart = normalizeAngle(start)
        val normalizedEnd = normalizeAngle(end)

        return if (normalizedStart <= normalizedEnd) {
            normalizedAngle >= normalizedStart && normalizedAngle <= normalizedEnd
        } else {
            normalizedAngle >= normalizedStart || normalizedAngle <= normalizedEnd
        }
    }

    private fun normalizeAngle(angle: Float): Float {
        var normalized = angle % (2 * PI).toFloat()
        if (normalized < 0) normalized += (2 * PI).toFloat()
        return normalized
    }

    private fun interpolateBezier(points: List<Offset>): List<Offset> {
        if (points.size < 2) return points
        if (points.size == 2) return interpolateLinear(points)

        val result = mutableListOf<Offset>()
        val segments = 100

        for (t in 0..segments) {
            val progress = t.toFloat() / segments
            result.add(evaluateBezier(points, progress))
        }

        return result
    }

    private fun evaluateBezier(points: List<Offset>, t: Float): Offset {
        var tempPoints = points.toMutableList()

        while (tempPoints.size > 1) {
            val newPoints = mutableListOf<Offset>()
            for (i in 0 until tempPoints.size - 1) {
                newPoints.add(Offset(
                    tempPoints[i].x + (tempPoints[i + 1].x - tempPoints[i].x) * t,
                    tempPoints[i].y + (tempPoints[i + 1].y - tempPoints[i].y) * t
                ))
            }
            tempPoints = newPoints
        }

        return tempPoints[0]
    }

    private fun interpolateCatmullRom(points: List<Offset>): List<Offset> {
        if (points.size < 2) return points
        if (points.size == 2) return interpolateLinear(points)

        val result = mutableListOf<Offset>()

        for (i in 0 until points.size - 1) {
            val p0 = if (i > 0) points[i - 1] else points[i]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 < points.size) points[i + 2] else points[i + 1]

            val segments = 100
            for (t in 0..segments) {
                val tNorm = t.toFloat() / segments
                result.add(catmullRom(p0, p1, p2, p3, tNorm))
            }
        }

        return result
    }

    private fun catmullRom(p0: Offset, p1: Offset, p2: Offset, p3: Offset, t: Float): Offset {
        val t2 = t * t
        val t3 = t2 * t

        val x = 0.5f * (
                (2f * p1.x) +
                        (-p0.x + p2.x) * t +
                        (2f * p0.x - 5f * p1.x + 4f * p2.x - p3.x) * t2 +
                        (-p0.x + 3f * p1.x - 3f * p2.x + p3.x) * t3
                )

        val y = 0.5f * (
                (2f * p1.y) +
                        (-p0.y + p2.y) * t +
                        (2f * p0.y - 5f * p1.y + 4f * p2.y - p3.y) * t2 +
                        (-p0.y + 3f * p1.y - 3f * p2.y + p3.y) * t3
                )

        return Offset(x, y)
    }

    fun getSliderEndPosition(note: Note): Offset {
        if (note.curvePoints.isEmpty()) {
            return Offset(note.x, note.y)
        }

        return if (note.slides % 2 == 1) {
            val lastPoint = note.curvePoints.last()
            Offset(lastPoint.first, lastPoint.second)
        } else {
            Offset(note.x, note.y)
        }
    }

    fun checkSliderFollowing(
        touchPosition: Offset?,
        note: Note,
        sliderProgress: Float,
        screenWidth: Float,
        screenHeight: Float
    ): Boolean {
        if (touchPosition == null) return false

        val (scale, screenOffset) = calculateScaleAndOffset(screenWidth, screenHeight)
        val (scaleX, scaleY) = scale
        val (screenOffsetX, screenOffsetY) = screenOffset

        val targetPos = getSliderPositionAtProgress(note, sliderProgress)
        val scaledTargetPos = Offset(
            targetPos.x * scaleX + screenOffsetX,
            targetPos.y * scaleY + screenOffsetY
        )

        val dx = touchPosition.x - scaledTargetPos.x
        val dy = touchPosition.y - scaledTargetPos.y
        val distance = sqrt(dx * dx + dy * dy)

        return distance <= 180f
    }

    fun handleSliderStart(
        offset: Offset,
        activeNotes: List<ActiveNote>,
        currentTime: Long,
        screenWidth: Float,
        screenHeight: Float,
        beatmap: Beatmap,
        onHit: (ActiveNote, HitResult) -> Unit
    ) {
        val (scale, screenOffset) = calculateScaleAndOffset(screenWidth, screenHeight)
        val (scaleX, scaleY) = scale
        val (screenOffsetX, screenOffsetY) = screenOffset

        val tappedSlider = activeNotes
            .filter { it.note.type == NoteType.SLIDER && !it.isHit }
            .minByOrNull { activeNote ->
                val noteScreenX = activeNote.note.x * scaleX + screenOffsetX
                val noteScreenY = activeNote.note.y * scaleY + screenOffsetY
                val dx = offset.x - noteScreenX
                val dy = offset.y - noteScreenY
                sqrt(dx * dx + dy * dy)
            }

        tappedSlider?.let { activeNote ->
            val noteScreenX = activeNote.note.x * scaleX + screenOffsetX
            val noteScreenY = activeNote.note.y * scaleY + screenOffsetY

            val dx = offset.x - noteScreenX
            val dy = offset.y - noteScreenY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= 150f) {
                val timeDiff = abs(currentTime - activeNote.note.time)

                if (timeDiff <= beatmap.hitWindowGood) {
                    val hitResult = when {
                        timeDiff <= beatmap.hitWindowPerfect -> HitResult.PERFECT
                        timeDiff <= beatmap.hitWindowGreat -> HitResult.GREAT
                        else -> HitResult.GOOD
                    }

                    Log.d(TAG, "Slider start result: $hitResult (timeDiff=$timeDiff)")

                    onHit(activeNote, hitResult)
                }
            }
        }
    }

    fun handleSliderRelease(
        activeNote: ActiveNote,
        currentTime: Long,
        onComplete: (HitResult, Offset) -> Unit
    ) {
        if (!activeNote.sliderCompleted && activeNote.isHit) {
            val sliderDuration = activeNote.note.endTime - activeNote.note.time
            val followRatio = activeNote.followTime.toFloat() / sliderDuration.toFloat()

            val hitResult = when {
                followRatio >= 0.9f && activeNote.sliderProgress >= 0.95f -> HitResult.PERFECT
                followRatio >= 0.7f && activeNote.sliderProgress >= 0.8f -> HitResult.GREAT
                followRatio >= 0.5f && activeNote.sliderProgress >= 0.6f -> HitResult.GOOD
                else -> HitResult.MISS
            }

            Log.d(TAG, "Slider release: followRatio=$followRatio, " +
                    "sliderProgress=${activeNote.sliderProgress}, " +
                    "result=$hitResult")

            activeNote.sliderCompleted = true
            activeNote.sliderCompleteTime = currentTime

            val endPos = getSliderEndPosition(activeNote.note)
            onComplete(hitResult, endPos)
        }
    }

    fun clearCache() {
        arcLengthCache.clear()
    }
}