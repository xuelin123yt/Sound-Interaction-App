#include <jni.h>
#include <cmath>
#include <vector>
#include <android/log.h>
#include <cstdlib>

#define LOG_TAG "C++GameEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// --- 遊戲常數參數 ---
const float GRAVITY = 1.2f;           // 重力加速度
const float LIFT = -20.0f;            // 跳躍力度
const float FLOOR_Y = 2000.0f;        // 地板高度
const float BIRD_X = 300.0f;          // 鳥的固定 X 軸位置
const float BIRD_RADIUS = 70.0f;      // 鳥的碰撞半徑

// --- 障礙物設定 ---
const float PIPE_SPEED = 10.0f;       // 管子移動速度
const float COLLISION_PIPE_WIDTH = 300.0f; // 管子寬度
const float GAME_WIDTH = 4000.0f;     // 遊戲世界生成寬度

// --- 全域變數 ---
float birdY = 1000.0f;
float birdVelocity = 0.0f;
int score = 0;
int currentHp = 100;
const int MAX_HP = 100;

bool isGameOver = false;
bool isVictory = false;

// ✅ 改進的音量觸發跳躍設定
const float BASE_VOLUME_THRESHOLD = 800.0f;  // 基礎閾值
float dynamicThreshold = BASE_VOLUME_THRESHOLD;  // 動態閾值（會自動調整）
int flapCooldown = 0;
int loudFrameCount = 0;                 // 連續大聲的幀數
const int MIN_LOUD_FRAMES = 4;          // 至少要連續 4 幀才觸發（加強過濾）
int sfxMuteFrames = 0;                  // 音效播放期間靜音的幀數

// ✅ 背景噪音自動校準
float backgroundNoiseLevel = 0.0f;      // 背景噪音水平
int noiseCalibrationFrames = 0;         // 校準計數器
const int CALIBRATION_FRAMES = 30;      // 前 30 幀用於校準
const float NOISE_MULTIPLIER = 2.5f;    // 閾值必須超過背景噪音的 2.5 倍

// --- 結構體定義 ---
struct Obstacle {
    float x;                // 管子左側的 X 座標
    float gapY;             // 縫隙中心點的 Y 座標
    float gapHeight;        // 縫隙的高度
    bool passed;            // 是否已通過加分
    float nextSpawnDistance;// 下一根管子的生成距離
    bool hasCollided;       // 是否已經扣過血
};

std::vector<Obstacle> obstacles;

// --- JNI 函數實作 ---

// 1. 初始化遊戲
extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_initGame(JNIEnv* env, jobject) {
    birdY = 1000.0f;
    birdVelocity = 0.0f;
    score = 0;
    currentHp = MAX_HP;
    isGameOver = false;
    isVictory = false;
    flapCooldown = 0;
    loudFrameCount = 0;  // ✅ 重置音訊計數
    sfxMuteFrames = 0;   // ✅ 重置音效靜音計數

    // ✅ 重置背景噪音校準
    backgroundNoiseLevel = 0.0f;
    noiseCalibrationFrames = 0;
    dynamicThreshold = BASE_VOLUME_THRESHOLD;

    obstacles.clear();

    // 生成第一根管子（調整後的縫隙高度）
    float firstGapY = 700.0f + (rand() % 600);
    float firstGapH = (BIRD_RADIUS * 2 * 4.5f);  // ✅ 改成 4.5 倍（原本 3.0）
    float firstDist = 1000.0f + (rand() % 800);

    obstacles.push_back({GAME_WIDTH, firstGapY, firstGapH, false, firstDist, false});
}

// 2. 玩家跳躍
extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_flap(JNIEnv* env, jobject) {
    if (!isGameOver) {
        birdVelocity = LIFT;
    }
}

