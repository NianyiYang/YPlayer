package com.yny.yplayer.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Android 原生提取器的封装
 *
 * @author nianyi.yang
 * create on 2020/11/6 5:23 PM
 */
class MMExtractor(path: String) {

    /**
     * 音视频分离器
     */
    private var extractor: MediaExtractor? = null

    /**
     * 音频通道索引
     */
    private var audioTrack = -1

    /**
     * 视频通道索引
     * */
    private var videoTrack = -1

    /**
     * 当前帧时间戳
     * */
    private var curSampleTime: Long = 0

    /**
     * 当前帧标志
     * */
    private var curSampleFlag: Int = 0

    /**
     * 开始解码时间点
     * */
    private var startPos: Long = 0

    init {
        extractor = MediaExtractor()
        extractor?.setDataSource(path)
    }

    /**
     * 获取视频格式参数
     */
    fun getVideoFormat(): MediaFormat? {
        for (i in 0 until extractor!!.trackCount) {
            val mediaFormat = extractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("video/")) {
                videoTrack = i
                break
            }
        }
        return if (videoTrack >= 0)
            extractor!!.getTrackFormat(videoTrack)
        else null
    }

    /**
     * 获取音频格式参数
     */
    fun getAudioFormat(): MediaFormat? {
        for (i in 0 until extractor!!.trackCount) {
            val mediaFormat = extractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("audio/")) {
                audioTrack = i
                break
            }
        }
        return if (audioTrack >= 0) {
            extractor!!.getTrackFormat(audioTrack)
        } else null
    }

    /**
     * 读取视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int {
        byteBuffer.clear()
        selectSourceTrack()
        val readSampleCount = extractor!!.readSampleData(byteBuffer, 0)
        if (readSampleCount < 0) {
            return -1
        }
        //记录当前帧的时间戳
        curSampleTime = extractor!!.sampleTime
        curSampleFlag = extractor!!.sampleFlags
        //进入下一帧
        extractor!!.advance()
        return readSampleCount
    }

    /**
     * 选择通道
     */
    private fun selectSourceTrack() {
        if (videoTrack >= 0) {
            extractor!!.selectTrack(videoTrack)
        } else if (audioTrack >= 0) {
            extractor!!.selectTrack(audioTrack)
        }
    }

    /**
     * Seek到指定位置，并返回实际帧的时间戳
     */
    fun seek(pos: Long): Long {
        extractor!!.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return extractor!!.sampleTime
    }

    /**
     * 停止读取数据
     */
    fun stop() {
        extractor?.release()
        extractor = null
    }

    fun getVideoTrack(): Int {
        return videoTrack
    }

    fun getAudioTrack(): Int {
        return audioTrack
    }

    fun setStartPos(pos: Long) {
        startPos = pos
    }

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimestamp(): Long {
        return curSampleTime
    }

    fun getSampleFlag(): Int {
        return curSampleFlag
    }
}