package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.HitResult
import com.soundinteractionapp.screens.game.levels.level4.LevelColors
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.models.ActiveNote
import com.soundinteractionapp.screens.game.levels.level4.models.HitEffect
import com.soundinteractionapp.screens.game.levels.level4.logic.NoteHandler
import kotlin.math.*

@Composable
fun GameCanvas(
    activeNotes: List<ActiveNote>,
    hitEffects: List<HitEffect>,
    missEffects: List<HitEffect>,
    currentTime: Long,
    screenWidth: Float,
    screenHeight: Float,
    beatmap: Beatmap,
    onTouchEvent: (Offset, Boolean, Boolean) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onTouchEvent(offset, true, false)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onTouchEvent(change.position, false, false)
                    },
                    onDragEnd = {
                        onTouchEvent(Offset.Zero, false, true)
                    },
                    onDragCancel = {
                        onTouchEvent(Offset.Zero, false, true)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onTouchEvent(offset, true, false)
                    onTouchEvent(offset, false, true)
                }
            }
    ) {
        val playFieldScale = 0.85f
        val originalWidth = 512f
        val originalHeight = 384f

        val scaledWidth = originalWidth * playFieldScale
        val scaledHeight = originalHeight * playFieldScale

        val offsetX = (originalWidth - scaledWidth) / 2f
        val offsetY = (originalHeight - scaledHeight) / 2f

        val scaleX = (screenWidth / originalWidth) * playFieldScale
        val scaleY = (screenHeight / originalHeight) * playFieldScale

        val screenOffsetX = offsetX * (screenWidth / originalWidth)
        val screenOffsetY = offsetY * (screenHeight / originalHeight)

        // 繪製虛線引導線 (Follow Points)
        val sortedNotes = activeNotes
            .filter { !it.isHit && !it.isMissed }
            .sortedBy { it.note.time }

        for (i in 0 until sortedNotes.size - 1) {
            val current = sortedNotes[i]
            val next = sortedNotes[i + 1]

            if (next.note.time - current.note.time < 1000) {
                val currentX = current.note.x * scaleX + screenOffsetX
                val currentY = current.note.y * scaleY + screenOffsetY
                val nextX = next.note.x * scaleX + screenOffsetX
                val nextY = next.note.y * scaleY + screenOffsetY

                val timeUntilCurrent = current.note.time - currentTime
                val fadeProgress = 1f - (timeUntilCurrent.toFloat() / beatmap.preempt.toFloat())
                val alpha = (fadeProgress * 0.6f).coerceIn(0f, 0.6f)

                if (alpha > 0.1f) {
                    val path = Path().apply {
                        moveTo(currentX, currentY)
                        lineTo(nextX, nextY)
                    }

                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = alpha),
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(20f, 15f), 0f
                            )
                        )
                    )

                    val angle = atan2(
                        (nextY - currentY).toDouble(),
                        (nextX - currentX).toDouble()
                    ).toFloat() * (180f / PI.toFloat())

                    rotate(angle, Offset(nextX, nextY)) {
                        val arrowPath = Path().apply {
                            moveTo(nextX - 20f, nextY)
                            lineTo(nextX - 35f, nextY - 12f)
                            lineTo(nextX - 35f, nextY + 12f)
                            close()
                        }
                        drawPath(
                            path = arrowPath,
                            color = Color.White.copy(alpha = alpha),
                            style = Fill
                        )
                    }
                }
            }
        }

        activeNotes.forEach { activeNote ->
            val fadeAlpha = if (activeNote.sliderCompleted) {
                val fadeTime = currentTime - activeNote.sliderCompleteTime
                1f - (fadeTime / 300f).coerceIn(0f, 1f)
            } else if (activeNote.isMissed) {
                val fadeTime = currentTime - activeNote.missTime
                1f - (fadeTime / 300f).coerceIn(0f, 1f)
            } else {
                1f
            }

            if (fadeAlpha <= 0f) return@forEach

            val shouldDraw = when (activeNote.note.type) {
                NoteType.CIRCLE -> !activeNote.isHit || activeNote.isMissed
                NoteType.SLIDER -> !activeNote.sliderCompleted || fadeAlpha > 0f || activeNote.isMissed
                else -> false
            }

            if (shouldDraw) {
                val note = activeNote.note
                val timeUntilHit = note.time - currentTime
                val progress = 1f - (timeUntilHit.toFloat() / beatmap.preempt.toFloat())

                when (note.type) {
                    NoteType.CIRCLE -> {
                        drawCircleNote(
                            note = note,
                            noteNumber = activeNote.noteNumber,
                            progress = progress,
                            scaleX = scaleX,
                            scaleY = scaleY,
                            offsetX = screenOffsetX,
                            offsetY = screenOffsetY
                        )
                    }
                    NoteType.SLIDER -> {
                        drawSliderNote(
                            note = note,
                            noteNumber = activeNote.noteNumber,
                            progress = progress,
                            sliderProgress = activeNote.sliderProgress,
                            isActive = activeNote.isHit,
                            isFollowing = activeNote.sliderFollowing,
                            fadeAlpha = fadeAlpha,
                            currentTime = currentTime,
                            scaleX = scaleX,
                            scaleY = scaleY,
                            offsetX = screenOffsetX,
                            offsetY = screenOffsetY
                        )
                    }
                    NoteType.SPINNER -> {}
                }
            }
        }

        // 繪製 MISS 特效
        missEffects.forEach { effect ->
            val elapsed = System.currentTimeMillis() - effect.startTime
            val animProgress = (elapsed / 500f).coerceIn(0f, 1f)

            val yOffset = -100f * animProgress
            val alpha = 1f - animProgress
            val scale = 1f + animProgress * 0.5f

            val effectColor = Color(0xFFFF0000)

            val effectX = effect.position.x * scaleX + screenOffsetX
            val effectY = effect.position.y * scaleY + screenOffsetY + yOffset

            drawCircle(
                color = effectColor.copy(alpha = alpha * 0.3f),
                radius = 80f * scale,
                center = Offset(effectX, effectY),
                style = Stroke(width = 8f)
            )

            drawContext.canvas.nativeCanvas.apply {
                val text = "MISS"

                val paint = android.graphics.Paint().apply {
                    color = effectColor.toArgb()
                    this.alpha = (255 * alpha).toInt()
                    textSize = 40f * scale
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD
                    )
                    isAntiAlias = true
                    setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
                }

                drawText(text, effectX, effectY, paint)
            }
        }

        // 繪製判定文字(帶彈出動畫)
        hitEffects.forEach { effect ->
            val elapsed = System.currentTimeMillis() - effect.startTime
            val animProgress = (elapsed / 500f).coerceIn(0f, 1f)

            val yOffset = -100f * animProgress
            val alpha = 1f - animProgress
            val scale = 1f + animProgress * 0.5f

            val effectColor = when (effect.hitResult) {
                HitResult.PERFECT -> Color(0xFFFFD700)
                HitResult.GREAT -> Color(0xFF00FF00)
                HitResult.GOOD -> Color(0xFF87CEEB)
                HitResult.MISS -> Color(0xFFFF0000)
            }

            val effectX = effect.position.x * scaleX + screenOffsetX
            val effectY = effect.position.y * scaleY + screenOffsetY + yOffset

            drawCircle(
                color = effectColor.copy(alpha = alpha * 0.3f),
                radius = 80f * scale,
                center = Offset(effectX, effectY),
                style = Stroke(width = 8f)
            )

            drawContext.canvas.nativeCanvas.apply {
                val text = when (effect.hitResult) {
                    HitResult.PERFECT -> "PERFECT"
                    HitResult.GREAT -> "GREAT"
                    HitResult.GOOD -> "GOOD"
                    HitResult.MISS -> "MISS"
                }

                val paint = android.graphics.Paint().apply {
                    color = effectColor.toArgb()
                    this.alpha = (255 * alpha).toInt()
                    textSize = 40f * scale
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD
                    )
                    isAntiAlias = true
                    setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
                }

                drawText(text, effectX, effectY, paint)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCircleNote(
    note: Note,
    noteNumber: Int,
    progress: Float,
    scaleX: Float,
    scaleY: Float,
    offsetX: Float,
    offsetY: Float
) {
    val x = note.x * scaleX + offsetX
    val y = note.y * scaleY + offsetY
    val radius = 80f

    val approachScale = 1f + (3f * (1f - progress.coerceIn(0f, 1f)))
    val approachRadius = radius * approachScale
    val approachAlpha = progress.coerceIn(0f, 1f)

    drawCircle(
        color = Color(0xFF64B5F6).copy(alpha = approachAlpha * 0.8f),
        radius = approachRadius,
        center = Offset(x, y),
        style = Stroke(width = 6f)
    )

    val comboColor = LevelColors.COMBO_COLORS.getOrNull(note.comboColor)
        ?: LevelColors.COMBO_COLORS.first()

    drawCircle(
        color = Color(comboColor).copy(alpha = 0.3f),
        radius = radius * 1.3f,
        center = Offset(x, y)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(comboColor).copy(alpha = 0.9f),
                Color(comboColor)
            ),
            center = Offset(x, y),
            radius = radius
        ),
        radius = radius,
        center = Offset(x, y)
    )

    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(x, y),
        style = Stroke(width = 10f)
    )

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = radius * 0.7f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
        }

        val number = (noteNumber % 10).toString()
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(number, 0, number.length, textBounds)

        drawText(number, x, y + textBounds.height() / 2f, paint)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSliderNote(
    note: Note,
    noteNumber: Int,
    progress: Float,
    sliderProgress: Float,
    isActive: Boolean,
    isFollowing: Boolean,
    fadeAlpha: Float,
    currentTime: Long,
    scaleX: Float,
    scaleY: Float,
    offsetX: Float,
    offsetY: Float
) {
    val startX = note.x * scaleX + offsetX
    val startY = note.y * scaleY + offsetY
    val radius = 80f

    // ✅ 根據 curveType 生成正確的路徑點
    val pathPoints = generateSliderPath(note, scaleX, scaleY, offsetX, offsetY)

    if (pathPoints.size > 1) {
        val path = Path().apply {
            moveTo(pathPoints[0].x, pathPoints[0].y)
            pathPoints.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        // 繪製滑條背景
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.5f * fadeAlpha),
            style = Stroke(
                width = radius * 2.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 繪製滑條主體
        drawPath(
            path = path,
            color = Color(0xFF64B5F6).copy(alpha = 0.9f * fadeAlpha),
            style = Stroke(
                width = radius * 2.4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 繪製增強的折返箭頭
        if (note.slides > 1) {
            val endPoint = pathPoints.last()
            drawEnhancedReverseArrow(
                position = endPoint,
                previousPoint = pathPoints[pathPoints.size - 2],
                radius = radius,
                alpha = fadeAlpha,
                currentTime = currentTime
            )
        }

        // 繪製跟隨球體（帶脈動動畫）
        if (isActive && sliderProgress > 0f && sliderProgress < 1f) {
            val ballPosition = NoteHandler.getSliderPositionAtProgress(note, sliderProgress)
            val scaledBallPos = Offset(
                ballPosition.x * scaleX + offsetX,
                ballPosition.y * scaleY + offsetY
            )

            // 快速脈動動畫：週期 300ms
            val pulsePhase = (currentTime % 300) / 300f
            val pulseScale = 1f + sin(pulsePhase * 2f * PI.toFloat()) * 0.15f

            val progressColor = if (isFollowing) Color(0xFF00FF00) else Color(0xFFFF9800)

            // 外圈光暈
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        progressColor.copy(alpha = 0.5f * pulseScale * fadeAlpha),
                        Color.Transparent
                    ),
                    center = scaledBallPos,
                    radius = radius * 1.8f * pulseScale
                ),
                radius = radius * 1.8f * pulseScale,
                center = scaledBallPos
            )

            // 主球體（跟隨脈動）
            drawCircle(
                color = Color.White.copy(alpha = fadeAlpha),
                radius = radius * 0.7f * pulseScale,
                center = scaledBallPos
            )

            drawCircle(
                color = progressColor.copy(alpha = fadeAlpha),
                radius = radius * 0.5f * pulseScale,
                center = scaledBallPos
            )

            // 邊框
            drawCircle(
                color = Color.White.copy(alpha = fadeAlpha),
                radius = radius * 0.7f * pulseScale,
                center = scaledBallPos,
                style = Stroke(width = 6f)
            )
        }
    }

    // 繪製 approach circle
    if (!isActive) {
        val approachScale = 1f + (3f * (1f - progress.coerceIn(0f, 1f)))
        val approachRadius = radius * approachScale
        val approachAlpha = progress.coerceIn(0f, 1f)

        drawCircle(
            color = Color(0xFF64B5F6).copy(alpha = approachAlpha * 0.9f * fadeAlpha),
            radius = approachRadius,
            center = Offset(startX, startY),
            style = Stroke(width = 8f)
        )
    }

    // 繪製起始圓圈
    val comboColor = LevelColors.COMBO_COLORS[note.comboColor]

    drawCircle(
        color = Color(comboColor).copy(alpha = 0.3f * fadeAlpha),
        radius = radius * 1.4f,
        center = Offset(startX, startY)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(comboColor).copy(alpha = 0.9f * fadeAlpha),
                Color(comboColor).copy(alpha = fadeAlpha)
            ),
            center = Offset(startX, startY),
            radius = radius
        ),
        radius = radius,
        center = Offset(startX, startY)
    )

    drawCircle(
        color = Color.White.copy(alpha = fadeAlpha),
        radius = radius,
        center = Offset(startX, startY),
        style = Stroke(width = 10f)
    )

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = (255 * fadeAlpha).toInt()
            textSize = radius * 0.7f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
        }

        val number = (noteNumber % 10).toString()
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(number, 0, number.length, textBounds)

        drawText(number, startX, startY + textBounds.height() / 2f, paint)
    }
}

