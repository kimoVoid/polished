package io.github.kimovoid.polished.server.feature.connection;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public interface ExtendedConnectionListener {
    void close(Socket socket);
    HashMap<InetAddress, Long> getAddresses();
}
