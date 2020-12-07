package com.yny.yplayer.muxer

import android.media.MediaCodec
import android.util.Log
import com.yny.yplayer.extractor.AudioExtractor
import com.yny.yplayer.extractor.VideoExtractor
import java.nio.ByteBuffer

/**
 * MP4 打包器
 *
 * @author nianyi.yang
 * create on 12/7/20 5:11 PM
 */
class MP4Repack(path: String) {

    private val TAG = MP4Repack::class.java.simpleName

    private val audioExtractor: AudioExtractor = AudioExtractor(path)
    private val videoExtractor: VideoExtractor = VideoExtractor(path)
    private val muxer: MMuxer = MMuxer()

    fun start() {
        val audioFormat = audioExtractor.getFormat()
        val videoFormat = videoExtractor.getFormat()

        if (audioFormat != null) {
            muxer.addAudioTrack(audioFormat)
        } else {
            muxer.setNoAudio()
        }
        if (videoFormat != null) {
            muxer.addVideoTrack(videoFormat)
        } else {
            muxer.setNoVideo()
        }

        // 开启异步线程操作
        Thread {
            // #stackoverflow https://stackoverflow.com/questions/33148629/android-mediaextractor-readsampledata-illegalargumentexception
//            val buffer = ByteBuffer.allocate(500 * 1024)
            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            if (audioFormat != null) {
                var size = audioExtractor.readBuffer(buffer)
                while (size > 0) {
                    bufferInfo.set(0, size, audioExtractor.getCurrentTimestamp(), audioExtractor.getSampleFlag())
                    muxer.writeAudioData(buffer, bufferInfo)
                    size = audioExtractor.readBuffer(buffer)
                }
            }
            if (videoFormat != null) {
                var size = videoExtractor.readBuffer(buffer)
                while (size > 0) {
                    bufferInfo.set(0, size, videoExtractor.getCurrentTimestamp(), videoExtractor.getSampleFlag())
                    muxer.writeVideoData(buffer, bufferInfo)
                    size = videoExtractor.readBuffer(buffer)
                }
            }
            audioExtractor.stop()
            videoExtractor.stop()
            muxer.releaseAudioTrack()
            muxer.releaseVideoTrack()
            Log.i(TAG, "MP4 重打包完成")
        }.start()
    }
}