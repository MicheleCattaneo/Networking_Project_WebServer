# Networking_Project_WebServer
### Implementation of a Web Server using a subset of http requests

### **Usage**

Start the program by passing an optional port number argument. By default, if no argument is given, it will listen on port 80.
The server will try to set itself up, by reading the `vhosts.conf` file, which specifies all the domains hosted on this server.
On success the server will start accepting and handling concurrent requests. 

### **Basic request example**

The requests need to follow the http standards.

Netcat example:

Run the server on port 80 and run this command on a terminal to start a connection

`netcat localhost 80`

Add the following request:

```http
GET /home.html HTTP/1.0

```

To finish an http request you need  `\r\n` , in this case an empty line is delimiting your request and the response will be generated. In HTTP/1.0 the `Host` header is not mandatory, and in this case the first domain found in the `vhosts.conf` file will be used. With this version of http there is no support for `keep-alive` connections. More examples with `http/1.1` later.

### File System access

Each domain has a dedicated folder. Requests to a specific host are limited in the access to files/objects that are inside that specific folder.