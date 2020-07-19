package net.qiujuer.lesson.sample.server;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class ServerAcceptor extends Thread {
    private boolean done = false;
    private final AcceptorListener listener;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Selector selector;

    public ServerAcceptor(AcceptorListener listener) throws IOException {
        super("Server-Acceptor-Thread");
        this.listener = listener;
        this.selector = Selector.open();
    }

    boolean awaitTRunning() {
        try {
            latch.await();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        super.run();
        Selector selector = this.selector;
        System.out.println("服务器准备就绪～");
        // 等待客户端连接
        do {
            // 得到客户端
            try {
                if (selector.select() == 0) {
                    if (done) {
                        break;
                    }
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    if (done) {
                        break;
                    }

                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // 检查当前Key的状态是否是我们关注的
                    // 客户端到达状态
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        // 非阻塞状态拿到客户端连接
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        listener.onNewSocketArrived(socketChannel);

                     /*   try {
                            // 客户端构建异步线程
                            ClientHandler clientHandler = new ClientHandler(socketChannel,
                                    TCPServer.this, cachePath);
                            // 添加同步处理
                            synchronized (TCPServer.this) {
                                clientHandlerList.add(clientHandler);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("服务器异常：" + e.getMessage());
                        }*/
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } while (!done);

        System.out.println("Server-Acceptor finished！");
    }


    void exit() {
        done = true;
        // 唤醒当前的阻塞
        selector.wakeup();
    }

    interface AcceptorListener {
        void onNewSocketArrived(SocketChannel channel);
    }
}
