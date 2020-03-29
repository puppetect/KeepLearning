# Spring Integration


## Message
Message = Header + Payload


## Message Channel
The pipe between message endpoints
- Two general classifications of message channels
    - Pollable Channel
        - May buffer its messages
            - Requires a queue to hold the messages
            - The queue has a designated capacity
        - Waits for the consumer to get the messages
            - Consumers actively poll to receive messages
        - Typically a point-to-point channel
            - Only one receiver of a message in the channel
        - Usually used for sending information or "document" messages between endpoints
    - Subscribable Channel
        - Allows multiple subsribers (or consumers) to register for its messages.
            - Messages are delivered to all registered subscribers on message arrival
            - It has to manage a list of registry of subscribers
        - Doesn't buffer its messages
        - Usually used for "event" messages
            - Notifying the subscribers that something happened and to take appropraite action
- While there are many subtypes, they all implement at least one of these SI channel interfaces
    - see https://docs.spring.io/spring-integration/reference/html/channel.html


## Message Endpoints
- Adapters
    - An endpoint that connects a channel to an external system.
        - It provide the bridge between integration framework and the external systems and services
        - Providing separation of the messaging concerns from the transports and protocols used
    - Adapter are inbound or outbound
        - Those that bring messsages into the SI channels
        - Those that get messages from SI channels to the outside applications, databases, etc
    - Adapters are represented by the follo
    - Spring Integration comes with a number of built-in adapters
        - Stream adapters (like Standard Input and Output stream adapters)
        - File adapters
        - JMS adapters
        - JDBC & JPA adapters
        - FTP & Secure FTP (SFTP) adapters
        - Feed (RSS, Atom, etc.) adapters
        - Mail adapters
        - MongoDB adapters
        - UDP adapters
        - Twitter adapter
    - Use of the Adapter may (usally does) require the addition of another SI module
    - Example: JMS Adapters
        - A JMS Inbound Adapter
            - Takes messages from a message Queue (via JMS under the hood) and gets it to a SI channel
            - Needs a JMS connection factory and queue (configuration not shown below)
            - JMS channel adapters are part of SI's JMS module (int-jms namespace)
            - Note that the adapter pulls the messages into the channel at the poll rate
        ```xml
        <int-jms:inbound-channel-adapter id="my-inbound-jms-adapter"
            connection-factory="jmsQueueConnectionFactorySecured" destination="in.message.queue.name"
            channel="my-message-channel">
            <int:poller fixed-rate="3000"/> <!-- time in milliseconds -->
        </int-jms:inbound-channel-adapter>
        ```
        - A JMS Outbound Adapter
            - Takes messages from a message channel and delivers it to a message Queue (via JMS under the hood)
            - Also, need a JMS queue
        ```xml
        <int-jms:outbound-channel-adapter id="my-outbound-jms-adapter"
            destination="out.message.queue.name" channel="my-message-channel" />
        ```

- Filter
    - Spring Integration filters are endpoints that sit between channels and allow or reject messages from one message channel to the next
        - Filters allow some messages to pass from one channel to another channel
        - Messages not selected are discarded
        - Selection occurs on the basis of message payload or message metadata (header information)
        - The logic of a filter is simple. It must either "accept" or "reject" a message coming from one channel to the next
    - As with adapters, SI provides many filters out of the box.
    - You can create your own custom filter with its own custom message selection criteria
    - Spring Integration provides several ready-to-use filters with the framework
        - You merely have to configure them to use
    - Built-in filters include:
        - Expression Filter - a filter that uses an evaluated SpEL expression against the message to select messages
        - Xpath Filter - User Xpath expressions against the XML payload to select messages
        - XML Validating Filter - select XML payload messages that validate against a given schema
    - Example: String Payload Filter
        - Here is a filter that accepts all String messages that do not start with the text "Hello"
        ```xml
        <int:filter input-channel="inboundChannel" output-channel="outboundChannel"
            expression="payload.startsWith('Hello')"/>
        ```
        - Messages that are rejected are simply removed from the system
            - Optionally, a discard-channel can be specified with a filter to capture and route discarded messages
        ```xml
        <int:filter input-channel="inboundChannel" output-channel="outboundChannel"
            discard-channel="relook-channel" expression="payload.startsWith('Hello')"/>
        ```
        - To create a custom filter, you must implement the SI provided `MessageSelector` interface
            - The MessageSelector's method must have a method that returns a boolean indicating selection or rejection of each message that passed
        ```java
        public class MySelector implements MessageSelector {
            public boolean accept(Message<?> message){
                if(message.getPayload() instanceof String && ((String)message.getPayload()).startsWith("Hello"))
                    return true;
                return false;
            }
        }
        ```
            - Once the MessageSelector is defined, configure a filter to use its logic to do the filtering
        ```xml
        <int:filter input-channel="inboundChannel" output-channel="outboundChannel" ref="selector"/>
        <bean id="selector" class="com.example.MySelector"/>
        ```

