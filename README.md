BetterUDPSocket
========


This helps you handle UDP protocol similar to TCP. This will sort your packets into UDPSocket's similar to TCP Socket. You can then write to or read from the socket.
As you may know DatagramSocket's don't have continuous I/O. I have made some custom I/O to make allow you to send and receive similar to TCP.

Usage
-----
An example of this project can be found: (https://github.com/DrBrad/BetterUDPSocket/blob/main/src/unet/uncentralized/betterudpsocket/Test.java)

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

**Closing server socket & sockets**
```Java
server.close(); //CLOSES THE SERVER
socket.close(); //CLOSE THE SOCKET - WONT CLOSE THE SERVER
```

**Set SafeMode - ACK**

This will ensure all packets arrive at the destination in order.
```Java
server.setSafeMode(true);
```


License
-----------
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
