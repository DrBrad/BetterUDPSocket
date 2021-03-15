package unet.uncentralized.betterudpsocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

public class UDPOutputStream extends OutputStream {

    private UDPSocket socket;
    private int max = 65535;
    private ByteArrayOutputStream out = new ByteArrayOutputStream(max);

    public UDPOutputStream(UDPSocket socket)throws IOException {
        this.socket = socket;
        writeKey();
    }

    @Override
    public synchronized void write(int b)throws IOException {
        byte[] buff = { (byte)b };
        write(buff, 0, buff.length);
    }

    @Override
    public synchronized void write(byte[] b)throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len)throws IOException {
        if(len+out.size() > max){
            while(len > max-out.size()){
                out.write(b, off, max-out.size());
                flush();
                off += max-out.size();
                len -= max-out.size();
            }

            if(len > 0){
                out.write(b, off, len);
            }
        }else{
            out.write(b, off, len);
        }

        /*

        if(off+len+out.size() > max){

            System.out.println((off+len+out.size())+"  "+max+"  "+len+"  "+out.size()+"  "+((len+out.size())-max));

            out.write(b, off, (len+out.size())-max);
            flush();

            write(b, off+(len-out.size()), len);

            //int l = off+len+out.size()-65535;

            //for()

            /*
            for(int i = 0; i < (off+len+out.size())/max; i++){
                out.write(b, off+(i*max), max-out.size());
                flush();
                /*
                out.write(new byte[]{
                        (byte) (0xff & (socket.getKey() >> 24)),
                        (byte) (0xff & (socket.getKey() >> 16)),
                        (byte) (0xff & (socket.getKey() >> 8)),
                        (byte) (0xff & socket.getKey()) });
                        *./
            }*./


        }else{
            out.write(b, off, len);
        }

        /*
        if(buffer == null){
            buffer = new byte[len];
            System.arraycopy(b, off, buffer, 0, len);

        }else{
            byte[] result = new byte[len+buffer.length];
            System.arraycopy(buffer, 0, result, 0, buffer.length);
            System.arraycopy(b, off, result, buffer.length, len);
            buffer = result;
        }
        */
    }

    private synchronized void writeKey()throws IOException {
        out.write(new byte[]{
                (byte) (0xff & (socket.getKey() >> 24)),
                (byte) (0xff & (socket.getKey() >> 16)),
                (byte) (0xff & (socket.getKey() >> 8)),
                (byte) (0xff & socket.getKey()) });
    }

    @Override
    public synchronized void flush()throws IOException {
        DatagramPacket packet = new DatagramPacket(out.toByteArray(), out.size(), socket.getAddress(), socket.getPort());
        socket.getServer().send(packet);
        out.reset();
        writeKey();

        /*
        if(buffer.length > 65531){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            server.send(packet);

            byte[] result = new byte[buffer.length-65535];
            System.arraycopy(buffer, 65535, result, 0, result.length);
            buffer = result;

        }else{
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            server.send(packet);
            buffer = null;
        }
        */
    }
}
