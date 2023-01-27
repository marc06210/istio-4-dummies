# Introduction

This repository contains walkthrough samples to put the hand in the world of istio. It is mainly oriented on the security part of istio.

It relies on a java sample that is in the resources folder. Otherwise we will use samples provided by istio during its installation, mainly
**sleep** and **httpbin** applications.

Each folder contains a Readme.md file with instructions and a bit of explainations.

# What you need to run the samples

All the samples have been run on a local docker desktop installation with kubernetes enabled.

Istio 1.16.0 has been installed on the home directory. The components installed are: core, daemon and ingress.

# A very very short introduction to how istio works

Whenever you add the istio support to one of your POD, there is a sidecar container that is added to your POD. That sidecar container is the istio component.
Each network communication going out or reaching your component will first go through the istio container that will first verify if any security policy is defined.

# Table of content

- [00-mTLS-basics](00-mTLS-basics)<br/>
  Shows the impact of the Mutual TLS feature.
- [01-nonsecured-backend](01-nonsecured-backend)<br/>
  Shows how we can protect an application that do not contain any security feature and force the presence of a JWT in each request.
- 02-ext-authz (coming soon)<br/>
  Shows how we can create our own authorization policy.
