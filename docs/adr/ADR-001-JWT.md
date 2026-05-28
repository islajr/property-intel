# ADR-001: RS256 JWT Signing over HS256

> Date: 2026-05-15
> Status: Accepted

## Context

HS256 uses a shared symmetric secret. Any service holding the secret can both verify tokens and create new tokens. This means that a leaked secret enables complete authentication bypass. RS256 uses an asymmetric key pair: the API signs with the private key, and any downstream service can verify with the public key without having the ability to create tokens. Due to the potential for multi-service architecture in this project, and to employ best security practices at every level, RS256 is the correct choice as other services can verify tokens without having signing capability.

## Decision

JWT tokens are signed with an RSA private/public key pair (RS256) rather than a shared secret (HS256).

## Consequences

Key management and rotation could become cumbersome. For a portfolio project, HS256 is usually okay, but we want the best.
This decision choice should be revisited if rotation and management of keys ever becomes a problem.
