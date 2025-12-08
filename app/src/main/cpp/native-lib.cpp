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
const float BIRD_RADIUS = 80.0f; // 半徑 80

// --- 障礙物設定 ---
const float PIPE_SPEED = 18.0f;
const float COLLISION_PIPE_WIDTH = 300.0f;
const float GAME_WIDTH = 4000.0f;

// --- 變數 ---
float birdY = 1000.0f;
float birdVelocity = 0.0f;
int score = 0;
float gameTime = 180.0f;
bool isGameOver = false;
bool isVictory = false;

// 音量門檻
const float VOLUME_THRESHOLD = 200.0f;
int flapCooldown = 0;

struct Obstacle {
    float x;
    float gapY;
    float gapHeight;
    bool passed;
    float nextSpawnDistance;
    bool hasCollided;
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
    flapCooldown = 0;

    obstacles.clear();

    float firstGapY = 700.0f + (rand() % 600);
    float firstGapH = (BIRD_RADIUS * 2 * 3.0f);
    float firstDist = 1000.0f + (rand() % 800);

    obstacles.push_back({GAME_WIDTH, firstGapY, firstGapH, false, firstDist, false});
}

extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_flap(JNIEnv* env, jobject) {
    birdVelocity = LIFT;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_soundinteractionapp_GameEngine_updateGame(JNIEnv* env, jobject) {
    if (isGameOver || isVictory) return birdY;

    // 1. 倒數
    gameTime -= 0.016f;
    if (gameTime <= 0) {
        isVictory = true;
        gameTime = 0;
    }

    // 2. 物理 (先移動)
    birdVelocity += GRAVITY;
    birdY += birdVelocity;

    if (birdY > FLOOR_Y) birdY = FLOOR_Y;
    if (birdY < 0.0f) { birdY = 0.0f; birdVelocity = 0; }

    if (flapCooldown > 0) flapCooldown--;

    bool currentlyColliding = false;

    // 3. ★ 碰撞邏輯修正：區分「撞牆」與「撞地板」
    for (auto& obs : obstacles) {
        // 判斷是否重疊
        if (BIRD_X + BIRD_RADIUS > obs.x && BIRD_X - BIRD_RADIUS < obs.x + COLLISION_PIPE_WIDTH) {

            float currentGap = obs.gapHeight;
            float gapTop = obs.gapY - currentGap / 2;     // 上管底部
            float gapBottom = obs.gapY + currentGap / 2;  // 下管頂部

            bool hit = false;
            bool isVerticalHit = false; // 是否為上下垂直碰撞 (已經進入管子X範圍)

            // 判斷鳥的中心是否已經超過管子的左邊界
            // 如果 BIRD_X >= obs.x，代表鳥已經「進入」管子區域，這時候撞到算是「踩到」或「頂到」
            // 如果 BIRD_X < obs.x，代表鳥還在管子左邊，這時候撞到算是「撞牆」
            if (BIRD_X >= obs.x) {
                isVerticalHit = true;
            }

            // A. 撞到上管
            if (birdY - BIRD_RADIUS < gapTop) {
                hit = true;
                if (isVerticalHit) {
                    // 只有已經進入管子區域，才執行位置校正 (壓回邊緣)
                    birdY = gapTop + BIRD_RADIUS;
                    if (birdVelocity < 0) birdVelocity = 0;
                }
            }

            // B. 撞到下管
            if (birdY + BIRD_RADIUS > gapBottom) {
                hit = true;
                if (isVerticalHit) {
                    // 只有已經進入管子區域，才執行位置校正 (提回邊緣)
                    birdY = gapBottom - BIRD_RADIUS;
                    if (birdVelocity > 0) birdVelocity = 0;
                }
            }

            if (hit) {
                currentlyColliding = true; // 只要撞到 (無論是撞牆還是撞地板)，管子都要停

                // 扣分機制
                if (!obs.hasCollided) {
                    gameTime -= 10.0f;
                    obs.hasCollided = true;
                }
            }
        }
    }

    // 4. 遊戲邏輯分支
    if (currentlyColliding) {
        // ★ 卡住模式：
        // 管子停止移動。
        // 如果是「撞牆 (BIRD_X < obs.x)」，因為上面沒有執行 birdY = ... 的校正，
        // 所以鳥會保持原本的高度，看起來就像是貼在牆壁上，可以自由上下滑動 (Gravity 依然有效)。

    } else {
        // ★ 正常模式：管子移動
        for (auto& obs : obstacles) {
            obs.x -= PIPE_SPEED;
        }

        // 生成新管子
        float currentSpawnDist = obstacles.back().nextSpawnDistance;
        if (obstacles.back().x < (GAME_WIDTH - currentSpawnDist)) {
            float minGapY = 700.0f;
            float range = 600.0f;
            float randomGapY = minGapY + (rand() % (int)range);

            float birdDiameter = BIRD_RADIUS * 2;
            float multipliers[] = {2.0f, 3.0f, 4.0f, 5.0f};
            int randomIndex = rand() % 4;
            float randomGapH = birdDiameter * multipliers[randomIndex];

            float nextDist = 1000.0f + (rand() % 800);

            obstacles.push_back({obstacles.back().x + currentSpawnDist, randomGapY, randomGapH, false, nextDist, false});
        }

        if (!obstacles.empty() && obstacles[0].x < -COLLISION_PIPE_WIDTH) {
            obstacles.erase(obstacles.begin());
        }

        // 加分
        for (auto& obs : obstacles) {
            if (!obs.passed && obs.x + COLLISION_PIPE_WIDTH < BIRD_X - BIRD_RADIUS) {
                score += 20;
                obs.passed = true;
            }
        }
    }

    return birdY;
}

// ... 狀態函數 ...
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
    }
    env->SetFloatArrayRegion(result, 0, size, tempList.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_soundinteractionapp_GameEngine_processAudio(JNIEnv* env, jobject, jshortArray audioData, jint size) {
    jshort* audioPtr = env->GetShortArrayElements(audioData, nullptr);
    long sum = 0;
    for (int i = 0; i < size; i++) {
        sum += audioPtr[i] * audioPtr[i];
    }
    float rms = sqrt(sum / size);

    if (rms > VOLUME_THRESHOLD) {
        if (flapCooldown == 0) {
            birdVelocity = LIFT;
            flapCooldown = 8;
        }
    }
    env->ReleaseShortArrayElements(audioData, audioPtr, 0);
}

extern "C" JNIEXPORT void JNICALL Java_com_soundinteractionapp_GameEngine_sendPitchData(JNIEnv* env, jobject, jfloat) {}
