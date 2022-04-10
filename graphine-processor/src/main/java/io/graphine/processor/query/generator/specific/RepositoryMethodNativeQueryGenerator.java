package io.graphine.processor.query.generator.specific;

import io.graphine.processor.metadata.model.entity.EmbeddableEntityMetadata;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.model.repository.method.name.fragment.AttributeChain;
import io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.AndPredicate;
import static io.graphine.processor.metadata.model.repository.method.name.fragment.ConditionFragment.OrPredicate;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public abstract class RepositoryMethodNativeQueryGenerator {
    protected final EntityMetadataRegistry entityMetadataRegistry;

    protected RepositoryMethodNativeQueryGenerator(EntityMetadataRegistry entityMetadataRegistry) {
        this.entityMetadataRegistry = entityMetadataRegistry;
    }

    public final NativeQuery generate(String entityQualifiedName, MethodMetadata method) {
        EntityMetadata entity = entityMetadataRegistry.getEntity(entityQualifiedName);
        return new NativeQuery(generateQuery(entity, method));
    }

    protected abstract String generateQuery(EntityMetadata entity, MethodMetadata method);

    protected List<String> collectColumns(EntityMetadata entity) {
        return entity.getAttributes()
                     .stream()
                     .flatMap(attribute -> getColumn(attribute).stream())
                     .collect(Collectors.toList());
    }

    protected List<String> getColumn(AttributeMetadata attribute) {
        if (attribute instanceof EmbeddedAttributeMetadata) {
            EmbeddedAttributeMetadata embeddedAttribute = (EmbeddedAttributeMetadata) attribute;
            EmbeddableEntityMetadata embeddableEntity =
                    entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
            List<AttributeMetadata> embeddedAttributes = embeddableEntity.getAttributes();

            List<String> columns = new ArrayList<>(embeddedAttributes.size());
            for (AttributeMetadata innerAttribute : embeddedAttributes) {
                String column = embeddedAttribute.overrideAttribute(innerAttribute);
                if (isNull(column)) {
                    columns.addAll(getColumn(innerAttribute));
                    continue;
                }
                columns.add(column);
            }
            return columns;
        }
        return singletonList(attribute.getColumn());
    }

    protected final String generateWhereClause(EntityMetadata entity, ConditionFragment condition) {
        StringJoiner orPredicateJoiner = new StringJoiner(" OR ");

        List<OrPredicate> orPredicates = condition.getOrPredicates();
        for (OrPredicate orPredicate : orPredicates) {
            StringJoiner andPredicateJoiner = new StringJoiner(" AND ");

            List<AndPredicate> andPredicates = orPredicate.getAndPredicates();
            for (AndPredicate andPredicate : andPredicates) {
                AttributeChain attributeChain = andPredicate.getAttributeChain();
                List<String> attributeNames = attributeChain.getAttributeNames();

                AttributeMetadata prevAttribute = null;
                AttributeMetadata attribute = entity.getAttribute(attributeNames.get(0));
                for (int i = 1; i < attributeNames.size(); i++) {
                    prevAttribute = attribute;
                    if (attribute instanceof EmbeddedAttributeMetadata) {
                        EmbeddableEntityMetadata embeddableEntity =
                                entityMetadataRegistry.getEmbeddableEntity(attribute.getNativeType().toString());
                        attribute = embeddableEntity.getAttribute(attributeNames.get(i));
                    }
                }

                List<String> columns = null;
                if (prevAttribute instanceof EmbeddedAttributeMetadata) {
                    EmbeddedAttributeMetadata embeddedAttribute = (EmbeddedAttributeMetadata) prevAttribute;
                    String column = embeddedAttribute.overrideAttribute(attribute);
                    if (nonNull(column)) {
                        columns = singletonList(column);
                    }
                }
                if (isNull(columns)) {
                    columns = getColumn(attribute);
                }

                for (String column : columns) {
                    switch (andPredicate.getOperator()) {
                        case BETWEEN:
                            andPredicateJoiner.add(column + " BETWEEN ? AND ?");
                            break;
                        case NOT_BETWEEN:
                            andPredicateJoiner.add(column + " NOT BETWEEN ? AND ?");
                            break;
                        case NULL:
                            andPredicateJoiner.add(column + " IS NULL");
                            break;
                        case NOT_NULL:
                            andPredicateJoiner.add(column + " IS NOT NULL");
                            break;
                        case BEFORE:
                        case LESS_THAN:
                            andPredicateJoiner.add(column + " < ?");
                            break;
                        case LESS_THAN_EQUAL:
                            andPredicateJoiner.add(column + " <= ?");
                            break;
                        case AFTER:
                        case GREATER_THAN:
                            andPredicateJoiner.add(column + " > ?");
                            break;
                        case GREATER_THAN_EQUAL:
                            andPredicateJoiner.add(column + " >= ?");
                            break;
                        case LIKE:
                        case STARTING_WITH:
                        case ENDING_WITH:
                        case CONTAINING:
                            andPredicateJoiner.add(column + " LIKE ?");
                            break;
                        case NOT_LIKE:
                        case NOT_CONTAINING:
                            andPredicateJoiner.add(column + " NOT LIKE ?");
                            break;
                        case EMPTY:
                            andPredicateJoiner.add(column + " = ''");
                            break;
                        case NOT_EMPTY:
                            andPredicateJoiner.add(column + " <> ''");
                            break;
                        case IN:
                            andPredicateJoiner.add(column + " IN (%s)");
                            break;
                        case NOT_IN:
                            andPredicateJoiner.add(column + " NOT IN (%s)");
                            break;
                        case TRUE:
                            andPredicateJoiner.add(column + " IS TRUE");
                            break;
                        case FALSE:
                            andPredicateJoiner.add(column + " IS FALSE");
                            break;
                        case NOT_EQUAL:
                            andPredicateJoiner.add(column + " <> ?");
                            break;
                        case EQUAL:
                            andPredicateJoiner.add(column + " = ?");
                            break;
                    }
                }
            }
            orPredicateJoiner.add(andPredicateJoiner.toString());
        }

        return " WHERE " + orPredicateJoiner;
    }
}
