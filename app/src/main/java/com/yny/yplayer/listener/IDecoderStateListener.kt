package com.yny.yplayer.listener

import com.yny.yplayer.Frame
import com.yny.yplayer.decoder.BaseDecoder

/**
 * 解码状态回调
 *
 * @author nianyi.yang
 * create on 2020/11/6 3:42 PM
 */
interface IDecoderStateListener {
    fun decoderPrepare(decodeJob: BaseDecoder?)
    fun decoderReady(decodeJob: BaseDecoder?)
    fun decoderRunning(decodeJob: BaseDecoder?)
    fun decoderPause(decodeJob: BaseDecoder?)
    fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame)
    fun decoderFinish(decodeJob: BaseDecoder?)
    fun decoderDestroy(decodeJob: BaseDecoder?)
    fun decoderError(decodeJob: BaseDecoder?, msg: String)
}