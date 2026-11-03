package com.jouletracker.platform.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

/**
 * Hibernate physical naming strategy that converts camel case to snake case.
 * Table names are also pluralized before snake-case conversion.
 */
public class SnakeCaseWithPluralizedTabledPhysicalNamingStrategy implements PhysicalNamingStrategy {
  /**
   * Determine the physical catalog name from the given logical name
   *
   * @param logicalName
   * @param jdbcEnvironment
   */
  @Override
  public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return this.toSnakeCase(logicalName);
  }

  /**
   * Determine the physical schema name from the given logical name
   *
   * @param logicalName
   * @param jdbcEnvironment
   */
  @Override
  public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return this.toSnakeCase(logicalName);
  }

  /**
   * Determine the physical table name from the given logical name
   *
   * @param logicalName
   * @param jdbcEnvironment
   */
  @Override
  public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return this.toSnakeCase(this.toPlural(logicalName));
  }

  /**
   * Determine the physical sequence name from the given logical name
   *
   * @param logicalName
   * @param jdbcEnvironment
   */
  @Override
  public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return this.toSnakeCase(logicalName);
  }

  /**
   * Determine the physical column name from the given logical name
   *
   * @param logicalName
   * @param jdbcEnvironment
   */
  @Override
  public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return this.toSnakeCase(logicalName);
  }

  /**
   * Convert the given identifier to snake case.
   * @param identifier original identifier
   * @return Snake case identifier
   */
  private Identifier toSnakeCase(final Identifier identifier) {
    if (identifier == null) return null;

    final String regex = "([a-z])([A-Z])";
    final String replacement = "$1_$2";
    final String newName = identifier.getText().replaceAll(regex, replacement).toLowerCase();
    return Identifier.toIdentifier(newName);
  }

  /**
   * Convert the given identifier to plural form.
   * @param identifier original identifier
   * @return Plural form identifier
   */
  private Identifier toPlural(final Identifier identifier) {
    final String newName = pluralize(identifier.getText());
    return Identifier.toIdentifier(newName);
  }
}
