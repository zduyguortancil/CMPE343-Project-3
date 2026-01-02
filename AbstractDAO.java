package dao;

import model.Entity;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Data Access Object (DAO) providing common database operations.
 * 
 * <p>This abstract class implements BaseDAO and provides a template method pattern
 * for database operations. It defines abstract methods that must be implemented
 * by concrete DAO subclasses to handle entity-specific mapping and table information.
 * 
 * <p>Common operations provided:
 * <ul>
 *   <li>Find entity by ID</li>
 *   <li>Find all entities</li>
 *   <li>Delete entity by ID</li>
 *   <li>Default implementations for save/update (can be overridden)</li>
 * </ul>
 * 
 * <p>INHERITANCE: Implements BaseDAO, extended by concrete DAOs
 * <p>POLYMORPHISM: Template Method Pattern - abstract methods called by concrete implementations
 * <p>ENCAPSULATION: Protected utility methods
 * 
 * @param <T> Entity type (must extend Entity)
 * @author GreenGrocer Team
 * @version 1.0
 */
public abstract class AbstractDAO<T extends Entity> implements BaseDAO<T> {

    /**
     * Gets a database connection.
     * 
     * <p>This protected method provides access to database connections for subclasses.
     * Uses DBUtil to obtain connections.
     * 
     * @return A database connection
     * @throws Exception If a database access error occurs
     */
    protected Connection getConnection() throws Exception {
        return DBUtil.getConnection();
    }

    // ========== ABSTRACT METHODS (POLYMORPHISM - Template Method Pattern)
    // ==========

    /**
     * Maps a ResultSet row to an entity object.
     * 
     * <p>This abstract method must be implemented by each subclass to convert
     * a database row (ResultSet) into the appropriate entity object. The
     * ResultSet cursor is positioned at the row to be mapped.
     * 
     * @param rs The ResultSet positioned at the row to map
     * @return An entity object created from the ResultSet row
     * @throws Exception If an error occurs during mapping
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws Exception;

    /**
     * Gets the database table name for this entity type.
     * 
     * <p>This abstract method must be implemented by each subclass to return
     * the name of the database table that stores this entity type.
     * 
     * @return The database table name
     */
    protected abstract String getTableName();

    /**
     * Gets the primary key column name for this entity type.
     * 
     * <p>This abstract method must be implemented by each subclass to return
     * the name of the primary key column in the database table.
     * 
     * @return The primary key column name
     */
    protected abstract String getIdColumnName();

    // ========== CONCRETE IMPLEMENTATIONS (INHERITANCE) ==========

    /**
     * Finds an entity by its ID using the Template Method Pattern.
     * 
     * <p>This method uses the abstract methods getTableName(), getIdColumnName(),
     * and mapResultSetToEntity() to perform the query. The actual entity mapping
     * is delegated to the subclass implementation.
     * 
     * <p>POLYMORPHISM: Calls abstract mapResultSetToEntity() which is implemented by subclasses
     * 
     * @param id The unique identifier of the entity to find
     * @return The entity if found, or null if not found or an error occurs
     */
    @Override
    public T findById(int id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + "=?";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs); // POLYMORPHISM: Calls subclass implementation
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all entities of this type.
     * 
     * <p>Retrieves all records from the table and maps them to entity objects
     * using the abstract mapResultSetToEntity() method.
     * 
     * <p>POLYMORPHISM: Uses abstract methods implemented by subclasses
     * 
     * @return A list of all entities, or an empty list if none found or an error occurs
     */
    @Override
    public List<T> findAll() {
        List<T> list = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName();

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs)); // POLYMORPHISM
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Saves a new entity to the database.
     * 
     * <p>Default implementation throws UnsupportedOperationException.
     * Subclasses should override this method to provide entity-specific
     * save functionality.
     * 
     * @param entity The entity to save
     * @return true if the save was successful, false otherwise
     * @throws UnsupportedOperationException If not implemented by subclass
     */
    @Override
    public boolean save(T entity) {
        throw new UnsupportedOperationException("save() not implemented for " + getClass().getSimpleName());
    }

    /**
     * Updates an existing entity in the database.
     * 
     * <p>Default implementation throws UnsupportedOperationException.
     * Subclasses should override this method to provide entity-specific
     * update functionality.
     * 
     * @param entity The entity to update
     * @return true if the update was successful, false otherwise
     * @throws UnsupportedOperationException If not implemented by subclass
     */
    @Override
    public boolean update(T entity) {
        throw new UnsupportedOperationException("update() not implemented for " + getClass().getSimpleName());
    }

    /**
     * Deletes an entity by its ID.
     * 
     * <p>Uses the abstract methods getTableName() and getIdColumnName()
     * to construct the DELETE SQL statement.
     * 
     * @param id The unique identifier of the entity to delete
     * @return true if the deletion was successful, false otherwise
     */
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + "=?";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
