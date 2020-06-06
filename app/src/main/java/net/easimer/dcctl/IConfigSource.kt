package net.easimer.dcctl

interface IConfigSource {
    val isReady : Boolean

    val delay : Float
    val interval: Float
    val count: Int
}