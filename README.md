Encrypted Config Value
======================
This repository provides tooling for encrypting certain configuration parameter values in dropwizard apps. This defends against accidental leaks of sensitive information such as copy/pasting a config file - unlike jetty obsfucated passwords, one would also have to share the encryption key to actually reveal the sensitive information.

encrypted-config-value
----------------------
Provides the EncryptedConfigValueClass. Use this in your dropwizard configuration, and call the `getDecryptedValue` method to retrieve the plaintext.

encrypted-config-value-bundle
-----------------------------
A dropwizard bundle which provides two utility commands:
 - `encrypt-config-value -k <keyfile> -v <value>` for encrypting values
 - `generate-random-key -a <algorithm> -n <keysize> [-f <keyfile>]` for generating random keys with the specified [algorithm](https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyGenerator)


License
-------
This repository is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