- Transformer(convert a message content or structure)
    - Transformer converts the payload or the structure of a message into a modified message
        - For example, convert an XML payload message into a JSON payload message
    - Spring Integration comes with a number of built-in transformers
        - XML to object (unmarchallers) / object to XML (marshallers)
        - Object to string / string to object
        - File to string / string to file
        - Object serializer / deserializer
        - Object to map / map to object
        - Object to JSON / JSON to object
        - Claim check (implementing the claim check design pattern)
    - Use simkple POJOs to create your own custom transformer
    - Example:
        - An object-to-string transformer
        ```xml
        <int:object-to-string-transformer input-channel="inboundChannel" output-channel="outboundChannel"/>
        ```
            - Takes a message with an object payload from the inboundChannel
            - Calls the toString() method of the payload object
            - Puts a message containing the resulting string in the outbound Channel
        - String to string transformer using SpEL
        ```xml
        <int:transformer input-channel="inboundChannel" output-channel="outboundChannel" expression="payload.toUpperCase()"/>
        ```

- Routers
    - Routers act as message distribution components
        - They take messages from one channel and distribute the message to one or more other channels
        - Some routers must inspect a message to determine where to send the message
        - Other routers simply spray the message to all receiving channels
    - Content Routers examine the incoming message content and use the payload type or header value to determine which channel receives the message
        - XPath and Error Message Exception routers also fall under this category
    - Recipient List Routers don't have to examine the message
        - Incoming messages are delivered to all listed recipient channels
    - Example:
        - Below is the configuration for a simple payload type Content Router
        ```xml
        <int:payload-type-router input-channel="inboundChannel">
            <int:mapping-type="java.lang.Double" channel="doubleChannel"/>
            <int:mapping-type="java.lang.Integer" channel="integerChannel"/>
        </int:payload-type-router>
        ```
        - Recipient List Routers deliver messages to all recipients regardless of content
        ```xml
        <int:recipient-list-router id="listRouter" input-channel="inboundChannel">
            <int:recipient channel="outboundA"/>
            <int:recipient channel="outboundB"/>
        </int:recipient-list-router>
        ```

- Enricher
    - Enrichers add information or content to an SI message
        - Enrichers pull messages from a channel, add data or information to the message's header or payload, and post the message to another channel
        - Enrichers are consider variants of transformers
        - Spring provides a number of enrichers out of the box
        - You can also create your own enrichment customization by using additional SI components
    - Example:
        - Header Enrichers
            - Note there are some standard headers, like priority, that are built into configuration
            - Other generic or custom headers can be added with `<header>` elements (like paymentAccountId below)
        ```xml
        <int:header-enricher input-channel="in" output-channel="out">
            <int:priority value="HIGHEST"/>
            <int:header name="paymentAccountId" value="1"/>
        </int:header-enricher>
        ```
        - Payload Enrichers
            - Add data to the message payload, which in this example is about setting a object property to a new Date
        ```xml
        <int:enricher id="orderEnricher" input-channel="in" output-channel="out">
            <int:property name="shipDate" expression="new java.util.Date()"/>
        </int:enricher>
        ```
        - Another Payload Enricher
            - Customize the enricher with a service activator bean. For example, use a request channel to call on the service activator to provide the augmented data.

