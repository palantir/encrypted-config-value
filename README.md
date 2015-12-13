Encrypted Config Value
======================
This repository provides tooling for encrypting certain configuration parameter values in dropwizard apps. This defends against accidental leaks of sensitive information such as copy/pasting a config file - unlike jetty obsfucated passwords, one would also have to share the encryption key to actually reveal the sensitive information.

encrypted-config-value
----------------------
Provides the EncryptedConfigValue class. Use this class in your dropwizard configuration, and call `getDecryptedValue()` to 
retrieve the corresponding plaintext. This method will attempt to load the key from the location specified by the Java property
`palantir.config.key_path` or use a default location of `var/conf/encrypted-config-value.key`. Alternatively, pass the key using
the alternate method `getDecryptedValue(KeyWithAlgorithm key)`.

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

Additionally, the bundle sets the `ConfigurationSourceProvider` to one capable of parsing encrypted values specified as variables.

###Example Usage
To use in your app, just add the bundle.

```java
public final class Main extends Application<MyApplicationConfig> {
    @Override
    public void initialize(Bootstrap<MyApplicationConfig> bootstrap) {
        ...
        bootstrap.addBundle(new EncryptedConfigValueBundle());
    }
    ...
}
```
 
then

```console
my-application$ ./bin/my-dropwizard-app generate-random-key -a AES -n 128
Wrote key to var/conf/encrypted-config-value.key
my-application$ ./bin/my-dropwizard-app encrypt-config-value -v topSecretPassword $HONK
enc:V92jePHsFbT0PxdJoer+oA== 
```

For config values of type `EncryptedConfigValue`, just set the value to the string from the above calls:

```yaml
var: enc:V92jePHsFbT0PxdJoer+oA==
```

Or for `String` types, use variable substitution:

```yaml
some-string: ${enc:V92jePHsFbT0PxdJoer+oA==}
```

License
-------
This repository is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

