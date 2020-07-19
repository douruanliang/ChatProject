package net.qiujuer.lesson.sample.server.handle;


import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.library.clink.box.StringReceivePacket;
import net.qiujuer.library.clink.core.Connector;
import net.qiujuer.library.clink.core.Packet;
import net.qiujuer.library.clink.core.ReceivePacket;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

public class ClientHandler extends Connector {
    private final File cachePath;
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;
    private final Executor mDeliverPool;
    private final ConnectorCloseChain closeChain = new DefaultPrintConnectorCloseChain();
    private final ConnectorStringPacketChain stringPacketChain = new DefaultNonConnectorStringChain();

    public ClientHandler(SocketChannel socketChannel,
                         ClientHandlerCallback clientHandlerCallback,
                         File cachePath, Executor deliverPool) throws IOException {
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        this.cachePath = cachePath;
        this.mDeliverPool = deliverPool;
        setup(socketChannel);
    }

    public void exit() {
        CloseUtils.close(this);
        closeChain.handle(this, this);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        closeChain.handle(this, this);
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    protected void onReceivedPacket(ReceivePacket packet) {
        super.onReceivedPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String string = (String) packet.entity();
            System.out.println(key.toString() + ":" + string);
            clientHandlerCallback.onNewMessageArrived(this, string);
        }

        switch (packet.type()) {
            case Packet.TYPE_MEMORY_STRING:
                deliveryStringPacket((StringReceivePacket) packet);
                break;
            default:
                System.out.println("New Packet" + packet.type() + "-" + packet.length());
        }
    }

    private void deliveryStringPacket(StringReceivePacket packet) {

        mDeliverPool.execute(() -> {
            stringPacketChain.handle(this, packet);
        });

    }

    public ConnectorCloseChain getCloseChain() {
        return closeChain;
    }

    public ConnectorStringPacketChain getStringPacketChain() {
        return stringPacketChain;
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler);

        // 收到消息时通知
        void onNewMessageArrived(ClientHandler handler, String msg);
    }
}
