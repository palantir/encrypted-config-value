Encrypted Config Value
======================
This repository provides tooling for encrypting certain configuration parameter values in dropwizard apps. This defends against accidental leaks of sensitive information such as copy/pasting a config file - unlike jetty obsfucated passwords, one would also have to share the encryption key to actually reveal the sensitive information.

encrypted-config-value
----------------------
Provides the EncryptedConfigValueClass. Use this in your dropwizard configuration, and call the `getDecryptedValue()` method to retrieve the plaintext. By default, this will attempt to read the key from the default location (`var/conf/encrypted-config-value.key`). For more control, one can use the method `getDecryptedValue(KeyWithAlgorithm key)` to specify the key.

###Example Usage
```java
public final class ServiceConfig extends Configuration {
    private final String serviceUrl;
    private final String username;
    private final String password;

    public ServiceConfig(
            @JsonProperty("serviceUrl") String serviceUrl,
            @JsonProperty("username") String username,
            @JsonProperty("password") EncryptedConfigValue password) {
        this.serviceUrl = serviceUrl;
        this.username = username;
        this.password = password.getDecryptedValue();
    }
    
    ...
    
}
```


encrypted-config-value-bundle
-----------------------------
A dropwizard bundle which provides two utility commands:
 - `encrypt-config-value -v <value> [-k <keyfile>]` for encrypting values
 - `generate-random-key -a <algorithm> -n <keysize> [-f <keyfile>]` for generating random keys with the specified [algorithm](https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyGenerator)

###Example Usage
To use in your app, just add the bundle.

```java
public final class Main extends Application<MyApplicationConfig> {

    @Override
    public void initialize(Bootstrap<MyApplicationConfig> bootstrap) {
        ...
        bootstrap.addBundle(new EncryptedConfigValueBundle());
        ...
    }
    
    ...
    
}
 ```

License
-------
This repository is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

