BetterUDPSocket
========


This helps you handle UDP protocol similar to TCP. This will sort your packets into UDPSocket's similar to TCP Socket. You can then write to or read from the socket.
As you may know DatagramSocket's don't have continuous I/O. I have made some custom I/O to make allow you to send and receive similar to TCP.

Usage
-----
An example of this project can be found: [Test Communication](https://github.com/DrBrad/BetterUDPSocket/blob/main/src/unet/uncentralized/betterudpsocket/Samples/Test.java)

**Creating a server socket**
```Java
int port = 8080;
UDPServerSocket server = new UDPServerSocket(port);
```

**Creating a socket from server**
```Java
InetAddress address = InetAddress.getByName("localhost"); //TO ADDRESS
int port = 8080; //TO PORT
UDPSocket socket = server.create(address, port);
```

**Receiving socket from server**
```Java
server.addUDPListener(new UDPServerSocket.UDPListener(){
    @Override
    public void accept(UDPSocket socket){
    }
}
```

**Closing server server & sockets**
```Java
server.close(); //CLOSES THE SERVER
socket.close(); //CLOSE THE SOCKET - WONT CLOSE THE SERVER
```

**Set SafeMode - ACK**

This will ensure all packets arrive at the destination in order with acknowledgment.
```Java
server.setSafeMode(true);
```

**Set KeepAlive**

NAT will typically close your port after nothing has been sent or received for 30-60 seconds. Enabling KeepAlive will send a packet for KeepAlive every 25 seconds to ensure the port stays open. If you are using this for P2P where both peers haven't port forwarded only one peer will need to do KeepAlive.
```Java
server.setKeepAlive(true);
```

**Set NoDelay**

This will send a packet everytime you write to the output stream, there is no need to flush.
```Java
server.setNoDelay(true);
```

**Port Forward with UPnP**

You can port forward or get your external IP address using UPnP if its availible.
```Java
server.openPort();
server.closePort();
server.getExternalIP();
```

UDP HolePunching
-----
![UDP Hole-Punch Image](https://raw.githubusercontent.com/DrBrad/BetterUDPSocket/main/hole_punch.png)

An example for hole punching with this project can be found:
[Hole-Punch Server](https://github.com/DrBrad/BetterUDPSocket/blob/main/src/unet/uncentralized/betterudpsocket/Samples/HolePunchServer.java), 
[Hole-Punch Client](https://github.com/DrBrad/BetterUDPSocket/blob/main/src/unet/uncentralized/betterudpsocket/Samples/HolePunchClient.java)
If you use hole punching the server must be port forwarded while both clients don't have to be. Both clients will then be able to communicate directly without any server relay. This will only work with Non Symmetric NATs.

UDP File Transfer
-----
An example for file transfer with I/O with this project can be found:
[File-Transfer](https://github.com/DrBrad/BetterUDPSocket/blob/main/src/unet/uncentralized/betterudpsocket/Samples/FileTransfer.java)
