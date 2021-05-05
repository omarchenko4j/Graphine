# Graphine

---

**Graphine** is a *Java framework* that provides
an [Object/Relational Mapping (ORM)](https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping)
implementation by generating native human-readable JDBC code.

Graphine does not support *lazy loading*, *caching*, *dirty checking* and other advanced *JPA features*. It focuses on
high-level [Domain-Driven Design (DDD)](https://en.wikipedia.org/wiki/Domain-driven_design#Building_blocks)
concepts and generates low-level JDBC code from them.

## Features

- Simplified entity markup;
- Spring-Data style repositories;
- Native JDBC code generation;
- Human-readable generated source code;
- NO reflection.

## Getting Started

### Installation

> **Warning!** While there are no artifacts in the *Maven Central* (**it is temporary**), so they must be placed **manually**.


#### Maven

1. Download artifacts:

[graphine-core-0.2.0.jar](https://github.com/MarchenkoProjects/Graphine/releases/download/v0.2.0/graphine-core-0.2.0.jar)
   
[graphine-processor-0.2.0.jar](https://github.com/MarchenkoProjects/Graphine/releases/download/v0.2.0/graphine-processor-0.2.0.jar)

2. Add dependencies:

```xml
<dependency>
  <groupId>io.graphine.core</groupId>
  <artifactId>graphine-core</artifactId>
  <version>0.2.0</version>
  <scope>system</scope>
  <systemPath>./graphine-core-0.2.0.jar</systemPath> <!-- path to downloaded artifacts -->
</dependency>
<dependency>
  <groupId>io.graphine.processor</groupId>
  <artifactId>graphine-processor</artifactId>
  <version>0.2.0</version>
  <scope>system</scope>
  <systemPath>./graphine-processor-0.2.0.jar</systemPath> <!-- path to downloaded artifacts -->
</dependency>
```

3. Configure [maven-compiler-plugin](https://maven.apache.org/plugins/maven-compiler-plugin):

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessors>
      <processor>io.graphine.processor.GraphineRepositoryProcessor</processor>
    </annotationProcessors>
  </configuration>
</plugin>
```

### Writing code

Here is a small snippet that shows *Graphine in action*.

```java
/**
 * Your model.
 */
@Entity(table = "users")
public class User {
    @Id
    private Integer id;
    @Attribute
    private String login;
    @Attribute
    private String email;
    
    // Default ctor, getters/setters, ...
}

/**
 * Repository for your model.
 */
@Repository(User.class)
public interface UserRepository {
    User findById(int id);
}

/**
 * After building, Graphine will generate the following class.
 */
@Generated("io.graphine.processor.GraphineRepositoryProcessor")
public class GraphineUserRepository implements UserRepository {
    private final DataSource dataSource;

    public GraphineUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User findById(int id) {
        try (Connection _connection = dataSource.getConnection()) {
            String _query = "SELECT id, login, email FROM users WHERE id = ?";
            try (PreparedStatement _statement = _connection.prepareStatement(_query)) {
                _statement.setInt(1, id);
                try (ResultSet _resultSet = _statement.executeQuery()) {
                    if (_resultSet.next()) {
                        User _user = new User();
                        _user.setId(_resultSet.getInt(1));
                        _user.setLogin(_resultSet.getString(2));
                        _user.setEmail(_resultSet.getString(3));
                        if (_resultSet.next()) {
                            throw new NonUniqueResultException();
                        }
                        return _user;
                    }
                    return null;
                }
            }
        }
        catch (SQLException e) {
            throw new GraphineException(e);
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
        User user = userRepository.findById(1);
        System.out.println(user);
    }
}
```

But this example is too simple. More complex examples can be found in
the [graphine-test](https://github.com/MarchenkoProjects/Graphine/tree/develop/graphine-test) module.

## License

Graphine is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
