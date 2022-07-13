package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.mapping.ResultSetMappingRenderer;
import io.graphine.processor.code.renderer.mapping.StatementMappingRenderer;
import io.graphine.processor.metadata.model.entity.EntityMetadata;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.metadata.registry.EntityMetadataRegistry;
import io.graphine.processor.query.model.NativeQuery;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    public RepositoryCountMethodImplementationGenerator(EntityMetadataRegistry entityMetadataRegistry,
                                                        StatementMappingRenderer statementMappingRenderer,
                                                        ResultSetMappingRenderer resultSetMappingRenderer) {
        super(entityMetadataRegistry, statementMappingRenderer, resultSetMappingRenderer);
    }

    @Override
    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query, EntityMetadata entity) {
        ExecutableElement methodElement = method.getNativeElement();
        TypeMirror returnType = methodElement.getReturnType();

        CodeBlock.Builder snippetBuilder = CodeBlock.builder();
        snippetBuilder
                .beginControlFlow("if ($L.next())", RESULT_SET_VARIABLE_NAME)
                .addStatement("return $L", resultSetMappingRenderer.render(returnType, "1"))
                .endControlFlow();

        switch (returnType.getKind()) {
            case INT:
            case LONG:
                snippetBuilder.addStatement("return 0");
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Integer":
                        snippetBuilder.addStatement("return 0");
                        break;
                    case "java.lang.Long":
                        snippetBuilder.addStatement("return 0L");
                        break;
                }
                break;
        }
        return snippetBuilder.build();
    }
}
