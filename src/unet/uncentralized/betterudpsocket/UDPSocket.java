package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class UDPSocket {

    private UDPServerSocket server;
    private UDPKey key;
    private UDPInputStream in;
    private UDPOutputStream out;
    private boolean safeMode, keepAlive, noDelay;
    private int inOrder = 0, timeout = 5000;
    private DatagramPacket lastPacket;

    public UDPSocket(UDPServerSocket server, UDPKey key, boolean safeMode)throws IOException {
        this.server = server;
        this.key = key;
        this.safeMode = safeMode;

        in = new UDPInputStream(this);
        out = new UDPOutputStream(this);
    }

    public boolean isSafeMode(){
        return safeMode;
    }

    public boolean isKeepAlive(){
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive){
        this.keepAlive = keepAlive;
    }

    public boolean isNoDelay(){
        return noDelay;
    }

    public void setNoDelay(boolean noDelay){
        this.noDelay = noDelay;
    }

    public UDPKey getKey(){
        return key;
    }

    public InetAddress getAddress(){
        return key.getAddress();
    }

    public int getPort(){
        return key.getPort();
    }

    public DatagramSocket getServer(){
        return server.getServer();
    }

    public UDPInputStream getInputStream(){
        return in;
    }

    public UDPOutputStream getOutputStream(){
        return out;
    }

    public boolean isInputStreamShutdown(){
        return out.isClosed();
    }

    public boolean isOutputStreamShutdown(){
        return in.isClosed();
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    public int getTimeout(){
        return timeout;
    }

    public void receive(byte[] buf)throws IOException {
        receive(buf, 0, buf.length);
    }

    public void receive(byte[] buf, int off, int len)throws IOException {
        if(!in.isClosed()){
            byte ack = buf[off];

            switch(ack){
                case 0x00: //PACKET TO BE RECEIVED
                    if(safeMode){
                        int pos = (((buf[off+1] & 0xff) << 24) |
                                ((buf[off+2] & 0xff) << 16) |
                                ((buf[off+3] & 0xff) << 8) |
                                (buf[off+4] & 0xff));

                        if(inOrder == pos){
                            //while(!in.isAckReady()){ }
                            in.append(buf, off+5, len-5);
                            inOrder++;
                            sendSuccessAcknowledgment();

                        }else{
                            sendFailureAcknowledgment();
                        }
                    }else{
                        in.append(buf, off+1, len-1);
                    }
                    break;

                case 0x01: //KEEP ALIVE
                    break;

                case 0x02: //SUCCESSFUL ACKNOWLEDGMENT
                    out.setAckReady();
                    break;

                case 0x03: //FAILURE ACKNOWLEDGMENT
                    send(lastPacket);
                    break;

                case 0x05: //PEER CLOSED
                    peerClosed();
                    break;
            }
        }
    }

    public void send(DatagramPacket packet)throws IOException {
        if(!out.isClosed() && !isClosed()){
            lastPacket = packet;
            server.getServer().send(packet);
        }
    }

    private void sendSuccessAcknowledgment()throws IOException {
        if(!out.isClosed() && !isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 56)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 48)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 40)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 32)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 24)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 16)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >>  8)),
                    (byte) (0xff & key.getUUID().getMostSignificantBits()),

                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 56)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 48)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 40)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 32)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 24)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 16)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >>  8)),
                    (byte) (0xff & key.getUUID().getLeastSignificantBits()),

                    0x02
            };
            server.getServer().send(new DatagramPacket(b, b.length, key.getAddress(), key.getPort()));
        }
    }

    private void sendFailureAcknowledgment()throws IOException {
        if(!out.isClosed() && !isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 56)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 48)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 40)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 32)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 24)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 16)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >>  8)),
                    (byte) (0xff & key.getUUID().getMostSignificantBits()),

                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 56)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 48)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 40)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 32)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 24)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 16)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >>  8)),
                    (byte) (0xff & key.getUUID().getLeastSignificantBits()),

                    0x03
            };
            server.getServer().send(new DatagramPacket(b, b.length, key.getAddress(), key.getPort()));
        }
    }

    public void sendPunch()throws IOException {
        if(!out.isClosed() && !isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 56)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 48)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 40)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 32)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 24)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >> 16)),
                    (byte) (0xff & (key.getUUID().getMostSignificantBits() >>  8)),
                    (byte) (0xff & key.getUUID().getMostSignificantBits()),

                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 56)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 48)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 40)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 32)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 24)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >> 16)),
                    (byte) (0xff & (key.getUUID().getLeastSignificantBits() >>  8)),
                    (byte) (0xff & key.getUUID().getLeastSignificantBits()),

                    0x01
            };
            server.getServer().send(new DatagramPacket(b, b.length, key.getAddress(), key.getPort()));
        }
    }

    public void shutdownInputStream()throws IOException {
        if(!in.isClosed() && !isClosed()){
            in.close();
        }
    }

    public void shutdownOutputStream()throws IOException {
        if(!in.isClosed() && !isClosed()){
            out.close();
        }
    }

    public abstract boolean isClosed();

    public abstract void close();

    abstract void peerClosed();
}
