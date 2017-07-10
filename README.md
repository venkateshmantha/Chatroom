# chatroom
A server-client multithreaded chat application supporting file transfer submitted as course work for the course Computer Networks.

## Usage guide

The server starts on the default port 2222. To start it on a different port, type

_java Server_ <portnumber>
  
_java Client localhost_ <portnumber> to connect to the server.
  
### Message passing

1.To broadcast

_broadcast_ <message> without '<>'

2.To unicast

_unicast @clientname_ <message>

3.To blockcast

_blockcast @clientname_ <message>
  
### File transfer

1.To broadcast

_file broadcast_ <filepath> without '<>' (make sure the file exists and the path is correct to avoid unexpected behaviour)

2.To unicast

_file unicast @clientname_ <filepath>