// 3. 遊戲核心更新 (包含防抖動與防堆疊邏輯)
extern "C" JNIEXPORT jfloat JNICALL
Java_com_soundinteractionapp_GameEngine_updateGame(JNIEnv* env, jobject) {
    if (isGameOver) return birdY;

    // --- A. 物理計算 (重力) ---
    birdVelocity += GRAVITY;
    birdY += birdVelocity;

    // 地板與天花板限制
    if (birdY > FLOOR_Y) birdY = FLOOR_Y;
    if (birdY < 0.0f) { birdY = 0.0f; birdVelocity = 0; }
    if (flapCooldown > 0) flapCooldown--;
    if (sfxMuteFrames > 0) sfxMuteFrames--;  // ✅ 減少音效靜音計數

    // --- B. 碰撞檢測與全域位移計算 ---
    bool isBlocked = false;        // 是否被擋住 (決定是否停止捲動)
    float globalCorrection = 0.0f; // 全域修正量 (防止穿透)

    for (auto& obs : obstacles) {
        float pipeLeft = obs.x;
        float pipeRight = obs.x + COLLISION_PIPE_WIDTH;

        // X 軸範圍判定
        bool inPipeRangeX = (BIRD_X + BIRD_RADIUS > pipeLeft) && (BIRD_X - BIRD_RADIUS < pipeRight);

        if (inPipeRangeX) {
            float currentGap = obs.gapHeight;
            float gapTop = obs.gapY - currentGap / 2;
            float gapBottom = obs.gapY + currentGap / 2;

            // Y 軸碰撞判定 (撞到實體)
            bool hitSolid = (birdY - BIRD_RADIUS < gapTop) || (birdY + BIRD_RADIUS > gapBottom);

            if (hitSolid) {
                // 扣血邏輯
                if (!obs.hasCollided) {
                    currentHp -= 5;
                    obs.hasCollided = true;
                    sfxMuteFrames = 20;  // ✅ 碰撞音效播放，靜音 20 幀（約 0.3-0.4 秒）
                    if (currentHp <= 0) {
                        currentHp = 0;
                        isGameOver = true;
                    }
                }

                // --- 阻擋判斷 ---
                // 判斷是否撞到管子「正面」(鳥在管子左側邊緣)
                // +20.0f 是一個緩衝值，確保不會誤判內部碰撞
                if (BIRD_X < pipeLeft + 20.0f) {
                    isBlocked = true;

                    // ★★★ [防水平抖動] 計算精確修正量 ★★★
                    // 目標位置：鳥的右緣剛好貼著管子左緣
                    float targetX = BIRD_X + BIRD_RADIUS;

                    // 如果管子目前的 X 比目標小 (代表穿透進去了)，或者非常接近
                    // 只要有一點點誤差，我們都計算修正量，讓它穩穩停在 targetX
                    if (obs.x < targetX + 0.5f) {
                        float diff = targetX - obs.x;
                        if (diff > globalCorrection) {
                            globalCorrection = diff;
                        }
                    }
                }
                else {
                    // --- 內部碰撞 (上下壁) ---
                    // ★★★ [防垂直抖動] 速度抑制邏輯 ★★★

                    if (birdY - BIRD_RADIUS < gapTop) { // 撞到上管底部
                        birdY = gapTop + BIRD_RADIUS + 0.1f; // 輕微推開，避免浮點數重疊

                        // 如果速度很快才反彈，速度慢就直接歸零 (吸附效果)
                        if (birdVelocity < -5.0f) {
                            birdVelocity = -birdVelocity * 0.5f; // 彈力減半
                        } else {
                            birdVelocity = 0.0f; // 停止垂直移動，消除抖動
                        }
                    }

                    if (birdY + BIRD_RADIUS > gapBottom) { // 撞到下管頂部
                        birdY = gapBottom - BIRD_RADIUS - 0.1f; // 輕微推開

                        if (birdVelocity > 5.0f) {
                            birdVelocity = -birdVelocity * 0.5f; // 彈力減半
                        } else {
                            birdVelocity = 0.0f; // 停止垂直移動，消除抖動
                        }
                    }
                }
            }
        }
    }

    // --- C. 移動障礙物 (解決堆疊問題) ---

    // 情況 1: 被擋住 (Blocked)
    if (isBlocked) {
        // 停止世界捲動 (不執行 x -= SPEED)

        // ★★★ 應用全域修正 (Global Correction) ★★★
        // 將「所有」管子一起往後推修正量，保持相對距離，解決堆疊問題
        if (globalCorrection > 0.0f) {
            for (auto& obs : obstacles) {
                obs.x += globalCorrection;
            }
        }
    }
        // 情況 2: 沒被擋住 (Normal)
    else {
        // 正常向左捲動
        for (auto& obs : obstacles) {
            obs.x -= PIPE_SPEED;
        }
    }

    // 加分檢測
    for (auto& obs : obstacles) {
        if (!obs.passed && obs.x + COLLISION_PIPE_WIDTH < BIRD_X - BIRD_RADIUS) {
            score += 100;
            obs.passed = true;
            sfxMuteFrames = 20;  // ✅ 加分音效播放，靜音 20 幀
        }
    }

    // --- D. 生成新管子 ---
    // 只在沒有被擋住的情況下檢查生成，避免卡住時重複生成
    if (!isBlocked && !obstacles.empty()) {
        float currentSpawnDist = obstacles.back().nextSpawnDistance;
        if (obstacles.back().x < (GAME_WIDTH - currentSpawnDist)) {
            float minGapY = 700.0f;
            float range = 600.0f;
            float randomGapY = minGapY + (rand() % (int)range);
            float birdDiameter = BIRD_RADIUS * 2;

            // ✅ 調整縫隙高度倍數（原本 2.5, 3.0, 4.0 → 改成 4.5, 5.0, 5.5）
            float multipliers[] = {4.5f, 5.0f, 5.5f};
            int randomIndex = rand() % 3;
            float randomGapH = birdDiameter * multipliers[randomIndex];

            float nextDist = 1000.0f + (rand() % 800);

            obstacles.push_back({obstacles.back().x + currentSpawnDist, randomGapY, randomGapH, false, nextDist, false});
        }
    }

    // 移除超出畫面的管子
    if (!obstacles.empty() && obstacles[0].x < -COLLISION_PIPE_WIDTH) {
        obstacles.erase(obstacles.begin());
    }

    return birdY;
}

