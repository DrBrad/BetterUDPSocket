package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSocket {

    private UDPServerSocket server;
    private UDPInputStream in;
    private UDPOutputStream out;
    private InetAddress address;
    private int port;
    private int key;

    public UDPSocket(UDPServerSocket server, int key, InetAddress address, int port)throws IOException {
        this.server = server;
        this.key = key;
        this.address = address;
        this.port = port;

        in = new UDPInputStream();
        out = new UDPOutputStream(this);
    }

    public UDPSocket(UDPServerSocket server, DatagramPacket packet)throws IOException {
        this.server = server;
        address = packet.getAddress();
        port = packet.getPort();
        key = (((packet.getData()[packet.getOffset()] & 0xff) << 24) |
                ((packet.getData()[packet.getOffset()+1] & 0xff) << 16) |
                ((packet.getData()[packet.getOffset()+2] & 0xff) << 8) |
                (packet.getData()[packet.getOffset()+3] & 0xff));

        in = new UDPInputStream();
        in.append(packet.getData(), packet.getOffset()+4, packet.getLength()-4);
        out = new UDPOutputStream(this);
    }

    public UDPInputStream getInputStream(){
        return in;
    }

    public UDPOutputStream getOutputStream(){
        return out;
    }

    public DatagramSocket getServer(){
        return server.getServer();
    }

    public int getKey(){
        return key;
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }

    public void close(){
        server.close(key+":"+address.getHostAddress()+":"+port);
    }
}
