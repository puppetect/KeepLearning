# Nginx

## Starting, Stopping, and Reloading Configuration
To start nginx, run the executable file
nginx -s *signal*
- stop — fast shutdown
- quit — graceful shutdown
- reload — reloading the configuration file
- reopen — reopening the log files

## Configuration File’s Structure

nginx consists of modules which are controlled by directives specified in the configuration file. Directives are divided into simple directives and block directives. A simple directive consists of the name and parameters separated by spaces and ends with a semicolon (;). A block directive has the same structure as a simple directive, but instead of the semicolon it ends with a set of additional instructions surrounded by braces ({ and }). If a block directive can have other directives inside braces, it is called a context (examples: events, http, server, and location).

Directives placed in the configuration file outside of any contexts are considered to be in the main context. The events and http directives reside in the main context, server in http, and location in server.

The rest of a line after the # sign is considered a comment.

## Serving Static Content

Generally, the configuration file may include several server blocks distinguished by ports on which they listen to and by server names. Once nginx decides which server processes a request, it tests the URI specified in the request’s header against the parameters of the location directives defined inside the server block.
This location block specifies the “/” prefix compared with the URI from the request. For matching requests, the URI will be added to the path specified in the root directive

http {
    server {
        location / {
            root /data/www;
        }
        location /images/ {
            root /data;
        }
    }
}

## Setting Up a Simple Proxy Server

In the first location block, put the proxy_pass directive with the protocol, name and port of the proxied server specified in the parameter (in our case, it is http://localhost:8080)

The parameter in location block is a regular expression matching all URIs ending with .gif, .jpg, or .png. A regular expression should be preceded with ~. The corresponding requests will be mapped to the /data/images directory.

server {
    location / {
        proxy_pass http://localhost:8080;
    }

    location ~ \.(gif|jpg|png)$ {
        root /data/images;
    }
}

When nginx selects a location block to serve a request it first checks location directives that specify prefixes, remembering location with the longest prefix, and then checks regular expressions. If there is a match with a regular expression, nginx picks this location or, otherwise, it picks the one remembered earlier.
