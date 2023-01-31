# Introduction

This repository contains walkthrough samples to put the hand in the world of istio. It is mainly oriented on the security part of istio.

It relies on a java samples. You can find them under the **resources** folder. Otherwise we will use samples provided by istio during its installation, mainly
**sleep** and **httpbin** applications.

Each folder contains a Readme.md file with instructions and a bit of explainations.

Those tutorials do not make usage of the dashboards that are available when installing istio. You can use them to have a visual representation
of what is happening live. The istio documentation on those dashboards are clear enough and then not covered here.

# What you need to run the samples

All the samples have been run on a local docker desktop installation with kubernetes enabled.

Istio 1.16.0 has been installed on the home directory. The components installed are: core, daemon and ingress.

You also need a local OIDC provider for some parts. I personaly use a small Java program, that supports the minimal requirement needed by the
examples (you can find a light OIDC server into the [following directory](resources/oidc-light):

- a endpoint supporting basic auth, that returns a JWT
- a endpoint with the JWK used to generate the JWT
- the userinfo enpoint to return a full description of the user with its associated authorities

# A very very short introduction to how istio works

Whenever you add the istio support to one of your POD, there is a sidecar container that is added to your POD. That sidecar container is the istio component.
Each network communication going out or reaching your component will first go through the istio container that will first verify if any security policy is defined.

# Table of content

- [00-mTLS-basics](00-mTLS-basics)<br/>
  Shows the impact of the Mutual TLS feature.
- [01-nonsecured-backend](01-nonsecured-backend)<br/>
  Shows how we can protect an application that do not contain any security feature and force the presence of a JWT in each request.
- [02-ext-authz](02-ext-authz)<br/>
  Shows how we can create our own authorization policy.

# Activate istio logs

In order to understand and/or debug what happens with istio, we can activate some logs on the istio component.

When running the following command **istioctl proxy-config log $PODID**, we have a picture of each component inside the istio container of our POD and their log level.

If we want to change one level, we can do it like that: **istioctl proxy-config log $PODID --level rbac:debug**. During those tutorials, it might be interesting to
set to **debug** the levels of **rbac** and **jwt**.

Then you can tail those logs very simply: **kubectl logs -f $PODID -c istio-proxy**.
