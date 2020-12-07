package com.yny.yplayer.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.nio.ByteBuffer

/**
 * 封装器
 *
 * @author nianyi.yang
 * create on 12/7/20 5:04 PM
 */
class MMuxer {
    private val TAG = MMuxer::class.java.simpleName

    private var path: String

    private var mediaMuxer: MediaMuxer? = null

    private var videoTrackIndex = -1
    private var audioTrackIndex = -1

    private var isAudioTrackAdd = false
    private var isVideoTrackAdd = false

    private var isAudioEnd = false
    private var isVideoEnd = false

    private var isStart = false

    private var stateListener: IMuxerStateListener? = null

    init {
        // 指定储存位置，名称，格式
        val fileName =
            "YPlayer_Test" + /*SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) +*/ ".mp4"
        val filePath = Environment.getExternalStorageDirectory().absolutePath.toString() + "/"
        path = filePath + fileName
        mediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun addVideoTrack(mediaFormat: MediaFormat) {
        if (isVideoTrackAdd) {
            return
        }

        if (mediaMuxer != null) {
            videoTrackIndex = try {
                mediaMuxer!!.addTrack(mediaFormat)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

            Log.i(TAG, "添加视频轨道")
            isVideoTrackAdd = true
            startMuxer()
        }
    }

    fun addAudioTrack(mediaFormat: MediaFormat) {
        if (isAudioTrackAdd){
            return
        }

        if (mediaMuxer != null) {
            audioTrackIndex = try {
                mediaMuxer!!.addTrack(mediaFormat)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            Log.i(TAG, "添加音频轨道")
            isAudioTrackAdd = true
            startMuxer()
        }
    }

    fun setNoAudio() {
        if (isAudioTrackAdd) return
        isAudioTrackAdd = true
        isAudioEnd = true
        startMuxer()
    }

    fun setNoVideo() {
        if (isVideoTrackAdd) return
        isVideoTrackAdd = true
        isVideoEnd = true
        startMuxer()
    }

    fun writeVideoData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (isStart) {
            mediaMuxer?.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)
        }
    }

    fun writeAudioData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (isStart) {
            mediaMuxer?.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo)
        }
    }

    private fun startMuxer() {
        if (isAudioTrackAdd && isVideoTrackAdd) {
            mediaMuxer?.start()
            isStart = true
            stateListener?.onMuxerStart()
            Log.i(TAG, "启动封装器")
        }
    }

    fun releaseVideoTrack() {
        isVideoEnd = true
        release()
    }

    fun releaseAudioTrack() {
        isAudioEnd = true
        release()
    }

    /**
     * 这一步非常重要，必须要释放之后，才能生成可用的完整的MP4文件
     */
    private fun release() {
        if (isAudioEnd && isVideoEnd) {
            isAudioTrackAdd = false
            isVideoTrackAdd = false
            try {
                mediaMuxer?.stop()
                mediaMuxer?.release()
                mediaMuxer = null
                Log.i(TAG, "退出封装器")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stateListener?.onMuxerFinish()
            }
        }
    }

    fun setStateListener(l: IMuxerStateListener) {
        this.stateListener = l
    }

    interface IMuxerStateListener {
        fun onMuxerStart() {}
        fun onMuxerFinish() {}
    }
}