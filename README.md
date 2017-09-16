[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/venkateshmantha/MusiCLI/blob/master/LICENSE)

# Chatroom
A server-client multithreaded chat application supporting file transfer submitted as course work for the course Computer Networks CNT 5106C Fall 2016.

## Usage guide

The server starts on the default port 2222. To start it on a different port, type

_java Server \<portnumber>_
  
_java Client localhost \<portnumber>_ to connect to the server.
  
### Message passing

1.To broadcast

_broadcast \<message>_ without '<>'

2.To unicast

_unicast @clientname \<message>_

3.To blockcast

_blockcast @clientname \<message>_
  
### File transfer

1.To broadcast

_file broadcast \<filepath>_ without '<>' (make sure the file exists and the path is correct to avoid unexpected behaviour)

2.To unicast

_file unicast @clientname \<filepath>_

## License

This project is licensed under the MIT License.
