package com.yny.yplayer.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.yny.yplayer.extractor.IExtractor
import com.yny.yplayer.extractor.VideoExtractor
import java.nio.ByteBuffer

/**
 * 视频解码器
 *
 * @author nianyi.yang
 * create on 2020/11/6 5:28 PM
 */
class VideoDecoder(path: String, sfv: SurfaceView?, surface: Surface?): BaseDecoder(path) {
    private val TAG = "VideoDecoder"

    private val surfaceView = sfv
    private var surface = surface

    override fun check(): Boolean {
        if (surfaceView == null && surface == null) {
            Log.w(TAG, "SurfaceView和Surface都为空，至少需要一个不为空")
            decodeStateListener?.decoderError(this, "显示器为空")
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (surface != null) {
            codec.configure(format, surface , null, 0)
            notifyDecode()
        } else if (surfaceView?.holder?.surface != null) {
            surface = surfaceView.holder?.surface
            configCodec(codec, format)
        } else {
            surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    surface = holder.surface
                    configCodec(codec, format)
                }
            })

            return false
        }
        return true
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun render(outputBuffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo) {
    }

    override fun doneDecode() {
    }
}