// 4. 傳送遊戲狀態
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_soundinteractionapp_GameEngine_getGameState(JNIEnv* env, jobject) {
    jfloatArray result = env->NewFloatArray(5);
    float temp[5];
    temp[0] = (float)score;
    temp[1] = 0.0f;
    temp[2] = isGameOver ? 1.0f : 0.0f;
    temp[3] = isVictory ? 1.0f : 0.0f;
    temp[4] = (float)currentHp;
    env->SetFloatArrayRegion(result, 0, 5, temp);
    return result;
}

// 5. 傳送障礙物數據
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_soundinteractionapp_GameEngine_getObstacleData(JNIEnv* env, jobject) {
    int size = obstacles.size() * 3;
    jfloatArray result = env->NewFloatArray(size);
    std::vector<float> tempList;
    for (const auto& obs : obstacles) {
        tempList.push_back(obs.x);
        tempList.push_back(obs.gapY);
        tempList.push_back(obs.gapHeight);
    }
    env->SetFloatArrayRegion(result, 0, size, tempList.data());
    return result;
}

// 6. ✅ 改進的音訊處理 - 過濾環境噪音、遊戲音效、背景音樂
extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_processAudio(JNIEnv* env, jobject, jshortArray audioData, jint size) {
    jshort* audioPtr = env->GetShortArrayElements(audioData, nullptr);
    long sum = 0;
    for (int i = 0; i < size; i++) {
        sum += audioPtr[i] * audioPtr[i];
    }
    float rms = sqrt(sum / size);

    // ★★★ 背景噪音自動校準（遊戲開始前 30 幀） ★★★
    if (noiseCalibrationFrames < CALIBRATION_FRAMES) {
        // 收集前 30 幀的平均音量作為背景噪音基準
        backgroundNoiseLevel += rms;
        noiseCalibrationFrames++;

        if (noiseCalibrationFrames == CALIBRATION_FRAMES) {
            // 計算平均背景噪音
            backgroundNoiseLevel /= CALIBRATION_FRAMES;
            // 設定動態閾值 = 背景噪音 × 倍數，但不低於基礎閾值
            dynamicThreshold = fmax(backgroundNoiseLevel * NOISE_MULTIPLIER, BASE_VOLUME_THRESHOLD);
            LOGD("背景噪音校準完成: %.2f, 動態閾值: %.2f", backgroundNoiseLevel, dynamicThreshold);
        }

        env->ReleaseShortArrayElements(audioData, audioPtr, 0);
        return;
    }

    // ★★★ 改進的觸發邏輯 ★★★
    // 1. 使用動態閾值（自動適應背景音樂音量）
    // 2. 需要連續 4 幀都超過閾值才觸發（強力過濾背景噪音）
    // 3. 增加冷卻時間 (15 幀，避免連續誤觸發)
    // 4. 音效播放期間忽略麥克風輸入（避免遊戲音效觸發）

    // 音效播放期間不處理麥克風輸入
    if (sfxMuteFrames > 0) {
        loudFrameCount = 0;  // 重置計數
        env->ReleaseShortArrayElements(audioData, audioPtr, 0);
        return;
    }

    if (rms > dynamicThreshold) {
        loudFrameCount++;
        // 連續達到最小幀數且冷卻完成才觸發
        if (loudFrameCount >= MIN_LOUD_FRAMES && flapCooldown == 0 && !isGameOver) {
            birdVelocity = LIFT;
            flapCooldown = 15;  // 增加冷卻時間到 15 幀
            loudFrameCount = 0;
        }
    } else {
        // 音量低於閾值就重置計數
        loudFrameCount = 0;
    }

    env->ReleaseShortArrayElements(audioData, audioPtr, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_sendPitchData(JNIEnv* env, jobject, jfloat pitch) {
    // 空函數，保留供未來使用
}