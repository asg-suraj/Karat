package com.dcb.jpa;

import java.io.Serializable;

//Base contract for all entities managed by the repository
//ID Identifier Type which must be serializable

// ==========================================
// 1. BASE ENTITY CONTRACT
// ==========================================
// EXPLANATION: We define a base contract that all managed entities must implement.
// By forcing the ID to extend Serializable, we ensure the primary keys can be
// safely serialized (useful for caching, network transmission, or file storage).
// It also guarantees that the Repository can securely call `getId()` on any entity.
public interface BaseEntity<ID extends Serializable> {
  ID getId();

}
