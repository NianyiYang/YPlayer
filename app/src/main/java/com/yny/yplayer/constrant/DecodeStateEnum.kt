package com.yny.yplayer.constrant

/**
 * 解码状态
 *
 * @author nianyi.yang
 * create on 2020/11/6 3:30 PM
 */
enum class DecodeStateEnum {
    /**
     * 开始状态
     */
    START,

    /**
     * 解码中
     * */
    DECODING,

    /**
     * 解码暂停
     * */
    PAUSE,

    /**
     * 正在快进
     * */
    SEEKING,

    /**
     * 解码完成
     * */
    FINISH,

    /**
     * 解码器释放
     * */
    RELEASE
}