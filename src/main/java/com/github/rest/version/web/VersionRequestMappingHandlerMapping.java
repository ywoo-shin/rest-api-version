package com.github.rest.version.web;

import com.github.rest.version.common.annotation.ApiVersion;
import com.github.rest.version.common.annotation.PostApiVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class VersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    private static String prefix;
    private static List<String> versions;

    public VersionRequestMappingHandlerMapping() {}

    public VersionRequestMappingHandlerMapping(String prefix, List<String> versions) {
        VersionRequestMappingHandlerMapping.prefix = prefix;
        VersionRequestMappingHandlerMapping.versions = versions;
    }

    public VersionRequestMappingHandlerMapping(String prefix) {
        VersionRequestMappingHandlerMapping.prefix = prefix;
    }

    public VersionRequestMappingHandlerMapping(List<String> versions) {
       VersionRequestMappingHandlerMapping.versions = versions;
    }

    @Override
    public void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        super.registerHandlerMethod(mapping, method, mapping);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo requestMappingInfo = super.getMappingForMethod(method, handlerType);
        if (requestMappingInfo == null) {
            return null;
        }

        PostApiVersion postApiVersion = AnnotationUtils.findAnnotation(method, PostApiVersion.class);
        if (postApiVersion != null) {
            return createRequestMappingInfo(postApiVersion, requestMappingInfo, handlerType);
        }

        ApiVersion methodApiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if (methodApiVersion != null) {
            return createRequestMappingInfo(methodApiVersion, requestMappingInfo, handlerType);
        }

        ApiVersion typeApiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if (typeApiVersion != null) {
            return createRequestMappingInfo(typeApiVersion, requestMappingInfo, handlerType);
        }
        return requestMappingInfo;
    }

    private RequestMappingInfo createRequestMappingInfo(PostApiVersion apiVersion, RequestMappingInfo requestMappingInfo, Class<?> handlerType) {
        RequestCondition<?> requestCondition = getCustomTypeCondition(handlerType);
            return createApiVersionInfo(apiVersion, requestCondition).combine(requestMappingInfo);
    }

    private RequestMappingInfo createRequestMappingInfo(ApiVersion apiVersion, RequestMappingInfo requestMappingInfo, Class<?> handlerType) {
        RequestCondition<?> requestCondition = getCustomTypeCondition(handlerType);
        return createApiVersionInfo(apiVersion, requestCondition).combine(requestMappingInfo);
    }

    private RequestMappingInfo createApiVersionInfo(PostApiVersion apiVersion, RequestCondition<?> requestCondition) {
        List<String> patternList = getPatterns(apiVersion);
        return createApiVersionInfo(patternList, requestCondition);
    }

    private RequestMappingInfo createApiVersionInfo(ApiVersion apiVersion, RequestCondition<?> requestCondition) {
        List<String> patternList = getPatterns(apiVersion);
        return createApiVersionInfo(patternList, requestCondition);
    }

    private RequestMappingInfo createApiVersionInfo(List<String> patternList, RequestCondition<?> requestCondition) {
        String[] pattern = patternList.toArray(new String[0]);
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(pattern, getUrlPathHelper(), getPathMatcher(), useSuffixPatternMatch(), useTrailingSlashMatch(), getFileExtensions());
        return new RequestMappingInfo(patternsRequestCondition, new RequestMethodsRequestCondition(), new ParamsRequestCondition(), new HeadersRequestCondition(), new ConsumesRequestCondition(), new ProducesRequestCondition(), requestCondition);
    }

    private List<String> getPatterns(PostApiVersion apiVersion) {
        List<String> patterns = new ArrayList<>();
        if (isNotEmpty(apiVersion.fixed())) {
            patterns.add(getPrefix(apiVersion) + apiVersion.fixed());
        }
        int from = 0;
        if (isNotEmpty(apiVersion.from())) {
            patterns.add(getPrefix(apiVersion) + apiVersion.from());
            from = getGreaterThanIndexOfVersion(apiVersion.from());
        }
        for (int i = from; i < versions.size(); i++) {
            patterns.add(getPrefix(apiVersion) + versions.get(i));
        }
        return patterns;
    }

    private List<String> getPatterns(ApiVersion apiVersion) {
        List<String> patterns = new ArrayList<>();
        if (isNotEmpty(apiVersion.fixed())) {
            getIndexOfVersion(apiVersion.fixed());
            patterns.add(getPrefix(apiVersion) + apiVersion.fixed());
            return patterns;
        }
        int from = 0;
        int to = versions.size() - 1;
        if (isNotEmpty(apiVersion.from())) {
            from = getIndexOfVersion(apiVersion.from());
        }
        if (isNotEmpty(apiVersion.to())) {
            to = getIndexOfVersion(apiVersion.to());
        }
        for (int i = from; i <= to; i++) {
            patterns.add(getPrefix(apiVersion) + versions.get(i));
        }
        return patterns;
    }

    private String getPrefix(PostApiVersion apiVersion) {
        return getPrefix(apiVersion.prefix());
    }

    private String getPrefix(ApiVersion apiVersion) {
        return getPrefix(apiVersion.prefix());
    }

    private String getPrefix(String apiVersion) {
        return StringUtils.isEmpty(apiVersion) ? prefix : apiVersion;
    }

    private int getIndexOfVersion(String version) {
        int index = versions.indexOf(version);
        if (index == -1) {
            throw new IndexOutOfBoundsException("The version(" + version + ") of API is not specified in your application.");
        }
        return index;
    }

    private int getGreaterThanIndexOfVersion(String version) {
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).compareTo(version) > 0) {
                return i;
            }
        }
        return versions.size();
    }

    private boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }
}
