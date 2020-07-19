package net.qiujuer.lesson.sample.server.handle;

public abstract class ConnectorHandlerChain<Model> {

    private volatile ConnectorHandlerChain<Model> next;

    public ConnectorHandlerChain<Model> appendLast(ConnectorHandlerChain<Model> newChain) {

        if (newChain == this || this.getClass().equals(newChain.getClass())) {
            return this;
        }

        synchronized (this) {
            if (next == null) {
                next = newChain;
                return newChain;
            }
            return next.appendLast(newChain);
        }
    }

    /**
     *  1 移除节点中的某一个节点及其之后的节点
     *  2 移除某个节点，如果其具有后续的节点，则把后续节点接到当前节点上；实现移除中间某个节点
     * @param clx  待移除节点的class 信息
     * @return
     */
    public synchronized boolean remove(Class<? extends ConnectorHandlerChain<Model>> clx) {

        if (this.getClass().equals(clx)) {
            return false;
        }

        synchronized (this) {
            if (next == null) {
                return false;
            } else if (next.getClass().equals(clx)) {
                this.next = next.next;
                return true;
            } else {
                return next.remove(clx);
            }

        }
    }

    synchronized boolean handle(ClientHandler handler, Model model) {
        ConnectorHandlerChain<Model> next = this.next;

        if (consume(handler, model)) {
            return true;
        }
        boolean consumed = next != null && next.handle(handler, model);

        if (consumed) {
            return true;
        }
        return consumeAgain(handler, model);
    }

    protected abstract boolean consume(ClientHandler handler, Model model);

    /**
     * 扩充
     *
     * @param handler
     * @param model
     * @return
     */
    protected boolean consumeAgain(ClientHandler handler, Model model) {
        return false;
    }
}
