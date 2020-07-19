package net.qiujuer.lesson.sample.server;

import net.qiujuer.lesson.sample.server.handle.ClientHandler;
import net.qiujuer.lesson.sample.server.handle.ConnectorStringPacketChain;
import net.qiujuer.library.clink.box.StringReceivePacket;

import java.util.ArrayList;
import java.util.List;


/**
 * 群信息
 */
public class Group {

    private final String name;
    private final List<ClientHandler> members = new ArrayList<>();
    private GroupMessageAdapter mGroupMessageAdapter;

    public Group(String name, GroupMessageAdapter mGroupMessageAdapter) {
        this.name = name;
        this.mGroupMessageAdapter = mGroupMessageAdapter;
    }

    public String getName() {
        return name;
    }

    boolean addMember(ClientHandler clientHandler) {

        synchronized (members) {
            if (!members.contains(clientHandler)) {
                members.add(clientHandler);
                clientHandler.getStringPacketChain()
                        .appendLast(new ForwardConnectorStringPacketChain());
                System.out.println("Group{" + name + "} add new member");
                return true;
            }
        }
        return false;
    }


    boolean removeMember(ClientHandler clientHandler) {

        synchronized (members) {
            if (members.contains(clientHandler)) {
                clientHandler.getStringPacketChain()
                        .remove(ForwardConnectorStringPacketChain.class);
                System.out.println("Group{" + name + "} remove new member");
                return true;
            }
        }
        return false;
    }


    private class ForwardConnectorStringPacketChain extends ConnectorStringPacketChain {
        @Override
        protected boolean consume(ClientHandler handler, StringReceivePacket stringReceivePacket) {

            synchronized (members) {
                for (ClientHandler member : members) {
                    if (member.equals(handler)) {
                        continue;
                    }
                    mGroupMessageAdapter.sendMessageToClient(handler, stringReceivePacket.entity());
                }

            }
            return true;
        }
    }

    interface GroupMessageAdapter {
        void sendMessageToClient(ClientHandler clientHandler, String msg);
    }
}
