package com.yny.yplayer.extractor

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * 提取器接口
 *
 * @author nianyi.yang
 * create on 2020/11/6 3:35 PM
 */
interface IExtractor {

    /**
     * 获取音视频格式参数
     */
    fun getFormat(): MediaFormat?

    /**
     * 读取音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimestamp(): Long

    fun getSampleFlag(): Int

    /**
     * Seek到指定位置，并返回实际帧的时间戳
     */
    fun seek(pos: Long): Long

    /**
     * 设置开始位置
     */
    fun setStartPos(pos: Long)

    /**
     * 停止读取数据
     */
    fun stop()
}