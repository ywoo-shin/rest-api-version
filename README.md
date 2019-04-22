## REST API VERSION 관리
* Springframework 환경에서 annotation 기반의 REST API의 버전을 관리할 수 있도록 지원

```java
@Import(MvcAnnotationDriven.class)
public class AppConfig {
  // prefix: 버전앞에 추가될 동일한 속성값을 기입합니다.
  // versions: 지원하는 버전 리스트를 기입합니다.
  @Bean
  public VersionRequestMappingHandlerMapping versionRequestMappingHandlerMapping() {
    return new VersionRequestMappingHandlerMapping().setPrefix("/v")
                                                    .setVersions(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
  }
  
  private List<HttpMessageConverter<?>> messageConverters() {
    List<HttpMessageConverter<?>> list = new ArrayList<>();
    list.add(new MappingJackson2HttpMessageConverter());
    return list;
  }
 
  @Bean
  public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
    ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
    exceptionHandlerExceptionResolver.setMessageConverters(messageConverters());     
    return exceptionHandlerExceptionResolver;
  }
 
  @Bean
  public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
    RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
    requestMappingHandlerAdapter.setMessageConverters(messageConverters());
    return requestMappingHandlerAdapter;
  }
}
```

### Annotation
* `@ApiVersion`
| 속성 | 설명 |
| --- | --- |
| prefix | Controller마다 prefix를 설정 (환경 설정파일의 prefix보다 우선순위를 갖음) |
| fixed | 해당 버전만 지원 |
| from | 시작 버전 |
| to | 종료 버전 |

```java
@RestController
@ApiVersion  // from, to 속성 사용하지 않으면 지원하는 버전 모두를 지칭합니다.
             // prefix 속성을 사용하면 환경 설정파일의 prefix는 무시됩니다.
public class SampleController {
  // 1) 1.0, 2.0, 3.0, 4.0 지원
  @GetMapping("samples/{uuid}") 
  public ResponseBean select(@PathVariable String uuid) {
    return service.select(uuid);
  }
  // 2) 1.0, 2.0 지원
  @ApiVersion(to = "2.0")
  @GetMapping(value = "samples/{uuid}/groups")
  public GroupResponseBean selectGroups(@PathVariable String uuid) {
    return groupService.selectGroups(uuid);
  } 
  // 3) 3.0, 4.0 지원
  @ApiVersion(from = "3.0")
  @GetMapping(value = "samples/{uuid}/groups")
  public GroupResponseBean selectGroups(@PathVariable String uuid) {
    return groupService.selectEnhanceGroups(uuid);
  } 
  // 4) 2.0, 3.0, 4.0 지원
  @ApiVersion(from = "2.0", to = "4.0")
  @GetMapping(value = "samples/{uuid}/test")
  public ResponseBean selectOrg(@PathVariable String uuid) {
    return service.selectSample(uuid);
  }
   
  // 5) 2.0만 지원
  @ApiVersion(fixed = "2.0")
  @GetMapping(value = "samples/{uuid}/projects")
  public ResponseBean selectProject(@PathVariable String uuid) {
    return service.selectProject(uuid);
  } 
}
```

* `@PostApiVersion`
  * 전체 적용이 아닌, 특정 API에만 반영
  
| 속성 | 설명 |
| --- | --- |
| prefix | Controller마다 prefix를 설정 (환경 설정파일의 prefix보다 우선순위를 갖음) |
| fixed | 해당 버전만 지원 |
| from | 시작 버전 |

```java
@RestController
@ApiVersion
public class SampleController {
  @PostApiVersion(from = "4.1")
  @GetMapping(value = "samples/{uuid}/projects")
  public ResponseBean selectProject(@PathVariable String uuid) {
    return projectService.selectProject(uuid);
  } 
}
```
