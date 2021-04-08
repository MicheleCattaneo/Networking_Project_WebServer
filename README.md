# Networking_Project_WebServer

### Implementation of a Web Server using a subset of http requests

### **Usage**

```shell
// generate .class files
javac server/*.java
// run with default port
java server.Main
// run with given port
java server.Main 8080
```

Start the program by passing an optional port number argument. By default, if no argument is given, it will listen on port 80.
The server will try to set itself up, by reading the `vhosts.conf` file, which specifies all the domains hosted on this server.
On success the server will start accepting and handling concurrent requests.

---

### **Basic request example**

The requests need to follow the http standards. After opening the server with port 80 ( or any other ), we can send HTTP requests in different ways:

**Netcat** example:

```shell
printf "GET / HTTP/1.0\r\n\r\n" | nc 127.0.0.1 80
```

In HTTP/1.0 the `Host` header is not mandatory, and in this case the first domain found in the `vhosts.conf` file will be used. Therefore this is a valid http request. With this version of http there is no support for `keep-alive` connections. More examples with `http/1.1` later.

```shell
printf "NTW21INFO / HTTP/1.0\r\n\r\n" | nc 127.0.0.1 80
```

This command will send a response with informations about the administrator of the domain. In this case, since the request is again a 1.0, if the host is not specified, the default one is used.

---

**Postman** example:

Writing the whole http request in the terminal can take time. Using Postman instead we can send request having a graphical interface.

```http
GET http://localhost:80 /home.html
```

Then, on the headers tab, look at the hidden ones and remove the default `Host` header, as it will be set to be `localhost` . Use instead the desired host, as for example `michelecattaneo.ch`. Note that postman will use HTTP/1.1 by default and if you want the connection to close after the reponse the `Connection: close` header should be set.

---

**Web Browser** example:

Using a web browser you have no control over the headers sent by the client. You will then have to edit the hosts file of your operating system to map your 127.0.0.1 IP adress to your domains. For MacOS/Linux:

```bash
sudo vim /etc/hosts
```

Will open the hosts file. Edit it to add the following lines for each domain you want:

```
127.0.0.1    'yourdomain'
```

After that you will be able to access your domain from your browser, which will map to the localhost IP adress. You might need to remove the dns caches for it to work.

---

### File System access

Each domain has a dedicated folder. Requests to a specific host are limited in the access to files/objects that are inside that specific folder. For example:

```http
GET /../martinogiorgi.ch/home.html HTTP/1.1
Host: michelecattaneo.ch
Connection: close


```

Would return a `403 FORBIDDEN` as we would be trying to access a folder outside the scope of the Host folder.

     _________
    / ======= \
   / __________\
  | ___________ |
  | | -       | |
  | |         | |
  | |_________| |________________________
  \=____________/                        )
  / """"""""""" \                       /
 / ::::::::::::: \                  =D-'
(_________________)