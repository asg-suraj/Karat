package com.dcb.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ==========================================
// 4. GENERIC REPOSITORY CONTRACT
// ==========================================
// EXPLANATION: This is the core repository interface.
// We use bounded generics (<T extends BaseEntity<ID>, ID extends Serializable>) to guarantee
// that the repository only manages valid entities with serializable identifiers.

public interface Repository<T extends BaseEntity<ID>, ID extends Serializable> {

  // EXPLANATION: Covariant return type (<S extends T>).
  // If you pass a subclass of T (e.g., AdminUser extends User), it returns that exact subclass (AdminUser).
  // This eliminates the need for the caller to cast the returned object.
  <S extends T> S save(S entity);

  // EXPLANATION: Default method for bulk saving.
  // It relies on the abstract save() method, allowing implementers (like an SQL repo)
  // to get bulk functionality for free without writing extra code.
  default <S extends T> List<S> saveAll(Iterable<S> entities) {
    List<S> saved = new ArrayList<>();
    for (S e : entities) {
      saved.add(save(e));
    }
    return saved;
  }

  // EXPLANATION: Returning Optional avoids NullPointerExceptions.
  // It forces the caller to explicitly handle the case where the entity is not found in the database.
  Optional<T> findById(ID id);

  // EXPLANATION: Reuses findById to check for existence,
  // keeping implementations DRY (Don't Repeat Yourself).
  default boolean existsById(ID id) {
    return findById(id).isPresent();
  }

  List<T> findAll();

  //EXPLANATION: Reuses findById to safely gather multiple records,
  // ignoring IDs that don't exist.
  default List<T> findAllById(Iterable<ID> ids) {
    List<T> list = new ArrayList<>();
    for (ID id : ids) {
      findById(id).ifPresent(list::add);
    }
    return list;
  }

  long count();

  //Explanation Return boolean to indicate if deletion is actually done
  boolean deleteById(ID id);

  // EXPLANATION: Extracts the ID from the entity via the BaseEntity contract,
  // then delegates the actual work to deleteById.
  default  boolean delete(T entity){
    ID id  = entity.getId();
    return  (id!=null) && deleteById(id);
  }

  default  void deleteAll(Iterable<? extends  T> entities){
    for(T t : entities){
      delete(t);
    }
  }

  // EXPLANATION: Default pagination implementation built on top of findAll().
  // NOTE: This fetches everything into memory. In a real-world production database environment (SQL/NoSQL),
  // the implementing class should override this method to perform pagination at the database query level
  // (e.g., using SQL LIMIT and OFFSET) for better performance.
  default Page<T> findPage(Pageable pageable) {
    List<T> all = findAll();
    int total = all.size();

    int offset = pageable.getOffset();
    int size   = pageable.getPageSize();

    if (offset >= total) {
      // EXPLANATION: Return an empty page if the offset exceeds the total records.
      return new Page<>(List.of(), pageable.getPageNumber(), size, total);
    }

    int toIndex = Math.min(offset + size, total);
    List<T> content = all.subList(offset, toIndex);

    return new Page<>(new ArrayList<>(content), pageable.getPageNumber(), size, total);
  }


}
