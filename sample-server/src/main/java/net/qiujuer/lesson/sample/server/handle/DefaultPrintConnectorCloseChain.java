package net.qiujuer.lesson.sample.server.handle;

import net.qiujuer.library.clink.core.Connector;

public class DefaultPrintConnectorCloseChain extends ConnectorCloseChain {
    @Override
    protected boolean consume(ClientHandler handler, Connector connector) {

        System.out.println(handler + ":Exit!!"+ connector.getKey());
        return false;
    }
}
