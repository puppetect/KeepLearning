# Mokito

Test made sweet with Mockito by Jeroen Mols ([YouTube](https://www.youtube.com/watch?v=DJDBl0vURD4))

## Definition: Mock & Stub
What's mock?
- Generated class
- Doesn't do anything (returning void or default value)
- **Behavior** verification

What's stub?
- Handwritten class
- Returns predefined responses
- **State** verification

## Ingredients
### Creating a mock
```java
import static org.mockito.Mockito.mock;
@Test
public void createWork() throws Exception {
    WebService mockWebService = mock(WebService.class);
    new User(mockWebService, 0, null);
}
```
or
```java
// initialize and verify all mockito annotations
@Rule
public MockitoRule mokitoRule = MockitoJUnit.rule();
@Mock
private WebService mockWebService;
@Test
public void createMockUsingAnnotation() throws Exception {
    new User(mockWebService, 0, null);
}
```

### Verify interactions

#### verify times
Being overly rectictive makes test brittle
```java
@Test
public void verifyInteractionTimes() throws Exception {
    User user = new User(mockWebService, USER_ID, PASSWORD);
    user.logout();
    // check how many times the service's method is called
    verify(mockWebService, times(1)).logout();
                           atLeast(1)
                           atLeastOnce()
                           atMost(1)
                           only()
                           never()
}
```

#### verify parameters
One matcher -> all arguments need to be matchers
* Mockito.Matchers *
* Mockito.AdditionalMatchers *
```java
    verify(mockWebService).login(anyInt());
                                 anyString()
                                 any(Response.class)
                                 gt(0)
                                 lt(10000)
                                 leq(10000)
                                 not(eq(0))
                                 not(eq("123"))
                                 and(gt(0), lt(10000))
```

#### verify order
```java
@Test
public void verifyInteractionOrder() throws Exception {
    User user = new User(mockWebService, USER_ID, PASSWORD);
    user.login(null);
    user.logout();

    Inorder inOrder = inOrder(mockWebService);
    inOrder.verify(mockWebService).login();
    inOrder.verify(mockWebService).logout();
}
```

### Stubbing methods
```java
@Test
public void stubMethod() throws Exception {
    User user = new User(mockWebService, USER_ID, PASSWORD);
    when(mockWebService.isOffline()).thenReturn(true);
                                                true, false, true
                                                MyException.class
                                                new Answer<Boolean>() {
                                                    int index = 0;
                                                    @Override
                                                    public Boolean answer(InvocationOnMock in) throws ... {
                                                        return index++ % 2 > 0;
                                                    }
                                                }

    user.login(mockLoginInterface);
    verify(mockWebService, never()).login();
}
```

#### stubbing methods
- Normal syntax
`when(mockWebService.isOffline()).thenReturn(true);`
- Alternative syntax
`doReturn(true).when(mockWebService).isOffline();`
- BDD syntax
`given(mockWebService.isOffline()).willReturn(true);`

### Capturing arguments
```java
public void login(final loginInterface loginInterface){
    Response response = new Response() {
        @Override
        public void onRequestCompleted(...){
            ...
        }
    }
    webService.login(userId, password, response);
}
```
* Response.java*
```java
public class Response {
    public void onRequestCompleted(boolean success, String data) {
        if(success){
            loginInterface.onLoginSuccess();
        } else {
            loginInterface.onLoginFailed();
        }
    }
}
```
```java
@Captor
private ArgumentCaptor<Response> responseCaptor;
@Test
public void captureArguments() throws Exception {
    User user = new User(mockWebService, USER_ID, PASSWORD);
    user.login(mockLoginInterface);
    verify(mockWebService).login(responseCaptor.capture());
    Response response = responseCaptor.getValue();
    response.onRequestCompleted(true);
    verify(mockLoginInterface).onLoginSuccess();
}
```

## Mockito limitations
Unable to mock
- final classes => opt-in with Mockito 2.x
- final methods => opt-in with Mockito 2.x
- static methods
- private methods
- hashCode() and equals()

## Advanced Mockito

### Testing UI
