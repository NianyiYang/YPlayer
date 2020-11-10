package com.yny.yplayer.decoder

import android.media.*
import com.yny.yplayer.extractor.AudioExtractor
import com.yny.yplayer.extractor.IExtractor
import java.nio.ByteBuffer

/**
 * 音频解码器
 *
 * @author nianyi.yang
 * create on 2020/11/6 5:31 PM
 */
class AudioDecoder(path: String) : BaseDecoder(path) {
    /**采样率*/
    private var sampleRate = -1

    /**声音通道数量*/
    private var channels = 1

    /**PCM采样位数*/
    private var PCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT

    /**音频播放器*/
    private var audioTrack: AudioTrack? = null

    /**音频数据缓存*/
    private var audioOutTempBuf: ShortArray? = null

    override fun check(): Boolean {
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return AudioExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
        try {
            channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

            PCMEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                format.getInteger(MediaFormat.KEY_PCM_ENCODING)
            } else {
                //如果没有这个参数，默认为16位采样
                AudioFormat.ENCODING_PCM_16BIT
            }
        } catch (e: Exception) {
        }
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, null, null, 0)
        return true
    }

    override fun initRender(): Boolean {
        val channel = if (channels == 1) {
            //单声道
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            //双声道
            AudioFormat.CHANNEL_OUT_STEREO
        }

        //获取最小缓冲区
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, PCMEncodeBit)

        audioOutTempBuf = ShortArray(minBufferSize / 2)

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,//播放类型：音乐
            sampleRate, //采样率
            channel, //通道
            PCMEncodeBit, //采样位数
            minBufferSize, //缓冲区大小
            AudioTrack.MODE_STREAM
        ) //播放模式：数据流动态写入，另一种是一次性写入

        audioTrack!!.play()
        return true
    }

    override fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        if (audioOutTempBuf!!.size < bufferInfo.size / 2) {
            audioOutTempBuf = ShortArray(bufferInfo.size / 2)
        }
        outputBuffer.position(0)
        outputBuffer.asShortBuffer().get(audioOutTempBuf, 0, bufferInfo.size / 2)
        audioTrack!!.write(audioOutTempBuf!!, 0, bufferInfo.size / 2)
    }

    override fun doneDecode() {
        audioTrack?.stop()
        audioTrack?.release()
    }
}