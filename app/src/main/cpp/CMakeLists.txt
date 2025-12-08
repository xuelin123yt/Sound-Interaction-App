cmake_minimum_required(VERSION 3.22.1)

# 專案名稱
project("voicegame")

# 建立共享函式庫 (Shared Library)
add_library(
        voicegame
        SHARED
        native-lib.cpp)

# 尋找 Android 的 Log 庫
find_library(
        log-lib
        log)

# 連結函式庫
target_link_libraries(
        voicegame
        ${log-lib})