- Service activator
    - A message endpoint for connecting a Spring object or bean to a message channel
        - The object or bean serves as a service
        - The service is triggered (or activated) by the arrival of a message into the channel
    - If a service produces results, the output is sent to an output channel
    - Example:
        - The service activator configuration must specify the messsage channel that it polls for messages, and the class of the service bean
        ```java
        public class ExampleServiceBean {
            public void printShiporder(Object object){
                System.out.println(order);
            }
        }
        ```
        ```xml
        <int:service-activator id="printing-service-activator" input-channel="in" ref="serviceBean"/>
        <bean id="serviceBean" class="com.example.ExampleServiceBean"/>
        ```
            - Since the service bean has just one method, SI knows what method to call in the bean
            - If there were more public methods in the service or if you with to be explicit, the service activator would also need a `method` attribute.
        - As in the last example, the service (of the service activator) method can optionally have parameters.
            - The argument to a service method could be either a Message or an arbitrary type
            ```java
            public void serviceMethod(Message<Foo> message){
                // service code
            }
            ```
            - If it is an arbitrary type, it's assumed that the argument is the message payload extracted from the triggering message
            ```java
            public void serviceMethod(Foo foo){
                // service code
            }
            ```
        - The service method is not required to have any arguments
            - When passed no arguments, the message serves as a event trigger
            - When the service method has no arguments, the service activator is referred to as *event-style Service Activator*
        - Service methods for the service activator do not have to return a result
            - When they do return a non-null value, the service activator attempts to send a message containing the return value to the configuration designate output-channel
            ```xml
            <int:service-activator id="my-service-activator" input-channel="in" output-channel="out" ref="serviceBean"/>
            ```
            - If there is a return value and no "output-channel" Spring checks the triggering message for a replyChannel header and sends the result to that channel

- Gateway
    - A Spring Integration gateway serves as a facade to a Spring Integration system
        - It hides the SI API or any messaging API from the application
        - The gateway is defined by an interface
        - SI implements the gateway interface under the hood
    - There are two types of gateways
        - Synchronous gateways cause the application to block while the SI system process a request
        - Asynchronous gateways allow the application to continue and retrieve from the SI processes later
    - Interface
        - Applications must provide an interface to make requests of the SI system
            - The interface should be devoid of SI API to keep the application decoupled from SI
            - Under the hood, SI will implement the interface with a `org.springframework.integration.gateway.GatewayProxyFactoryBean`
            ```java
            public interface ShipService{
                Confirmation ship(Order order)
            }
            ```
    - Synchronous Gateway
        - Spring Integration provides the implementation of the gateway based on its configuration
        ```xml
        <int:gateway id="shipService" service-interface="com.example.ShipService"
            default-request-channel="requestChannel" default-reply-channel="replyChannel"/>
        ```
        - The application can then call on the gateway as it would any other Spring bean
        ```java
        ClassPathXMLApplicationContext context = new ClassPathXMLApplicationContext("/META-INF/spring/si-components.xml");
        ShipService service = context.getBean("shipService", ShipService.class);
        Confirmation confirm = service.ship(myOrder);
        ```
    - Asynchronous Gateway
        - The gateway interface should be altered to return an instance of `java.util.concurrent.Future<?>`
        ```java
        public interface ShipService {
            Future<Confirmation> ship(Order order);
        }
        ```
            - The application code still remains loosely coupled from the SI API
            - The application will need to get information from the Future at a point of its choosing by calling get()
        ```java
        ShipService service = context.getBean("shipService", ShipService.class);
        Future<Confirmation> future = service.ship(myOrder);
        // other application work here
        Confirmation confirm = future.get(5000, TimeUnit.SECONDS);
        ```
