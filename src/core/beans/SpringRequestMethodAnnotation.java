package core.beans;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 */
public enum SpringRequestMethodAnnotation {

    /**
     * RequestMapping
     */
    REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping", null),

    /**
     * GetMapping
     */
    GET_MAPPING("org.springframework.web.bind.annotation.GetMapping", RequestMethod.GET),

    /**
     * PostMapping
     */
    POST_MAPPING("org.springframework.web.bind.annotation.PostMapping", RequestMethod.POST),

    /**
     * PutMapping
     */
    PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping", RequestMethod.PUT),

    /**
     * DeleteMapping
     */
    DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping", RequestMethod.DELETE),

    /**
     * PatchMapping
     */
    PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping", RequestMethod.PATCH),

    /**
     * RequestParam
     */
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", null);

    private final String qualifiedName;
    private final RequestMethod method;

    SpringRequestMethodAnnotation(String qualifiedName, RequestMethod method) {
        this.qualifiedName = qualifiedName;
        this.method = method;
    }

    @Nullable
    public static SpringRequestMethodAnnotation getByQualifiedName(String qualifiedName) {
        for (SpringRequestMethodAnnotation springRequestAnnotation : SpringRequestMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    @Nullable
    public static SpringRequestMethodAnnotation getByShortName(String requestMapping) {
        for (SpringRequestMethodAnnotation springRequestAnnotation : SpringRequestMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().endsWith(requestMapping)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    public RequestMethod getMethod() {
        return this.method;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    @NotNull
    public String getShortName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
    }
}