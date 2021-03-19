package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

public class UDPOutputStream extends OutputStream {

    private UDPSocket socket;
    private int order = 0;
    private ByteBuffer buffer = new ByteBuffer(65507);
    private boolean closed, ackReady = true;

    public UDPOutputStream(UDPSocket socket)throws IOException {
        this.socket = socket;
        writeKey();
    }

    @Override
    public void write(int b)throws IOException {
        byte[] buf = { (byte)b };
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf)throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int off, int len)throws IOException {
        if(!closed && !socket.isClosed()){
            if(socket.isNoDelay()){
                buffer.put(buf, off, len);
                flush();

            }else{
                if(buffer.getLength()+len >= buffer.getCapacity()){
                    while(len > 0){
                        int rem = (buffer.getRemaining() < len) ? buffer.getRemaining() : len;
                        buffer.put(buf, off, rem);
                        off += rem;
                        len -= rem;
                        flush();
                    }
                }else{
                    buffer.put(buf, off, len);
                }
            }

        }else{
            throw new IOException("OutputStream is closed.");
        }
    }

    private void writeKey()throws IOException {
        if(!closed && !socket.isClosed()){
            buffer.put(new byte[]{
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 56)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 48)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 40)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 32)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 24)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 16)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >>  8)),
                    (byte) (0xff & socket.getKey().getUUID().getMostSignificantBits()),

                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 56)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 48)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 40)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 32)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 24)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 16)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >>  8)),
                    (byte) (0xff & socket.getKey().getUUID().getLeastSignificantBits()),

                    0x00
            });

            if(socket.isSafeMode()){
                buffer.put(new byte[]{
                        (byte) (0xff & (order >> 24)),
                        (byte) (0xff & (order >> 16)),
                        (byte) (0xff & (order >> 8)),
                        (byte) (0xff & order)
                });
                order++;
            }
        }
    }

    public void setAckReady(){
        ackReady = true;
    }

    @Override
    public void flush()throws IOException {
        if(buffer.getLength() > 17){
            if(socket.isSafeMode()){
                long now = System.currentTimeMillis();
                while(!ackReady){
                    if(now+socket.getTimeout() <= System.currentTimeMillis()){
                        closed = true;
                        return;
                    }
                }
                ackReady = false;

                if(closed && socket.isClosed()){
                    throw new IOException("OutputStream is closed.");
                }
            }

            byte[] b = new byte[buffer.getLength()];
            buffer.get(b);

            socket.send(new DatagramPacket(b, b.length, socket.getAddress(), socket.getPort()));
            writeKey();
        }
    }

    public boolean isClosed(){
        return closed;
    }

    public void close(){
        closed = true;
    }
}