// ✅ 核心修復：根據曲線類型生成正確的路徑
private fun generateSliderPath(
    note: Note,
    scaleX: Float,
    scaleY: Float,
    offsetX: Float,
    offsetY: Float
): List<Offset> {
    val controlPoints = mutableListOf(Offset(note.x, note.y))
    note.curvePoints.forEach { pair ->
        controlPoints.add(Offset(pair.first, pair.second))
    }

    if (controlPoints.size < 2) return listOf(Offset(note.x * scaleX + offsetX, note.y * scaleY + offsetY))

    // ✅ 根據不同的曲線類型使用不同的插值方法
    val interpolatedPoints = when (note.curveType) {
        CurveType.LINEAR -> interpolateLinear(controlPoints)
        CurveType.PERFECT -> interpolatePerfectCircle(controlPoints)
        CurveType.BEZIER -> interpolateBezier(controlPoints)
        CurveType.CATMULL -> interpolateCatmullRom(controlPoints)
    }

    // 縮放到螢幕座標
    return interpolatedPoints.map { point ->
        Offset(point.x * scaleX + offsetX, point.y * scaleY + offsetY)
    }
}

// ✅ LINEAR: 直線插值
private fun interpolateLinear(points: List<Offset>): List<Offset> {
    val result = mutableListOf<Offset>()
    val segments = 50

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

// ✅ PERFECT: 圓弧插值（通過3點的圓）
private fun interpolatePerfectCircle(points: List<Offset>): List<Offset> {
    if (points.size < 3) return interpolateLinear(points)

    val p1 = points[0]
    val p2 = points[1]
    val p3 = points[2]

    // 計算圓心和半徑
    val circle = calculateCircleFrom3Points(p1, p2, p3)
    if (circle == null) {
        // 如果三點共線，退化為直線
        return interpolateLinear(points)
    }

    val (center, radius) = circle

    // 計算起始和結束角度
    val startAngle = atan2(p1.y - center.y, p1.x - center.x)
    val endAngle = atan2(p3.y - center.y, p3.x - center.x)

    // 確定順時針還是逆時針
    val midAngle = atan2(p2.y - center.y, p2.x - center.x)
    val isClockwise = isAngleBetween(midAngle, startAngle, endAngle)

    var angleSpan = if (isClockwise) {
        if (endAngle > startAngle) endAngle - startAngle else (2 * PI + endAngle - startAngle).toFloat()
    } else {
        if (startAngle > endAngle) startAngle - endAngle else (2 * PI + startAngle - endAngle).toFloat()
    }

    // 限制最大角度為 2π
    if (angleSpan > 2 * PI) angleSpan = (2 * PI).toFloat()

    val result = mutableListOf<Offset>()
    val segments = max(50, (angleSpan * 50).toInt())

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

// 計算通過三點的圓
private fun calculateCircleFrom3Points(p1: Offset, p2: Offset, p3: Offset): Pair<Offset, Float>? {
    val ax = p1.x
    val ay = p1.y
    val bx = p2.x
    val by = p2.y
    val cx = p3.x
    val cy = p3.y

    val d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by))
    if (abs(d) < 0.001f) return null // 三點共線

    val ux = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d
    val uy = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d

    val radius = sqrt((ax - ux) * (ax - ux) + (ay - uy) * (ay - uy))

    return Pair(Offset(ux, uy), radius)
}

