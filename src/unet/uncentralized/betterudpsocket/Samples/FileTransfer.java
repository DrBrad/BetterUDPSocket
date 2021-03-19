package unet.uncentralized.betterudpsocket.Samples;

import unet.uncentralized.betterudpsocket.UDPServerSocket;
import unet.uncentralized.betterudpsocket.UDPSocket;

import java.io.*;
import java.net.InetAddress;

public class FileTransfer {

    public static void main(String[] args){
        try{
            UDPServerSocket server = new UDPServerSocket(8080);
            server.setSafeMode(true);
            server.addUDPListener(new UDPServerSocket.UDPListener(){
                @Override
                public void accept(UDPSocket socket){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                DataInputStream in = new DataInputStream(socket.getInputStream());

                                FileOutputStream outc = new FileOutputStream(new File("/home/brad/Downloads/NEW_FILE.jpg"));

                                byte[] buffer = new byte[4096];
                                int length;
                                while((length = in.read(buffer)) >= 0){
                                    outc.write(buffer, 0, length);
                                }

                                outc.flush();
                                outc.close();

                            }catch(IOException e){
                                e.printStackTrace();
                            }finally{
                                socket.close();
                                server.close();
                            }
                        }
                    }).start();

                }
            });

            Thread.sleep(1000);

            client();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void client()throws Exception {
        UDPServerSocket client = new UDPServerSocket(8070);
        client.setSafeMode(true);
        UDPSocket c = client.create(InetAddress.getLocalHost(), 8080);

        DataOutputStream out = new DataOutputStream(c.getOutputStream());

        FileInputStream inc = new FileInputStream(new File("/home/brad/Downloads/FILE_NAME.jpg"));

        byte[] buffer = new byte[4096];
        int length;
        while((length = inc.read(buffer)) >= 0){
            out.write(buffer, 0, length);
        }

        inc.close();
        out.flush();

        client.close();
    }
}

