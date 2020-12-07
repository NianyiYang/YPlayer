package com.yny.yplayer.decoder

import com.yny.yplayer.constant.DecodeInformation
import com.yny.yplayer.listener.IDecoderStateListener

/**
 * 解码器定义
 *
 * @author nianyi.yang
 * create on 2020/11/6 3:24 PM
 */
interface IDecoder : Runnable {

    /**
     * 暂停解码
     */
    fun pause()

    /**
     * 继续解码
     */
    fun resume()

    /**
     * 跳转到指定位置
     * 并返回实际帧的时间
     *
     * @param pos: 毫秒
     * @return 实际时间戳，单位：毫秒
     */
    fun seekTo(pos: Long): Long

    /**
     * 跳转到指定位置,并播放
     * 并返回实际帧的时间
     *
     * @param pos: 毫秒
     * @return 实际时间戳，单位：毫秒
     */
    fun seekAndPlay(pos: Long): Long

    /**
     * 停止解码
     */
    fun stop()

    /**
     * 是否正在解码
     */
    fun isDecoding(): Boolean

    /**
     * 是否正在快进
     */
    fun isSeeking(): Boolean

    /**
     * 是否停止解码
     */
    fun isStop(): Boolean

    /**
     * 设置尺寸监听器
     */
//    fun setSizeListener(l: IDecoderProgress)

    /**
     * 设置状态监听器
     */
    fun setStateListener(l: IDecoderStateListener?)

    /**
     * 获取视频信息
     */
    fun getDecodeInformation(): DecodeInformation

    /**
     * 无需音视频同步
     */
    fun withoutSync(): IDecoder
}