# Graphine - next-generation of Data Access Layer

---

## What is Graphine?

Graphine is a *Java annotation processor* that provides
an [ORM (Object/Relational Mapping)](https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping)
implementation by generating native human-readable JDBC (Java Database Connectivity) code. It does not support *lazy loading*, *caching*,
*dirty checking* and other advanced *JPA features*.

Graphine focuses on some high-level concepts
from [DDD (Domain-Driven Design)](https://en.wikipedia.org/wiki/Domain-driven_design#Building_blocks)
such as **Aggregate** (cluster of **Entities** and **Value Objects**) and **Repository**.

## Features

- Simplified annotation of entities and repositories;
- Compile-time error reporting;
- Native human-readable code generation;
- **NO** reflection;
- **NO** runtime dependencies.

## Requirements

Graphine requires **Java 11** or later.

## Getting Started

### Installation

#### Maven

1. Add dependencies:

```xml
<properties>
  <graphine.version>0.4.1</graphine.version> <!-- Latest version -->
</properties>

<dependency>
  <groupId>io.github.omarchenko4j</groupId>
  <artifactId>graphine-annotation</artifactId>
  <version>${graphine.version}</version>
  <scope>provided</scope> <!-- Graphine is not a runtime dependency! -->
</dependency>
```

2. Configure [maven-compiler-plugin](https://maven.apache.org/plugins/maven-compiler-plugin):

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>${maven-compiler-plugin.version}</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>io.github.omarchenko4j</groupId>
        <artifactId>graphine-processor</artifactId>
        <version>${graphine.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

2.1 Configure Graphine with [Lombok](https://projectlombok.org):

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>${maven-compiler-plugin.version}</version>
  <configuration>
    <annotationProcessorPaths>
      <!-- Order of declaration is important! -->
      <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
      </path>
      <path>
        <groupId>io.github.omarchenko4j</groupId>
        <artifactId>graphine-processor</artifactId>
        <version>${graphine.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

### Documentation

#### Entity

##### Entity to table mapping

The standard behavior is that one entity maps to one database table.
`@Entity` annotation has two attributes to specify *schema* and *table* names in database.
If the *table name* is not specified then the *entity class name* will be used to determine the database table name.

The following transformation pipeline is used by default:
`SNAKE_CASE` | `LOWER_CASE`

For example:

| Entity class name | Table name |
|:------------------|:-----------|
| Order             | order      |
| OrderItem         | order_item |

This behavior can be changed using compiler arguments.

```xml
<compilerArgs>
  <arg>-Agraphine.table_naming_pipeline=SNAKE_CASE|UPPER_CASE</arg>
</compilerArgs>
```

Supported transformation options:

| Transformation option | Description                                         |
|:----------------------|:----------------------------------------------------|
| SNAKE_CASE            | Transforms entity class name to *Snake_Case* format |
| LOWER_CASE            | Transforms entity class name to *lower case* format |
| UPPER_CASE            | Transforms entity class name to *UPPER CASE* format |

> Use `|` separator for pipeline transformation.

Use the following compiler argument to specify the default database schema:

```xml
<compilerArgs>
  <arg>-Agraphine.default_schema={schema_name}</arg>
</compilerArgs>
```

##### Entity requirements
1. Entity must be a *Java class*;
2. Entity class must be annotated with `@Entity` annotation;
3. Entity class must be `public`;
4. Entity class must have *public no-arg constructor*;
5. Entity class must have *at least one* field that is annotated with `@Id` annotation;
6. Entity class must have *getters*/*setters* for all persistent fields.

By default, all fields of an entity class are persistent.

> *Terminology note:* The fields of an entity class are generally referred to as *attributes*.

This behavior can be changed using compiler arguments.

```xml
<compilerArgs>
  <!-- Default behavior - ALL fields are detected as attributes -->
  <arg>-Agraphine.attribute_detection_strategy=ALL_FIELDS</arg>
  <!-- or -->
  <!-- Only annotated fields with @Attribute annotation are detected as attributes -->
  <arg>-Agraphine.attribute_detection_strategy=ANNOTATED_FIELDS</arg>
</compilerArgs>
```

Supported Java types as entity attributes:
- Primitive types: `boolean`, `byte`, `short`, `int`, `long`, `float`, `double`
- Primitive wrapper types: `Boolean`, `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`
- `java.math` types: `BigDecimal`, `BigInteger`
- `java.sql` types: `Date`, `Time`, `Timestamp`
- `java.time` types: `Instant`, `LocalDate`, `LocalTime`, `LocalDateTime`, `Year`, `YearMonth`, `MonthDay`, `Period`, `Duration`
- Array types: `byte[]`
- `String`, `UUID`
- Enumeration types
- Custom Java classes annotated with `@Embeddable` annotation;

#### Embeddable entity (aka *Value Object*)

Embeddable entity requirements:
1. Embeddable entity must be a *Java class*;
2. Embeddable entity class must be annotated with `@Embeddable` annotation;
3. Embeddable entity class must be `public`;
4. Embeddable entity class must have *public no-arg constructor*;
5. Embeddable entity class must have *getters*/*setters* for all persistent fields.

#### Repository

Repository requirements:
1. Repository must be a *Java interface*;
2. Repository interface must be annotated with `@Repository` annotation indicating the entity class of it manages;

Supported queryable method names:

| Method name prefix | Supported return type                                            | Supported parameter type                            |
|:-------------------|:-----------------------------------------------------------------|:----------------------------------------------------|
| findBy...          | `T`, `Optional<T>`                                               | *                                                   |
| findAll            | `Iterable<T>`, `Collection<T>`, `List<T>`, `Set<T>`, `Stream<T>` | `void`                                              |
| findAllBy...       | `Iterable<T>`, `Collection<T>`, `List<T>`, `Set<T>`, `Stream<T>` | *                                                   |
| findFirstBy...     | `T`, `Optional<T>`                                               | *                                                   |
| countAll           | `int`, `long`, `Integer`, `Long`                                 | `void`                                              |
| countAllBy...      | `int`, `long`, `Integer`, `Long`                                 | *                                                   |
| save               | `void`                                                           | `T`                                                 |
| saveAll            | `void`                                                           | `Iterable<T>`, `Collection<T>`, `List<T>`, `Set<T>` |
| update             | `void`                                                           | `T`                                                 |
| updateAll          | `void`                                                           | `Iterable<T>`, `Collection<T>`, `List<T>`, `Set<T>` |
| delete             | `void`                                                           | `T`                                                 |
| deleteBy...        | `void`                                                           | *                                                   |
| deleteAll          | `void`                                                           | `Iterable<T>`, `Collection<T>`, `List<T>`, `Set<T>` |
| deleteAllBy...     | `void`                                                           | *                                                   |

> `T` - Your entity type. `*` - Any type of parameters.

> *Prerequisite to use*: All generated implementations of repositories use [DataSource](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/DataSource.html) as dependency;

### Writing code

Here is a small snippet that shows *Graphine in action*.

```java
/**
 * Your model.
 */
@Getter // Lombok annotation.
@Setter // Lombok annotation.
@Entity(table = "users")
public class User {
    @Id
    private UUID id;
    private String login;
    private String password;
    private String email;
}

/**
 * Repository for your model.
 */
@Repository(User.class)
public interface UserRepository {
    Optional<User> findByLogin(String login);
}

/**
 * After building, Graphine will generate the following class.
 */
@Generated("io.graphine.processor.GraphineProcessor")
public class GraphineUserRepository implements UserRepository {
    private final DataSource dataSource;

    public GraphineUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findByLogin(String login) {
        try (Connection _connection = dataSource.getConnection()) {
            String _query = "SELECT id, login, password, email FROM users WHERE login = ?";
            try (PreparedStatement _statement = _connection.prepareStatement(_query)) {
                AttributeMappers.setString(_statement, 1, login);
                try (ResultSet _resultSet = _statement.executeQuery()) {
                    if (_resultSet.next()) {
                        User _user = new User();
                        _user.setId(AttributeMappers.getUuid(_resultSet, 1));
                        _user.setLogin(AttributeMappers.getString(_resultSet, 2));
                        _user.setPassword(AttributeMappers.getString(_resultSet, 3));
                        _user.setEmail(AttributeMappers.getString(_resultSet, 4));
                        if (_resultSet.next()) {
                            throw new NonUniqueResultException();
                        }
                        return Optional.of(_user);
                    }
                    return Optional.empty();
                }
            }
        }
        catch (SQLException _e) {
            throw new GraphineException(_e);
        }
    }
}

/**
 * Application launch.
 */
public class Application {
    private static final DataSource DATA_SOURCE = createDataSource();

    private static DataSource createDataSource() {
        return null; // Your configured data source.
    }

    public static void main(String[] args) {
        UserRepository userRepository = new GraphineUserRepository(DATA_SOURCE);
        Optional<User> user = userRepository.findByLogin("oleh.marchenko");
        System.out.println(user);
    }
}
```

But this example is too simple. More complex examples can be found in
the [graphine-test](https://github.com/MarchenkoProjects/Graphine/tree/develop/graphine-test) module.

## License

Graphine is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
