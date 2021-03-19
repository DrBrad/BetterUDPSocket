package unet.uncentralized.betterudpsocket;

import java.nio.BufferOverflowException;

public class ByteBuffer {

    private byte[] buffer;
    private int capacity, length = 0;

    public ByteBuffer(int capacity){
        this.capacity = capacity;
        buffer = new byte[capacity];
    }

    public synchronized void put(byte[] buf){
        put(buf, 0, buf.length);
    }

    public synchronized void put(byte[] buf, int off, int len){
        if(capacity >= length+len){
            byte[] result = new byte[capacity];
            System.arraycopy(buffer, 0, result, 0, length);
            System.arraycopy(buf, off, result, length, len);
            buffer = result;
            length += len;

        }else{
            throw new BufferOverflowException();
        }
    }

    public synchronized int get(byte[] buf){
        return get(buf, 0, buf.length);
    }

    public synchronized int get(byte[] buf, int off, int len){
        len = (len > length) ? length : len;
        System.arraycopy(buffer, off, buf, 0, len);

        //I SEE THE POTENTIAL ISSUE...
        byte[] result = new byte[capacity];
        System.arraycopy(buffer, off+len, result, 0, length-len);
        buffer = result;

        length -= off+len;

        return len;
    }

    public synchronized byte[] toByteArray(){
        return buffer;
    }

    public synchronized int getRemaining(){
        return capacity-length;
    }

    public synchronized int getLength(){
        return length;
    }

    public synchronized int getCapacity(){
        return capacity;
    }
}
