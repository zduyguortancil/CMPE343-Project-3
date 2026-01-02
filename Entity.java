package model;

/**
 * Abstract base class for all entities in the GreenGrocer system.
 * 
 * <p>This abstract class provides a common foundation for all model entities.
 * It defines a unique identifier (id) and requires subclasses to implement
 * a display name method. All model classes in the system extend this class.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Unique identifier (id) for each entity</li>
 *   <li>Abstract display name method for polymorphic behavior</li>
 *   <li>Entity type identification</li>
 *   <li>String representation using display name</li>
 * </ul>
 * 
 * <p>ENCAPSULATION: protected/private fields with getters/setters
 * <p>INHERITANCE: All model classes extend this
 * <p>POLYMORPHISM: Abstract methods overridden by subclasses
 * 
 * @author GreenGrocer Team
 * @version 1.0
 */
public abstract class Entity {

    /**
     * The unique identifier for this entity.
     * Protected access allows subclasses to access it directly.
     */
    protected int id;

    /**
     * Default constructor.
     * 
     * <p>Creates a new Entity instance with id set to 0.
     */
    public Entity() {
    }

    /**
     * Constructor with ID.
     * 
     * @param id The unique identifier for this entity
     */
    public Entity(int id) {
        this.id = id;
    }

    /**
     * Gets the unique identifier.
     * 
     * @return The entity ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier.
     * 
     * @param id The entity ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets a display name for this entity.
     * 
     * <p>This is an abstract method that must be implemented by subclasses.
     * Each subclass should return a formatted string that represents the entity
     * in a human-readable way.
     * 
     * @return A formatted display string for this entity
     */
    public abstract String getDisplayName();

    /**
     * Gets the entity type name.
     * 
     * <p>Returns the simple class name of this entity. This method can be
     * overridden by subclasses if a different type name is desired.
     * 
     * @return The entity type name (class simple name)
     */
    public String getEntityType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns a string representation of this entity.
     * 
     * <p>Uses the polymorphic getDisplayName() method to create a string
     * representation. Format: "EntityType: displayName"
     * 
     * @return A string representation of this entity
     */
    @Override
    public String toString() {
        return getEntityType() + ": " + getDisplayName();
    }
}
