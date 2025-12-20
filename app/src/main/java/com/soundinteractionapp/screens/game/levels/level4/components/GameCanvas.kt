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
    starEffects: List<StarEffect> = emptyList(),  // ✅ 新增這行
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

        // ✅ 繪製星星特效（AUTO 模式） - 低調版
        starEffects.forEach { effect ->
            val elapsed = System.currentTimeMillis() - effect.startTime
            val animProgress = (elapsed / 600f).coerceIn(0f, 1f)  // ✅ 從 800ms 減少到 600ms

            val alpha = (1f - animProgress) * 0.6f  // ✅ 最大透明度降低到 60%
            val scale = 1f + animProgress * 1.2f    // ✅ 縮放從 3x 降低到 2.2x
            val rotation = animProgress * 180f      // ✅ 旋轉從 360° 降低到 180°

            val effectX = effect.position.x
            val effectY = effect.position.y - 35f * animProgress  // ✅ 向上飄動從 50px 降低到 35px

            // 繪製旋轉的星星
            rotate(rotation, Offset(effectX, effectY)) {
                val starSize = 60f * scale  // ✅ 基礎大小從 60f 降低到 40f

                // ✅ 拖尾效果減少到 2 層
                for (i in 1..2) {
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = alpha * 0.08f * (3 - i)),  // ✅ 透明度降低
                        radius = starSize * (1f + i * 0.2f),  // ✅ 擴散範圍縮小
                        center = Offset(effectX, effectY)
                    )
                }

                // 繪製星星主體（五角星）
                val starPath = Path().apply {
                    val points = 5
                    val outerRadius = starSize
                    val innerRadius = starSize * 0.4f

                    for (i in 0 until points * 2) {
                        val angle = (i * PI / points).toFloat() - PI.toFloat() / 2
                        val radius = if (i % 2 == 0) outerRadius else innerRadius
                        val x = effectX + radius * cos(angle)
                        val y = effectY + radius * sin(angle)

                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                // ✅ 漸層填充（透明度降低）
                drawPath(
                    path = starPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = alpha * 0.8f),  // ✅ 中心亮度降低
                            Color(0xFFFFD700).copy(alpha = alpha * 0.6f)   // ✅ 邊緣亮度降低
                        ),
                        center = Offset(effectX, effectY),
                        radius = starSize
                    )
                )

                // ✅ 金色邊框（更細）
                drawPath(
                    path = starPath,
                    color = Color(0xFFFFD700).copy(alpha = alpha * 0.7f),  // ✅ 透明度降低
                    style = Stroke(width = 2f)  // ✅ 線寬從 3f 降低到 2f
                )
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

    val pathPoints = generateSliderPath(note, scaleX, scaleY, offsetX, offsetY)

    if (pathPoints.size > 1) {
        val path = Path().apply {
            moveTo(pathPoints[0].x, pathPoints[0].y)
            pathPoints.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.5f * fadeAlpha),
            style = Stroke(
                width = radius * 2.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawPath(
            path = path,
            color = Color(0xFF64B5F6).copy(alpha = 0.9f * fadeAlpha),
            style = Stroke(
                width = radius * 2.4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // ✅ 先取得 comboColor（在繪製判定圈之前）
        val comboColor = LevelColors.COMBO_COLORS.getOrNull(note.comboColor)
            ?: LevelColors.COMBO_COLORS.first()

        // ✅ 繪製折返點/終點的多層判定圈
        if (note.slides > 1) {
            val endPoint = pathPoints.last()
            val secondLastPoint = if (pathPoints.size >= 2) {
                pathPoints[pathPoints.size - 2]
            } else {
                pathPoints.first()
            }

            drawEnhancedJudgmentCircle(
                position = endPoint,
                previousPoint = secondLastPoint,
                radius = radius,
                alpha = fadeAlpha,
                currentTime = currentTime,
                isReverse = true,
                comboColor = comboColor
            )
        } else {
            val endPoint = pathPoints.last()
            val secondLastPoint = if (pathPoints.size >= 2) {
                pathPoints[pathPoints.size - 2]
            } else {
                pathPoints.first()
            }

            drawEnhancedJudgmentCircle(
                position = endPoint,
                previousPoint = secondLastPoint,
                radius = radius,
                alpha = fadeAlpha,
                currentTime = currentTime,
                isReverse = false,
                comboColor = comboColor
            )
        }

        // ✅ 滑條球的繪製（改為使用主題色）
        if (isActive && sliderProgress > 0f && sliderProgress < 1f) {
            val ballPosition = NoteHandler.getSliderPositionAtProgress(note, sliderProgress)
            val scaledBallPos = Offset(
                ballPosition.x * scaleX + offsetX,
                ballPosition.y * scaleY + offsetY
            )

            val pulsePhase = (currentTime % 300) / 300f
            val pulseScale = 1f + sin(pulsePhase * 2f * PI.toFloat()) * 0.15f

            // ✅ 使用主題色，根據是否跟隨調整亮度
            val baseColor = Color(comboColor)
            val progressColor = if (isFollowing) {
                // 跟隨時：使用較亮的主題色
                Color(
                    red = (baseColor.red * 1.2f).coerceIn(0f, 1f),
                    green = (baseColor.green * 1.2f).coerceIn(0f, 1f),
                    blue = (baseColor.blue * 1.2f).coerceIn(0f, 1f),
                    alpha = 1f
                )
            } else {
                // 未跟隨時：使用原始主題色
                baseColor
            }

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

            drawCircle(
                color = Color.White.copy(alpha = fadeAlpha),
                radius = radius * 0.7f * pulseScale,
                center = scaledBallPos,
                style = Stroke(width = 6f)
            )
        }
    }

    // 起點圓圈繪製
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

    val comboColor = LevelColors.COMBO_COLORS.getOrNull(note.comboColor)
        ?: LevelColors.COMBO_COLORS.first()

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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEnhancedJudgmentCircle(
    position: Offset,
    previousPoint: Offset,
    radius: Float,
    alpha: Float,
    currentTime: Long,
    isReverse: Boolean,
    comboColor: Long
) {
    val angle = atan2(
        (position.y - previousPoint.y).toDouble(),
        (position.x - previousPoint.x).toDouble()
    ).toFloat() * (180f / PI.toFloat())

    val pulsePhase = (currentTime % 800) / 800f
    val pulseScale = 1f + sin(pulsePhase * 2f * PI.toFloat()) * 0.08f

    val circleColor = Color(comboColor)

    val glowRadius = radius * 2.2f * pulseScale
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                circleColor.copy(alpha = 0.4f * alpha),
                circleColor.copy(alpha = 0.15f * alpha),
                Color.Transparent
            ),
            center = position,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = position
    )

    drawCircle(
        color = circleColor.copy(alpha = 0.9f * alpha),
        radius = radius * 1.5f * pulseScale,
        center = position,
        style = Stroke(width = 10f)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.8f * alpha),
        radius = radius * 1.2f,
        center = position,
        style = Stroke(width = 6f)
    )

    if (isReverse) {
        rotate(angle + 180f, position) {
            val triangleSize = radius * 0.5f * pulseScale

            val leftTrianglePath = Path().apply {
                moveTo(position.x - 15f, position.y)
                lineTo(position.x - 15f - triangleSize, position.y - triangleSize * 0.8f)
                lineTo(position.x - 15f - triangleSize, position.y + triangleSize * 0.8f)
                close()
            }

            val rightTrianglePath = Path().apply {
                moveTo(position.x + 15f, position.y)
                lineTo(position.x + 15f - triangleSize, position.y - triangleSize * 0.8f)
                lineTo(position.x + 15f - triangleSize, position.y + triangleSize * 0.8f)
                close()
            }

            drawPath(
                path = leftTrianglePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        circleColor.copy(alpha = 0.6f * alpha)
                    ),
                    center = position,
                    radius = triangleSize * 2
                )
            )

            drawPath(
                path = rightTrianglePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        circleColor.copy(alpha = 0.6f * alpha)
                    ),
                    center = position,
                    radius = triangleSize * 2
                )
            )

            drawPath(
                path = leftTrianglePath,
                color = circleColor.copy(alpha = alpha),
                style = Stroke(width = 3f * pulseScale)
            )

            drawPath(
                path = rightTrianglePath,
                color = circleColor.copy(alpha = alpha),
                style = Stroke(width = 3f * pulseScale)
            )
        }
    } else {
        drawCircle(
            color = circleColor.copy(alpha = 0.8f * alpha),
            radius = radius * 0.3f * pulseScale,
            center = position
        )

        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius * 0.2f,
            center = position
        )
    }

    val extraGlowPhase = ((currentTime % 1200) / 1200f)
    val extraGlowAlpha = (1f - extraGlowPhase) * 0.3f * alpha
    val extraGlowScale = 1f + extraGlowPhase * 0.5f

    drawCircle(
        color = circleColor.copy(alpha = extraGlowAlpha),
        radius = radius * 1.5f * extraGlowScale,
        center = position,
        style = Stroke(width = 4f)
    )
}

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

    val interpolatedPoints = when (note.curveType) {
        CurveType.LINEAR -> interpolateLinear(controlPoints)
        CurveType.PERFECT -> interpolatePerfectCircle(controlPoints)
        CurveType.BEZIER -> interpolateBezier(controlPoints)
        CurveType.CATMULL -> interpolateCatmullRom(controlPoints)
    }

    return interpolatedPoints.map { point ->
        Offset(point.x * scaleX + offsetX, point.y * scaleY + offsetY)
    }
}

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