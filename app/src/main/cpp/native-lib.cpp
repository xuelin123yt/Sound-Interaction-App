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
const float PIPE_SPEED = 18.0f;       // 管子移動速度
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

// 音量觸發跳躍設定
const float VOLUME_THRESHOLD = 200.0f;
int flapCooldown = 0;

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

    obstacles.clear();

    // 生成第一根管子
    float firstGapY = 700.0f + (rand() % 600);
    float firstGapH = (BIRD_RADIUS * 2 * 3.0f);
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
            float multipliers[] = {2.5f, 3.0f, 4.0f};
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

// 6. 音訊處理
extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_processAudio(JNIEnv* env, jobject, jshortArray audioData, jint size) {
    jshort* audioPtr = env->GetShortArrayElements(audioData, nullptr);
    long sum = 0;
    for (int i = 0; i < size; i++) {
        sum += audioPtr[i] * audioPtr[i];
    }
    float rms = sqrt(sum / size);

    if (rms > VOLUME_THRESHOLD) {
        if (flapCooldown == 0 && !isGameOver) {
            birdVelocity = LIFT;
            flapCooldown = 8;
        }
    }
    env->ReleaseShortArrayElements(audioData, audioPtr, 0);
}

extern "C" JNIEXPORT void JNICALL Java_com_soundinteractionapp_GameEngine_sendPitchData(JNIEnv* env, jobject, jfloat) {}