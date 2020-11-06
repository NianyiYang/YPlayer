package com.yny.yplayer

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * 视频一帧的数据解雇
 *
 * @author nianyi.yang
 * create on 2020/11/6 3:49 PM
 */
class Frame {
    var buffer: ByteBuffer? = null

    var bufferInfo = MediaCodec.BufferInfo()
        private set

    fun setBufferInfo(info: MediaCodec.BufferInfo) {
        bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags)
    }
}