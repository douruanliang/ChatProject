package net.qiujuer.lesson.sample.server;


import net.qiujuer.lesson.sample.server.handle.ClientHandler;
import net.qiujuer.lesson.sample.server.handle.ConnectorStringPacketChain;
import net.qiujuer.library.clink.box.StringReceivePacket;

/**
 * 数据统计类
 */
public class ServerStatistics {
    long recerveSize;
    long sendSize;

    ConnectorStringPacketChain statisticsChain() {
        return new StatisticConnectorPacketChain();
    }

    class StatisticConnectorPacketChain extends ConnectorStringPacketChain {
        @Override
        protected boolean consume(ClientHandler handler, StringReceivePacket stringReceivePacket) {
            recerveSize++;
            return false;
        }
    }
}
