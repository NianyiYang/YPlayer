package com.yny.yplayer.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.yny.yplayer.Frame
import com.yny.yplayer.constant.DecodeInformation
import com.yny.yplayer.constant.DecodeStateEnum
import com.yny.yplayer.extractor.IExtractor
import com.yny.yplayer.listener.IDecoderStateListener
import java.io.File
import java.nio.ByteBuffer

/**
 * 解码器基类
 *
 * @author nianyi.yang
 * create on 2020/11/6 3:47 PM
 */
abstract class BaseDecoder(private val filePath: String) : IDecoder {

    companion object {
        const val TAG = "BaseDecoder"
    }

    //-------------线程相关------------------------
    /**
     * 解码器是否在运行
     */
    private var isRunning = true

    /**
     * 线程等待锁
     */
    private val lock = Object()

    /**
     * 是否可以进入解码
     */
    private var readyForDecode = false

    //---------------状态相关-----------------------
    /**
     * 音视频解码器
     */
    private var mediaCodec: MediaCodec? = null

    /**
     * 音视频数据读取器
     */
    private var extractor: IExtractor? = null

    /**
     * 解码输入缓存区
     */
    private var inputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码输出缓存区
     */
    private var outputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码数据信息
     */
    private var bufferInfo = MediaCodec.BufferInfo()

    private var state = DecodeStateEnum.RELEASE

    protected var decodeStateListener: IDecoderStateListener? = null

    /**
     * 流数据是否结束
     */
    private var isEOS = false

    protected var videoWidth = 0

    protected var videoHeight = 0

    private var duration: Long = 0

    private var startPos: Long = 0

    private var endPos: Long = 0

    /**
     * 开始解码时间，用于音视频同步
     */
    private var startTimeForSync = -1L

    // 是否需要音视频渲染同步
    private var syncRender = true

    final override fun run() {
        if (state == DecodeStateEnum.RELEASE) {
            state = DecodeStateEnum.START
        }
        decodeStateListener?.decoderPrepare(this)

        //【解码步骤：1. 初始化，并启动解码器】
        if (!init()) return

        Log.i(TAG, "开始解码")
        try {
            while (isRunning) {
                if (state != DecodeStateEnum.START &&
                    state != DecodeStateEnum.DECODING &&
                    state != DecodeStateEnum.SEEKING
                ) {
                    Log.i(TAG, "进入等待：$state")

                    waitDecode()

                    // ---------【同步时间矫正】-------------
                    //恢复同步的起始时间，即去除等待流失的时间
                    startTimeForSync =
                        System.currentTimeMillis() - getDecodeInformation().curTimestamp
                }

                if (!isRunning ||
                    state == DecodeStateEnum.RELEASE
                ) {
                    isRunning = false
                    break
                }

                if (startTimeForSync == -1L) {
                    startTimeForSync = System.currentTimeMillis()
                }

                // 如果数据没有解码完毕，将数据推入解码器解码
                if (!isEOS) {
                    //【解码步骤：2. 将数据压入解码器输入缓冲】
                    isEOS = pushBufferToDecoder()
                }

                //【解码步骤：3. 将解码好的数据从缓冲区拉取出来】
                val index = pullBufferFromDecoder()
                if (index >= 0) {
                    // ---------【音视频同步】-------------
                    if (syncRender && state == DecodeStateEnum.DECODING) {
                        sleepRender()
                    }
                    //【解码步骤：4. 渲染】
                    if (syncRender) {// 如果只是用于编码合成新视频，无需渲染
                        render(outputBuffers!![index], bufferInfo)
                    }

                    //将解码数据传递出去
                    val frame = Frame()
                    frame.buffer = outputBuffers!![index]
                    frame.setBufferInfo(bufferInfo)
                    decodeStateListener?.decodeOneFrame(this, frame)

                    //【解码步骤：5. 释放输出缓冲】
                    mediaCodec!!.releaseOutputBuffer(index, true)

                    if (state == DecodeStateEnum.START) {
                        state = DecodeStateEnum.PAUSE
                    }
                }
                //【解码步骤：6. 判断解码是否完成】
                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "解码结束")
                    state = DecodeStateEnum.FINISH
                    decodeStateListener?.decoderFinish(this)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            doneDecode()
            release()
        }
    }

    private fun init(): Boolean {
        if (filePath.isEmpty() || !File(filePath).exists()) {
            Log.w(TAG, "文件路径为空")
            decodeStateListener?.decoderError(this, "文件路径为空")
            return false
        }

        if (!check()) return false

        //初始化数据提取器
        extractor = initExtractor(filePath)
        if (extractor == null ||
            extractor!!.getFormat() == null
        ) {
            Log.w(TAG, "无法解析文件")
            return false
        }

        //初始化参数
        if (!initParams()) return false

        //初始化渲染器
        if (!initRender()) return false

        //初始化解码器
        if (!initCodec()) return false
        return true
    }