// 判斷角度是否在區間內
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

// ✅ BEZIER: 貝茲曲線插值
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

// De Casteljau 演算法計算 Bezier 曲線上的點
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

// ✅ CATMULL: Catmull-Rom 曲線插值
private fun interpolateCatmullRom(points: List<Offset>): List<Offset> {
    if (points.size < 2) return points
    if (points.size == 2) return interpolateLinear(points)

    val result = mutableListOf<Offset>()

    for (i in 0 until points.size - 1) {
        val p0 = if (i > 0) points[i - 1] else points[i]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i + 2 < points.size) points[i + 2] else points[i + 1]

        val segments = 50
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

// 增強的折返箭頭
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEnhancedReverseArrow(
    position: Offset,
    previousPoint: Offset,
    radius: Float,
    alpha: Float,
    currentTime: Long
) {
    val angle = atan2(
        (position.y - previousPoint.y).toDouble(),
        (position.x - previousPoint.x).toDouble()
    ).toFloat() * (180f / PI.toFloat())

    // 脈動效果
    val pulsePhase = (currentTime % 500) / 500f
    val pulseScale = 1f + sin(pulsePhase * 2f * PI.toFloat()) * 0.2f

    rotate(angle, position) {
        // 黃色發光背景
        val glowPath = Path().apply {
            moveTo(position.x, position.y)
            lineTo(position.x - 45f * pulseScale, position.y - 30f * pulseScale)
            lineTo(position.x - 45f * pulseScale, position.y + 30f * pulseScale)
            close()
        }

        drawPath(
            path = glowPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.8f * alpha),
                    Color(0xFFFFD700).copy(alpha = 0.3f * alpha)
                ),
                center = position,
                radius = 60f * pulseScale
            )
        )

        // 主箭頭
        val arrowPath = Path().apply {
            moveTo(position.x, position.y)
            lineTo(position.x - 40f * pulseScale, position.y - 25f * pulseScale)
            lineTo(position.x - 40f * pulseScale, position.y + 25f * pulseScale)
            close()
        }

        drawPath(
            path = arrowPath,
            color = Color.White.copy(alpha = alpha),
            style = Fill
        )

        // 邊框
        drawPath(
            path = arrowPath,
            color = Color(0xFFFFD700).copy(alpha = alpha),
            style = Stroke(width = 5f * pulseScale)
        )
    }
}