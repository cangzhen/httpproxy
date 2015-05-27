package proxy.mock.workstation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;

class DelayObject {
	public DelayObject(ChannelHandlerContext ctx, FullHttpResponse response,
			long sendTime) {
		this.ctx =ctx;
		this.response = response;
		this.sendTime = sendTime;
	}
	ChannelHandlerContext ctx;
	long sendTime;
	FullHttpResponse response ;
}
