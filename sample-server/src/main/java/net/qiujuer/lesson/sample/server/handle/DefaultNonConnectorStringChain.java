package net.qiujuer.lesson.sample.server.handle;

import net.qiujuer.library.clink.box.StringReceivePacket;

public class DefaultNonConnectorStringChain extends ConnectorStringPacketChain {
    @Override
    protected boolean consume(ClientHandler handler, StringReceivePacket stringReceivePacket) {
        return false;
    }
}
