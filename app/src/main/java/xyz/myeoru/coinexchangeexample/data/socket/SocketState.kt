package xyz.myeoru.coinexchangeexample.data.socket

import okhttp3.Response

sealed class SocketState {
    data class Open(val response: Response) : SocketState()
    data class Closing(val code: Int, val reason: String) : SocketState()
    data class Closed(val code: Int, val reason: String) : SocketState()
    data class Failure(val t: Throwable, val response: Response?) : SocketState()
}