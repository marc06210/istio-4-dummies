# Getting Started

This application relies on spring-security. The official document is available
[here](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

It exposes a public endpoint:

- /public/hello

And some protected resources:

- /me<br/>
  returns the **Principal** as it is known by **spring-security**
- /me2<br/>
  returns a human-readable and custom version of the **Principal**
- /hello
  returns hello world when the connected user owns the correct Spring profile
- /micro
  invokes another URL and concatenates the two answers or the error code.

# To build and register the docker image

docker
