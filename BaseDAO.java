package dao;

import java.util.List;

/**
 * Generic Data Access Object (DAO) interface defining standard CRUD operations.
 * 
 * <p>This interface provides a contract for all DAO implementations in the system.
 * It defines the basic operations that can be performed on entities: create, read,
 * update, and delete. All concrete DAO classes implement this interface, allowing
 * them to be used interchangeably through polymorphism.
 * 
 * <p>POLYMORPHISM: All DAO classes implement this interface
 * <p>This allows different DAOs to be used interchangeably through this interface
 * 
 * @param <T> Entity type (must extend Entity)
 * @author GreenGrocer Team
 * @version 1.0
 */
public interface BaseDAO<T> {

    /**
     * Finds an entity by its unique identifier.
     * 
     * @param id The unique identifier of the entity to find
     * @return The entity if found, or null if not found
     */
    T findById(int id);

    /**
     * Finds all entities of this type.
     * 
     * @return A list of all entities, or an empty list if none found
     */
    List<T> findAll();

    /**
     * Saves a new entity to the database.
     * 
     * @param entity The entity to save
     * @return true if the save was successful, false otherwise
     */
    boolean save(T entity);

    /**
     * Updates an existing entity in the database.
     * 
     * @param entity The entity to update (must have a valid ID)
     * @return true if the update was successful, false otherwise
     */
    boolean update(T entity);

    /**
     * Deletes an entity by its unique identifier.
     * 
     * @param id The unique identifier of the entity to delete
     * @return true if the deletion was successful, false otherwise
     */
    boolean delete(int id);
}
