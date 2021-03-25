package io.graphine.processor.code.renderer;

import com.squareup.javapoet.CodeBlock;
import io.graphine.processor.code.renderer.parameter.ParameterIndexProvider;
import io.graphine.processor.query.model.parameter.ComplexParameter;
import io.graphine.processor.query.model.parameter.IterableParameter;
import io.graphine.processor.query.model.parameter.Parameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static io.graphine.processor.util.MethodUtils.setter;

/**
 * @author Oleg Marchenko
 */
public class GeneratedKeyParameterRenderer extends ResultSetParameterRenderer {
    public GeneratedKeyParameterRenderer(ParameterIndexProvider parameterIndexProvider) {
        this(Function.identity(), parameterIndexProvider);
    }

    public GeneratedKeyParameterRenderer(Function<CodeBlock, CodeBlock> resultInserter,
                                         ParameterIndexProvider parameterIndexProvider) {
        super(resultInserter, "generatedKeys", parameterIndexProvider);
    }

    @Override
    public CodeBlock visit(ComplexParameter parameter) {
        String parameterName = parameter.getName();

        CodeBlock.Builder builder = CodeBlock.builder();

        List<Parameter> childParameters = parameter.getChildParameters();
        for (Parameter childParameter : childParameters) {
            GeneratedKeyParameterRenderer generatedKeyParameterRenderer =
                    new GeneratedKeyParameterRenderer(code -> CodeBlock.builder()
                                                                       .addStatement("$L.$L($L)",
                                                                                     parameterName,
                                                                                     setter(childParameter.getName()),
                                                                                     code)
                                                                       .build(),
                                                      parameterIndexProvider);
            builder.add(childParameter.accept(generatedKeyParameterRenderer));
        }

        return builder.build();
    }

    @Override
    public CodeBlock visit(IterableParameter parameter) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Parameter iteratedParameter = parameter.getIteratedParameter();

        TypeMirror parameterType = parameter.getType();
        switch (parameterType.getKind()) {
            case ARRAY:
                builder
                        .addStatement("int i = 0")
                        .beginControlFlow("while (generatedKeys.next())")
                        .addStatement("$T $L = $L[i]",
                                      iteratedParameter.getType(), iteratedParameter.getName(), parameter.getName())
                        .add(iteratedParameter.accept(new GeneratedKeyParameterRenderer(parameterIndexProvider)))
                        .addStatement("i++")
                        .endControlFlow();
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) parameterType;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                switch (typeElement.getQualifiedName().toString()) {
                    case "java.lang.Iterable":
                    case "java.util.Collection":
                    case "java.util.List":
                    case "java.util.Set":
                        builder
                                .addStatement("$T<$T> iterator = $L.iterator()",
                                              Iterator.class, iteratedParameter.getType(), parameter.getName())
                                .beginControlFlow("while (generatedKeys.next() && iterator.hasNext())")
                                .addStatement("$T $L = iterator.next()", iteratedParameter.getType(), iteratedParameter.getName())
                                .add(iteratedParameter.accept(new GeneratedKeyParameterRenderer(parameterIndexProvider)))
                                .endControlFlow();
                        break;
                }
                break;
        }

        return builder.build();
    }
}