    private fun initParams(): Boolean {
        try {
            val format = extractor!!.getFormat()!!
            duration = format.getLong(MediaFormat.KEY_DURATION) / 1000
            if (endPos == 0L) endPos = duration

            initSpecParams(extractor!!.getFormat()!!)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun initCodec(): Boolean {
        try {
            val type = extractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME) ?: ""
            mediaCodec = MediaCodec.createDecoderByType(type)
            if (!configCodec(mediaCodec!!, extractor!!.getFormat()!!)) {
                waitDecode()
            }
            mediaCodec!!.start()

            inputBuffers = mediaCodec?.inputBuffers
            outputBuffers = mediaCodec?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun pushBufferToDecoder(): Boolean {
        val inputBufferIndex = mediaCodec!!.dequeueInputBuffer(1000)
        var isEndOfStream = false

        if (inputBufferIndex >= 0) {
            val inputBuffer = inputBuffers!![inputBufferIndex]
            val sampleSize = extractor!!.readBuffer(inputBuffer)

            if (sampleSize < 0) {
                //如果数据已经取完，压入数据结束标志：MediaCodec.BUFFER_FLAG_END_OF_STREAM
                mediaCodec!!.queueInputBuffer(
                    inputBufferIndex, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            } else {
                mediaCodec!!.queueInputBuffer(
                    inputBufferIndex, 0,
                    sampleSize, extractor!!.getCurrentTimestamp(), 0
                )
            }
        }
        return isEndOfStream
    }

    private fun pullBufferFromDecoder(): Int {
        // 查询是否有解码完成的数据，index >=0 时，表示数据有效，并且index为缓冲区索引
        var index = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 1000)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
            }
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
            }
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                outputBuffers = mediaCodec!!.outputBuffers
            }
            else -> {
                return index
            }
        }
        return -1
    }

    private fun sleepRender() {
        val passTime = System.currentTimeMillis() - startTimeForSync
        val curTime = getDecodeInformation().curTimestamp
        if (curTime > passTime) {
            Thread.sleep(curTime - passTime)
        }
    }

    private fun release() {
        try {
            Log.i(TAG, "解码停止，释放解码器")
            state = DecodeStateEnum.RELEASE
            isEOS = false
            extractor?.stop()
            mediaCodec?.stop()
            mediaCodec?.release()
            decodeStateListener?.decoderDestroy(this)
        } catch (e: Exception) {
        }
    }

    /**
     * 解码线程进入等待
     */
    private fun waitDecode() {
        try {
            if (state == DecodeStateEnum.PAUSE) {
                decodeStateListener?.decoderPause(this)
            }
            synchronized(lock) {
                lock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecode() {
        synchronized(lock) {
            lock.notifyAll()
        }
        if (state == DecodeStateEnum.DECODING) {
            decodeStateListener?.decoderRunning(this)
        }
    }

    override fun pause() {
        state = DecodeStateEnum.DECODING
    }

    override fun resume() {
        state = DecodeStateEnum.DECODING
        notifyDecode()
    }

    override fun seekTo(pos: Long): Long {
        return 0
    }

    override fun seekAndPlay(pos: Long): Long {
        return 0
    }

    override fun stop() {
        state = DecodeStateEnum.RELEASE
        isRunning = false
        notifyDecode()
    }

    override fun isDecoding(): Boolean {
        return state == DecodeStateEnum.DECODING
    }

    override fun isSeeking(): Boolean {
        return state == DecodeStateEnum.SEEKING
    }

    override fun isStop(): Boolean {
        return state == DecodeStateEnum.RELEASE
    }

//    override fun setSizeListener(l: IDecoderProgress) {
//    }

    override fun setStateListener(l: IDecoderStateListener?) {
        decodeStateListener = l
    }

    override fun getDecodeInformation(): DecodeInformation {
        return DecodeInformation().apply {
            width = videoWidth
            height = videoHeight
            duration = this@BaseDecoder.duration
            curTimestamp = bufferInfo.presentationTimeUs / 1000
            rotationAngle = 0
            mediaFormat = extractor?.getFormat()
            mediaTrack = 0
            filePath = this@BaseDecoder.filePath
        }
    }

    override fun withoutSync(): IDecoder {
        syncRender = false
        return this
    }

    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(path: String): IExtractor

    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 渲染
     */
    abstract fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )

    /**
     * 结束解码
     */
    abstract fun doneDecode()
}