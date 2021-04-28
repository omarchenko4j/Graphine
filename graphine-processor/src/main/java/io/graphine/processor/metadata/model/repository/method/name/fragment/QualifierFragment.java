package io.graphine.processor.metadata.model.repository.method.name.fragment;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Oleg Marchenko
 */
public final class QualifierFragment {
    private final MethodType methodType;
    private final MethodForm methodForm;
    private final Set<SpecifierType> specifiers;

    public QualifierFragment(String fragment) {
        this.methodType = MethodType.defineBy(fragment);
        this.methodForm = MethodForm.defineBy(fragment);
        this.specifiers = SpecifierType.defineAllBy(fragment);
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public MethodForm getMethodForm() {
        return methodForm;
    }

    public Set<SpecifierType> getSpecifiers() {
        return specifiers;
    }

    public enum MethodType {
        FIND("find"),
        COUNT("count"),
        SAVE("save"),
        UPDATE("update"),
        DELETE("delete");

        private final String prefix;

        MethodType(String prefix) {
            this.prefix = prefix;
        }

        private static final MethodType[] ALL_TYPES = values();

        public static MethodType defineBy(String value) {
            for (MethodType type : ALL_TYPES) {
                if (value.startsWith(type.prefix)) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum MethodForm {
        SINGULAR,
        PLURAL;

        public static MethodForm defineBy(String value) {
            return value.contains("All") ? PLURAL : SINGULAR;
        }
    }

    public enum SpecifierType {
        DISTINCT,
        FIRST;

        public static Set<SpecifierType> defineAllBy(String value) {
            Set<SpecifierType> specifiers = EnumSet.noneOf(SpecifierType.class);
            if (value.contains("Distinct")) {
                specifiers.add(DISTINCT);
            }
            if (value.contains("First")) {
                specifiers.add(FIRST);
            }
            return specifiers;
        }
    }
}
