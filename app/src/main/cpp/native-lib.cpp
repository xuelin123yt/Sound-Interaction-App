#include <jni.h>
#include <cmath>
#include <vector>
#include <android/log.h>
#include <cstdlib>

#define LOG_TAG "C++GameEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// --- 遊戲參數 ---
const float GRAVITY = 1.2f;
const float LIFT = -20.0f;
const float FLOOR_Y = 2000.0f;
const float BIRD_X = 300.0f;
const float BIRD_RADIUS = 40.0f;

// --- 障礙物設定 ---
const float PIPE_SPEED = 18.0f;
const float PIPE_WIDTH = 150.0f;
// const float SPAWN_DISTANCE = 900.0f; // ★ 移除這個固定的，改成動態變數
const float GAME_WIDTH = 4000.0f;

// --- 變數 ---
float birdY = 1000.0f;
float birdVelocity = 0.0f;
int score = 0;
float gameTime = 180.0f;
bool isGameOver = false;
bool isVictory = false;
bool isColliding = false;

struct Obstacle {
    float x;
    float gapY;
    float gapHeight;
    bool passed;
    float nextSpawnDistance; // ★ 新增：這根管子跟下一根的距離
};

std::vector<Obstacle> obstacles;

// --- JNI 函數 ---

extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_initGame(JNIEnv* env, jobject) {
    birdY = 1000.0f;
    birdVelocity = 0.0f;
    score = 0;
    gameTime = 180.0f;
    isGameOver = false;
    isVictory = false;
    isColliding = false;

    obstacles.clear();

    // 第一根管子
    float firstGapY = 700.0f + (rand() % 600);
    float firstGapH = (BIRD_RADIUS * 2 * 3.0f);

    // ★ 隨機決定第一根管子跟下一根的距離 (600 ~ 1200)
    // 最小 600 確保有足夠空間讓球飛進去
    float firstDist = 600.0f + (rand() % 600);

    obstacles.push_back({GAME_WIDTH, firstGapY, firstGapH, false, firstDist});
}

extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_flap(JNIEnv* env, jobject) {
    birdVelocity = LIFT;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_soundinteractionapp_GameEngine_updateGame(JNIEnv* env, jobject) {
    if (isGameOver || isVictory) return birdY;

    // 倒數
    gameTime -= 0.016f;
    if (gameTime <= 0) isVictory = true;

    // 物理
    birdVelocity += GRAVITY;
    birdY += birdVelocity;

    if (birdY > FLOOR_Y) birdY = FLOOR_Y;
    if (birdY < 0.0f) { birdY = 0.0f; birdVelocity = 0; }

    // 碰撞檢測
    isColliding = false;
    for (auto& obs : obstacles) {
        if (BIRD_X + BIRD_RADIUS > obs.x && BIRD_X - BIRD_RADIUS < obs.x + PIPE_WIDTH) {
            float currentGap = obs.gapHeight;
            float gapTop = obs.gapY - currentGap / 2;
            float gapBottom = obs.gapY + currentGap / 2;

            if (birdY - BIRD_RADIUS < gapTop || birdY + BIRD_RADIUS > gapBottom) {
                isColliding = true;
                break;
            }
        }
    }

    if (isColliding) {
        score -= 1;
    } else {
        for (auto& obs : obstacles) {
            obs.x -= PIPE_SPEED;
        }

        // ★ 生成新管子：使用「最後一根管子」記錄的 nextSpawnDistance
        float currentSpawnDist = obstacles.back().nextSpawnDistance;

        if (obstacles.back().x < (GAME_WIDTH - currentSpawnDist)) {

            // 1. 隨機 Y 位置
            float minGapY = 700.0f;
            float range = 600.0f;
            float randomGapY = minGapY + (rand() % (int)range);

            // 2. 隨機缺口倍率 (2, 3, 4, 5)
            float birdDiameter = BIRD_RADIUS * 2;
            float multipliers[] = {2.0f, 3.0f, 4.0f, 5.0f};
            int randomIndex = rand() % 4;
            float randomGapH = birdDiameter * multipliers[randomIndex];

            // 3. ★ 隨機決定「這根新管子」與「再下一根」的距離
            // 範圍 600 ~ 1200 (原本固定 900)
            float nextDist = 600.0f + (rand() % 600);

            obstacles.push_back({obstacles.back().x + currentSpawnDist, randomGapY, randomGapH, false, nextDist});
        }

        if (!obstacles.empty() && obstacles[0].x < -PIPE_WIDTH) {
            obstacles.erase(obstacles.begin());
        }

        for (auto& obs : obstacles) {
            if (!obs.passed && obs.x + PIPE_WIDTH < BIRD_X - BIRD_RADIUS) {
                score += 100;
                obs.passed = true;
            }
        }
    }

    return birdY;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_soundinteractionapp_GameEngine_getGameState(JNIEnv* env, jobject) {
    jfloatArray result = env->NewFloatArray(4);
    float temp[4];
    temp[0] = (float)score;
    temp[1] = gameTime;
    temp[2] = isGameOver ? 1.0f : 0.0f;
    temp[3] = isVictory ? 1.0f : 0.0f;
    env->SetFloatArrayRegion(result, 0, 4, temp);
    return result;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_soundinteractionapp_GameEngine_getObstacleData(JNIEnv* env, jobject) {
    int size = obstacles.size() * 3;
    jfloatArray result = env->NewFloatArray(size);
    std::vector<float> tempList;
    for (const auto& obs : obstacles) {
        tempList.push_back(obs.x);
        tempList.push_back(obs.gapY);
        tempList.push_back(obs.gapHeight);
        // nextSpawnDistance 不需要傳給 Kotlin 畫圖，所以不用加
    }
    env->SetFloatArrayRegion(result, 0, size, tempList.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_com_soundinteractionapp_GameEngine_processAudio(JNIEnv* env, jobject, jshortArray, jint) {}
extern "C" JNIEXPORT void JNICALL Java_com_soundinteractionapp_GameEngine_sendPitchData(JNIEnv* env, jobject, jfloat) {}