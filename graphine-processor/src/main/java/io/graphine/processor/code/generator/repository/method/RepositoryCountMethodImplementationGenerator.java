package io.graphine.processor.code.generator.repository.method;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.ResultSetParameterHighLevelRenderer;
import io.graphine.processor.code.renderer.parameter.NumericParameterIndexProvider;
import io.graphine.processor.metadata.model.repository.method.MethodMetadata;
import io.graphine.processor.query.model.NativeQuery;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryCountMethodImplementationGenerator extends RepositoryMethodImplementationGenerator {
    @Override
    protected CodeBlock renderResultSetParameters(MethodMetadata method, NativeQuery query) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Parameter producedParameter = query.getProducedParameters().get(0);
        builder.add(producedParameter.accept(
                new ResultSetParameterHighLevelRenderer(snippet -> CodeBlock.builder()
                                                                            .addStatement("return $L", snippet)
                                                                            .build(),
                                                        new NumericParameterIndexProvider())
        ));

        ExecutableElement methodElement = method.getNativeElement();
        TypeMirror returnType = methodElement.getReturnType();
        switch (returnType.getKind()) {
            case INT:
            case LONG:
                builder.addStatement("return 0");
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Integer":
                        builder.addStatement("return 0");
                        break;
                    case "java.lang.Long":
                        builder.addStatement("return 0L");
                        break;
                }
                break;
        }

        return builder.build();
    }
}
