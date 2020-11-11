package com.yny.yplayer.extractor

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * 视频数据提取器
 *
 * @author nianyi.yang
 * create on 2020/11/6 5:29 PM
 */
class VideoExtractor(path: String): IExtractor {

    private val mediaExtractor = MMExtractor(path)

    override fun getFormat(): MediaFormat? {
        return mediaExtractor.getVideoFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimestamp(): Long {
        return mediaExtractor.getCurrentTimestamp()
    }

    override fun getSampleFlag(): Int {
        return mediaExtractor.getSampleFlag()
    }

    override fun seek(pos: Long): Long {
        return mediaExtractor.seek(pos)
    }

    override fun setStartPos(pos: Long) {
        return mediaExtractor.setStartPos(pos)
    }

    override fun stop() {
        mediaExtractor.stop()
    }
}