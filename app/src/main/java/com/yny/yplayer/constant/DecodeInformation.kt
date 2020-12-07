package com.yny.yplayer.constant

import android.media.MediaFormat

/**
 * Media 解析出来的信息
 *
 * @author nianyi.yang
 * create on 2020/11/10 6:31 PM
 */
class DecodeInformation {

    /**
     * 获取视频宽
     */
    var width:Int = 0

    /**
     * 获取视频高
     */
    var height:Int = 0

    /**
     * 获取视频长度
     */
    var duration:Long = 0

    /**
     * 当前帧时间，单位：ms
     */
    var curTimestamp:Long = 0L

    /**
     * 获取视频旋转角度
     */
    var rotationAngle:Int = 0

    /**
     * 获取音视频对应的格式参数
     */
    var mediaFormat:MediaFormat? = null

    /**
     * 获取音视频对应的媒体轨道
     */
    var mediaTrack:Int = 0

    /**
     * 获取解码的文件路径
     */
    var filePath:String = ""
}