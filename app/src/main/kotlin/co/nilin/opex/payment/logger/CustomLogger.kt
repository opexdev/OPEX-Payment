package co.nilin.opex.payment.logger

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufHolder
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.logging.LoggingHandler
import io.netty.util.internal.PlatformDependent
import java.nio.charset.Charset

class CustomLogger (clazz: Class<*>?) : LoggingHandler(clazz) {

    override fun format(ctx: ChannelHandlerContext?, event: String?, arg: Any?): String {
        return when (arg) {
            is ByteBuf -> {
                val msg = arg
                decode(msg, msg.readerIndex(), msg.readableBytes(), Charset.defaultCharset())
            }
            is ByteBufHolder -> {
                val msg = arg.content()
                decode(msg, msg.readerIndex(), msg.readableBytes(), Charset.defaultCharset())
            }
            else -> {
                super.format(ctx, event, arg)
            }
        }
    }

    private fun decode(src: ByteBuf, readerIndex: Int, len: Int, charset: Charset): String {
        if (len != 0) {
            val array: ByteArray
            val offset: Int
            if (src.hasArray()) {
                array = src.array()
                offset = src.arrayOffset() + readerIndex
            } else {
                array = PlatformDependent.allocateUninitializedArray(Math.max(len, 1024))
                offset = 0
                src.getBytes(readerIndex, array, 0, len)
            }
            return String(array, offset, len, charset)
        }
        return ""
    }

}